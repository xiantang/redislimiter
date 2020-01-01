package lock

import java.util.UUID
import java.util.concurrent.TimeUnit

import com.typesafe.scalalogging.LazyLogging
import ratelimiter.RedisClientBase
import redis.RedisServer
import redis.protocol.Integer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * @author zhujingdi
 * @since 2019/12/29
 */
class RedisLockManageImpl(redisServer: RedisServer) extends
  RedisClientBase(redisServer)
  with LockManager
  with LazyLogging {

  private val RELEASE_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end"
  private val TIME_SCRIPT = "local a=redis.call('TIME') ;return a[1]*1000+a[2]/1000"

  override def tryLock(key: String, expireTimeMillis: Long = DEFAULT_EXPIRY_TIME_MILLIS): Future[Option[Lock]] = {
    val uuid = UUID.randomUUID().toString
    val lock = Lock(key, uuid)
    redisClient.set(key, uuid, pxMilliseconds = Some(DEFAULT_EXPIRY_TIME_MILLIS), NX = true)
      .map {
        case true =>
          Some(lock)
        case _ =>
          None
      }
  }


  override def lock(key: String, expireTimeMillis: Long = DEFAULT_EXPIRY_TIME_MILLIS,
                    safeTime: Long = DEFAULT_SAFE_TIME): Future[Option[Lock]] = {

    def loop(currentWait: Long): Future[Option[Lock]] = {
      if (currentWait > DEFAULT_SAFE_TIME) {
        logger.error(s"failed get lock key ${key}")
        Future {
          None
        }
      }
      else {
        tryLock(key, expireTimeMillis).flatMap {
          case Some(lock) =>
            logger.info(s"success get the lock key ${key} uuid ${lock.uuid}")
            Future {
              Some(lock)
            }
          case _ =>
            for {
              _ <- Future {
                TimeUnit.MILLISECONDS.sleep(DEFAULT_WAIT_MILE)
              }
              lock <- loop(currentWait + DEFAULT_WAIT_MILE)
            } yield lock
        }
      }
    }

    loop(0)
  }


  override def unLock(lock: Lock): Future[Boolean] = {
    val result = redisClient.eval(RELEASE_SCRIPT, Seq(lock.key), Seq(lock.uuid))
    result.map {
      case s: Integer if s.toBoolean =>
        logger.info(s"success unlock key ${lock.key} uuid ${lock.uuid}")

        true
      case _ =>
        logger.error(s"failed unlock key ${lock.key} uuid ${lock.uuid}")
        false
    }
  }

  override def now(): Future[Long] = {
    redisClient.eval(TIME_SCRIPT, Seq("0")).map {
      case i: Integer =>
        val result = i.toLong
        result
    }
  }


}
