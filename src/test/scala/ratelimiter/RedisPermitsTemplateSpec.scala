package ratelimiter

import org.scalatest._


/**
 * @author zhujingdi
 * @since 2019/12/30
 */
class RedisPermitsTemplateSpec extends AsyncFlatSpec {

  "RedisPermitsTemplate" should "insert a permits" in {
    val template = new RedisPermitsTemplate()
    val testPermits = RedisPermits("test", 1, 0, 500, 0)
    for {
      status <- template.insertPermits(testPermits)
    } yield assert(status)
  }


  "RedisPermitsTemplate" should "get a permits" in {
    val template = new RedisPermitsTemplate()
    val testPermits = RedisPermits("test", 1, 0, 500, 0)
    for {
      status <- template.insertPermits(testPermits)
      result <- template.queryPermits(testPermits.name)
    } yield {
      assert(result == testPermits)
    }
  }


}
