package lock

import scala.concurrent.Future

/**
 * @author zhujingdi
 * @since 2019/12/29
 */
trait LockManager {
  lazy val DEFAULT_EXPIRY_TIME_MILLIS: Long = 60 * 1000L
  lazy val DEFAULT_SAFE_TIME: Long = 10000
  lazy val DEFAULT_WAIT_MILE: Long = 10


  def lock(key: String, expireTimeMillis: Long = DEFAULT_EXPIRY_TIME_MILLIS,
           safeTime: Long = DEFAULT_SAFE_TIME): Future[Option[Lock]]

  def tryLock(key: String, expireTimeMillis: Long = DEFAULT_EXPIRY_TIME_MILLIS): Future[Option[Lock]]

  def unLock(lock: Lock): Future[Boolean]

  def now(): Future[Long]


}
