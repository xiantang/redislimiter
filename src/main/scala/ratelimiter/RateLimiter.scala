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

                   lockManager: LockManager
                 ) {

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

  def reserveAndGetWaitLength(permits: Int): Future[Long] = {
    Future {
      1L
    }
  }

  def reserve(permits: Int): Future[Long] = {
    lockManager.lock("key", "value").flatMap {
      case Some(lock) =>
        for {
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
