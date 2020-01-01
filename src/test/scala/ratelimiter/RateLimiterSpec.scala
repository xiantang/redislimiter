package ratelimiter

import java.util.UUID
import java.util.concurrent.TimeUnit

import com.typesafe.scalalogging.LazyLogging
import org.scalatest._

import scala.concurrent.Future

/**
 * @author zhujingdi
 * @since 2019/12/30
 */
class RateLimiterSpec extends AsyncFlatSpec with LazyLogging {

  "RateLimiter" should "do request every 1 second" in {
    val rateLimiter = RateLimiterFactory.newRateLimiter(UUID.randomUUID().toString, 1)
    for {
      _ <- doRequest(rateLimiter)
      _ <- doRequest(rateLimiter)
      _ <- doRequest(rateLimiter)
    } yield {
      assert(true)
    }
  }

  "RateLimiter" should "do async request ever 1 second" in {
    val rateLimiter = RateLimiterFactory.newRateLimiter(UUID.randomUUID().toString, 1)

    doRequest(rateLimiter)
    doRequest(rateLimiter)
    doRequest(rateLimiter)
    TimeUnit.SECONDS.sleep(6)
    assert(true)

  }

  def doRequest(limiter: RateLimiter): Future[_] = {
    limiter.acquire(1).flatMap {
      x => {
        logger.info(x +"")
        Future {}
      }
    }
  }
}
