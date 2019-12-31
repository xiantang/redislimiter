package lock

import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import redis.protocol.{Bulk, Integer}
import redis.{RedisClientPool, RedisServer}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * @author zhujingdi
 * @since 2019/12/29
 */
class RedisLockManageImpl extends LockManager with LazyLogging {


  implicit val akkaSystem: ActorSystem = akka.actor.ActorSystem()
  private val redisClient = RedisClientPool(Seq(RedisServer("localhost", 6379)))
  private val WAIT_TIME = 500
  private val RELEASE_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end"
  private val TIME_SCRIPT = "return redis.call('TIME')[1]"

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

  override def tryLock(key: String): Future[Option[Lock]] = {
    val value = UUID.randomUUID().toString
    tryLock(key, value)
  }

  override def lock(key: String, value: String, expireTimeMillis: Long = DEFAULT_EXPIRY_TIME_MILLIS,
                    safeTime: Long = DEFAULT_SAFE_TIME): Future[Option[Lock]] = {
    def loop(currentWait: Long): Future[Option[Lock]] = {
      if (currentWait > DEFAULT_SAFE_TIME) {
        println(s"failed get lock key ${key} value ${value}")
        Future {
          None
        }
      }
      else {
        tryLock(key, value, expireTimeMillis).flatMap {
          case Some(lock) =>
            println(s"success get the lock key ${key} value ${value}")
            Future {
              Some(lock)
            }
          case _ =>
            for {
              _ <- Future {
                TimeUnit.MILLISECONDS.sleep(WAIT_TIME)
              }
              lock <- loop(currentWait + WAIT_TIME)
            } yield lock
        }
      }
    }

    loop(0)
  }

  override def unLock(key: String, value: String): Future[Boolean] = {
    unLock(Lock(key, value))
  }

  override def unLock(lock: Lock): Future[Boolean] = {
    val result = redisClient.eval(RELEASE_SCRIPT, Seq(lock.key), Seq(lock.value))

    result.map {
      case s: Integer if s.toBoolean =>
        println(s"success unlock key ${lock.key} value ${lock.value}")

        true
      case _ =>
        println(s"failed unlock key ${lock.key} value ${lock.value}")
        false
    }
  }

  override def now(): Future[Long] = {
    redisClient.eval(TIME_SCRIPT,Seq("0"),Seq("0")).map{
      case b:Bulk  =>
        val result = b.toString()
        result.toLong
    }
  }


}
