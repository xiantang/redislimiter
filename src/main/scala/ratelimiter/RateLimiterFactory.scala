package ratelimiter

/**
 * @author zhujingdi
 * @since 2019/12/30
 */
object RateLimiterFactory {

  def newRateLimiter(name:String): RateLimiter ={
    val limiter = RedisPermits(name, 1, 0, 500, 0)
    null
//    limiter.toJson()
  }
}
