package ratelimiter

import java.util.concurrent.TimeUnit

import com.typesafe.scalalogging.LazyLogging
import lock.LockManager

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * @author zhujingdi
 * @since 2019/12/29
 */
class RateLimiter(
                   name: String,
                   lockManager: LockManager,
                   permits: RedisPermits,
                   redisPermitsTemplate: RedisPermitsTemplate
                 ) extends LazyLogging {

  def acquire(permits: Int): Future[Double] = {
    for {
      microsToWait <- reserve(permits);
      _ <- Future {
        TimeUnit.MILLISECONDS.sleep(microsToWait)
      }
    } yield {
      microsToWait
    }

  }


  def reSync(): Future[(RedisPermits, Long)] = {
    lockManager.now().flatMap {
      now =>
        logger.info(s"get redis now :${now}")
        redisPermitsTemplate.queryPermits(name).flatMap {
          permits =>
            logger.info(s"queried permits, permits : ${permits}")
            if (permits.nextFreeTicketMillis < now) {
              val newPermits = (now - permits.nextFreeTicketMillis) / permits.intervalMillis
              val storedPermits = math.min(permits.maxPermits, newPermits + permits.storePermits)
              Future {
                (permits.copy(storePermits = storedPermits, nextFreeTicketMillis = now), now)
              }
            } else {
              Future {
                (permits, now)
              }
            }
        }

    }
  }


  def reserveAndGetWaitLength(requiredPermits: Int): Future[Long] = {
    reSync().flatMap {
      result =>
        val (permits: RedisPermits, now: Long) = result
        logger.info(s"get permits , permits ${permits}")
        val tobeSpend = Math.min(permits.storePermits, requiredPermits)
        val freshPermits = requiredPermits - tobeSpend
        val waitTime = freshPermits * permits.intervalMillis
        val storePermits = permits.storePermits - tobeSpend
        val nextFreeTicketMillis = permits.nextFreeTicketMillis + waitTime
        val newPermits = permits.copy(storePermits = storePermits, nextFreeTicketMillis = nextFreeTicketMillis)
        redisPermitsTemplate.insertPermits(newPermits).flatMap {
          case true =>
            Future {
              nextFreeTicketMillis - now
            }
          case false =>
            throw new Exception("can not insert permits to redis, please check your connection")
        }
    }
  }

  def checkPermits(): Future[RedisPermits] = {
    logger.info(s"check permits : ${permits}")
    redisPermitsTemplate.exists(permits).flatMap {
      case false =>
        logger.info(s"permit not exists , create permits ${permits}")
        redisPermitsTemplate.insertPermits(permits).flatMap {
          case true =>
            Future {
              logger.info(s"insert permits :${permits}")
              permits
            }
        }
      case true =>
        redisPermitsTemplate.queryPermits(permits.name).flatMap {
          permits =>
            Future {
              permits
            }
        }
    }
  }

  def reserve(permits: Int): Future[Long] = {
    val lockName = name + ":lock"
    lockManager.lock(lockName).flatMap {
      case Some(lock) =>
        for {
          _ <- checkPermits();
          waitLength <- reserveAndGetWaitLength(permits);
          _ <- lockManager.unLock(lock)
        } yield {
          waitLength
        }
      case None =>
        throw new Exception(s"can not  get the lock , name :${lockName}")
    }
  }


}

