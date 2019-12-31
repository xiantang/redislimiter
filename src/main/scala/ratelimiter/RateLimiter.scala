package ratelimiter

import java.util.concurrent.TimeUnit

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
                 ) {

  def acquire(permits: Int): Future[Double] = {
    for {
      microsToWait <- reserve(permits);
      _ <- Future {
        println(microsToWait)
        TimeUnit.MILLISECONDS.sleep(microsToWait)
      }
    } yield {
      microsToWait
    }

  }


  def reSync(): Future[(RedisPermits,Long)] = {
    lockManager.now().flatMap {
      now =>
        println("get now")
        redisPermitsTemplate.queryPermits(name).flatMap {
          permits =>
            println("queried permit")
            if (permits.nextFreeTicketMillis < now) {
              val newPermits = (now - permits.nextFreeTicketMillis) / permits.intervalMillis
              val storedPermits = math.min(permits.maxPermits, newPermits + permits.storePermits)
              Future {
                (permits.copy(storePermits = storedPermits, nextFreeTicketMillis = now),now)
              }
            } else {
              Future {
                (permits,now)
              }
            }
        }

    }
  }


  def reserveAndGetWaitLength(requiredPermits: Int): Future[Long] = {
    reSync().flatMap {
      tuple =>
      val  (permits:RedisPermits,now:Long) = tuple
        println("get permits")
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
        }
    }
  }

  def checkPermits(): Future[RedisPermits] = {
    println("check permits")
    redisPermitsTemplate.exists(permits).flatMap {
      case false =>
        println("create permits")
        redisPermitsTemplate.insertPermits(permits).flatMap {
          case true =>
            Future {
              println("insert permits")
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
    lockManager.lock(name + ":lock", "value").flatMap {
      case Some(lock) =>
        for {
          _ <- checkPermits();
          waitLength <- reserveAndGetWaitLength(permits);
          _ <- lockManager.unLock(lock)
        } yield {
          waitLength
        }
      case None =>
        // TODO Failed
        Future {
          1000L
        }
    }

  }


}

object RateLimiter {

}
