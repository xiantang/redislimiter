package lock

import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import redis.protocol.Integer
import redis.{RedisClientPool, RedisServer}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * @author zhujingdi
 * @since 2019/12/29
 */
class RedisLockManageImpl extends LockManager {


  implicit val akkaSystem: ActorSystem = akka.actor.ActorSystem()
  private val redisClient = RedisClientPool(Seq(RedisServer("localhost", 6379)))
  private val SAFE_TIME = 10000
  private val WAIT_TIME = 500
  private val RELEASE_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end"


  override def tryLock(key: String, value: String, expireTimeMillis: Long = DEFAULT_EXPIRY_TIME_MILLIS): Future[Option[Lock]] = {
    val lock = Lock(key, value)
    redisClient.set(key, value, pxMilliseconds = Some(DEFAULT_EXPIRY_TIME_MILLIS), NX = true)
      .map {
        case true =>
          Some(lock)
        case _ =>
          None
      }
  }

  def tryLock(key: String): Future[Option[Lock]] = {
    val value = UUID.randomUUID().toString
    tryLock(key, value)
  }

  def lock(key: String, value: String, expireTimeMillis: Long = DEFAULT_EXPIRY_TIME_MILLIS, safeTime: Long = SAFE_TIME): Future[Option[Lock]] = {
    def loop(currentWait: Long): Future[Option[Lock]] = {
      if (currentWait > SAFE_TIME) {
        tryLock(key, value, expireTimeMillis)
      }
      else {
        tryLock(key, value, expireTimeMillis).flatMap {
          case Some(lock) =>
            Future {Some(lock)}
          case _ =>
            for {
              _ <- Future {TimeUnit.MILLISECONDS.sleep(WAIT_TIME)}
              lock <- loop(currentWait + WAIT_TIME)
            } yield lock
        }
      }
    }

    loop(0)
  }

  def unLock(lock: Lock): Future[Boolean] = {
    val result = redisClient.eval(RELEASE_SCRIPT, Seq(lock.key), Seq(lock.value))
    result.map {
      case s: Integer if s.toBoolean =>
        true
      case _ =>
        false
    }
  }


}
