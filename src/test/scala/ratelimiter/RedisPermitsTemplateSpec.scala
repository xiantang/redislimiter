package ratelimiter


import org.scalatest._
import redis.RedisServer


/**
 * @author zhujingdi
 * @since 2019/12/30
 */
class RedisPermitsTemplateSpec extends AsyncFlatSpec {

  "RedisPermitsTemplate" should "insert a permit" in {
    val template = new RedisPermitsTemplate(RedisServer("localhost", 6379))
    val testPermits = RedisPermits("test", 1, 0, 500, 0)
    for {
      status <- template.insertPermits(testPermits)
    } yield assert(status)
  }


  "RedisPermitsTemplate" should "get a permit" in {
    val template = new RedisPermitsTemplate(RedisServer("localhost", 6379))
    val testPermits = RedisPermits("test", 1, 0, 500, 0)
    for {
      _ <- template.insertPermits(testPermits)
      result <- template.queryPermits(testPermits.name)
    } yield {
      assert(result == testPermits)
    }
  }


}
