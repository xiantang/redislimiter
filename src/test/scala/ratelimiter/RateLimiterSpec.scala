package ratelimiter

import java.util.concurrent.TimeUnit

import lock.RedisLockManageImpl
import org.scalatest._

import scala.concurrent.Future

/**
 * @author zhujingdi
 * @since 2019/12/30
 */
class RateLimiterSpec extends AsyncFlatSpec {

  "RateLimiter" should "do request ever 1 second" in {
    val lockManager = new RedisLockManageImpl()
    val limiter = new RateLimiter("test", lockManager, null,
      new RedisPermitsTemplate())
    for {
      _ <- doRequest(limiter)
      _ <- doRequest(limiter)
      _ <- doRequest(limiter)
    } yield {
      assert(true)
    }
  }

  "RateLimiter" should "do async request ever 1 second" in {
    val lockManager = new RedisLockManageImpl()
    val limiter = new RateLimiter("test", lockManager, null,
      new RedisPermitsTemplate())
    doRequest(limiter)
    doRequest(limiter)
    doRequest(limiter)
    TimeUnit.SECONDS.sleep(6)
    assert(true)

  }

  def doRequest(limiter: RateLimiter): Future[Long] = {
    limiter.acquire(1).flatMap {
      x => {
        println(x)
        Future {
          1000L
        }
      }

    }
  }
}
