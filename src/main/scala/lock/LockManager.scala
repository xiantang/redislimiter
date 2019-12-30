package lock

import scala.concurrent.Future

/**
 * @author zhujingdi
 * @since 2019/12/29
 */
trait LockManager {
  val DEFAULT_EXPIRY_TIME_MILLIS: Long = 60 * 1000L
  val DEFAULT_SAFE_TIME:Long= 10000


  def tryLock(key: String, value: String, expireTimeMillis: Long = DEFAULT_EXPIRY_TIME_MILLIS): Future[Option[Lock]]

  def tryLock(key: String): Future[Option[Lock]]

  def lock(key: String, value: String, expireTimeMillis: Long =
  DEFAULT_EXPIRY_TIME_MILLIS, safeTime: Long = DEFAULT_SAFE_TIME): Future[Option[Lock]]

  def unLock(key: String, value: String): Future[Boolean]

  def unLock(lock: Lock): Future[Boolean]
}
