package ratelimiter

import java.util.UUID
import java.util.concurrent.TimeUnit

import org.scalatest._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
/**
 * @author zhujingdi
 * @since 2019/12/31
 */
class RateLimiterFactorySpec extends FlatSpec {
  "RateLimiterFactory " should "create a limitRater get 2 permit per second" in {
    val limiter = RateLimiterFactory.newRateLimiter("test",2)
    val template = new RedisPermitsTemplate()
    for {
      result <- template.queryPermits("test")
    } yield {
      assert(result!= null)
    }
  }

  "RateLimiterFactory" should "create a new RateLimiter" in {
    val key = UUID.randomUUID().toString

    val limiter = RateLimiterFactory.newRateLimiter(key,0.5)
    limiter.acquire(1).flatMap{
      x =>
        println(x)
        Future{}
    }
    limiter.acquire(1)
      .flatMap{
        x =>
          println(x)
          Future{}
      }
    limiter.acquire(1)
      .flatMap{
        x =>
          println(x)
          Future{}
      }
    TimeUnit.SECONDS.sleep(20)
    assert(true)
  }

}
