package ratelimiter

import lock.{LockManager, RedisLockManageImpl}

/**
 * @author zhujingdi
 * @since 2019/12/30
 */
object RateLimiterFactory {

  def newRateLimiter(name: String, permitsPerSecond: Double): RateLimiter = {
    val permits = RedisPermits(name,1, 0, (1000.0 / permitsPerSecond).toLong, 0)
    val lockManager: LockManager = new RedisLockManageImpl()
    new RateLimiter(name, lockManager, permits, new RedisPermitsTemplate())
  }
}
