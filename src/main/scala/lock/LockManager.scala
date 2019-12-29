package lock

import scala.concurrent.Future

/**
 * @author zhujingdi
 * @since 2019/12/29
 */
trait LockManager {
  val DEFAULT_EXPIRY_TIME_MILLIS: Long = 60 * 1000L

  def tryLock(key: String, value: String, expireTimeMillis: Long = DEFAULT_EXPIRY_TIME_MILLIS): Future[Option[Lock]]
}
