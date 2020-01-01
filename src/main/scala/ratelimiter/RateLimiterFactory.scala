package ratelimiter

import lock.{LockManager, RedisLockManageImpl}
import redis.RedisServer

/**
 * @author zhujingdi
 * @since 2019/12/30
 */
object RateLimiterFactory {

  def newRateLimiter(name: String, permitsPerSecond: Double,maxPermits:Long = 1): RateLimiter = {
    val permits = RedisPermits(name,maxPermits, 0, (1000.0 / permitsPerSecond).toLong, 0)
    val lockManager: LockManager = new RedisLockManageImpl(RedisServer("localhost", 6379))
    new RateLimiter(name, lockManager, permits, new RedisPermitsTemplate(RedisServer("localhost", 6379)))
  }
}
