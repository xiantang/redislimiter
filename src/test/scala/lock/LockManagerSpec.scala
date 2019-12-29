package lock


/**
 * @author zhujingdi
 * @since 2019/12/29
 */


import java.util.UUID

import org.scalatest._

class LockManagerSpec extends AsyncFlatSpec {
  "acquire" should "can't again" in {
    val lockManager = new RedisLockManageImpl()
    val key = UUID.randomUUID().toString
    val value = UUID.randomUUID().toString

    val lock = lockManager.tryLock(key, value)
    for (
      some <- lock
    ) yield {
      assert(some == Some(Lock(key, value)))
    }

    for (
      lock <- lock
    ) yield {
      assert(lock == Some(None))
    }
  }


  "lock " should "wait safe time" in {

    val lockManager = new RedisLockManageImpl()
    val key = UUID.randomUUID().toString
    val value = UUID.randomUUID().toString
    for {
      lock <- lockManager.tryLock(key, value);
      lock2 <- lockManager.lock(key, value)
    } yield {
      assert(lock2 == None)
    }
  }

  "release" should "can acquire again" in {
    val lockManager = new RedisLockManageImpl()
    val key = UUID.randomUUID().toString
    val value = UUID.randomUUID().toString

    for (
      lock <- lockManager.tryLock(key, value);
      _ <- lockManager.unLock(lock.get);
      lockAgain <- lockManager.tryLock(key, value)

    ) yield {
      assert(lockAgain == Some(Lock(key, value)))
    }
  }
}
