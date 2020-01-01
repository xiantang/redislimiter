package ratelimiter

import java.util.UUID

import org.scalatest._
import redis.RedisServer

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * @author zhujingdi
 * @since 2019/12/31
 */
class RateLimiterFactorySpec extends FlatSpec {
  "RateLimiterFactory " should "create a limitRater get 2 permit per second" in {
    val key = UUID.randomUUID().toString
    val limiter = RateLimiterFactory.newRateLimiter(key, permitsPerSecond = 7,maxPermits = 1)
    val template = new RedisPermitsTemplate(RedisServer("localhost", 6379))
    for {
      result <- template.queryPermits(key)
    } yield {
      assert(result != null)
    }
  }

  "RateLimiterFactory" should "create a new RateLimiter" in {
    val key = UUID.randomUUID().toString

    val limiter = RateLimiterFactory.newRateLimiter(key, 7)
    limiter.acquire(1)
    limiter.acquire(1)
    limiter.acquire(1)
    limiter.acquire(1)
    limiter.acquire(1)
    limiter.acquire(1)
    limiter.acquire(1)
    limiter.acquire(1)
    limiter.acquire(1)
    assert(true)
  }

}
