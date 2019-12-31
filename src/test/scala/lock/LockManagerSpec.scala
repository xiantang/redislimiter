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
      assert(some.contains(Lock(key, value)))
    }

    for (
      lock <- lockManager.tryLock(key, value)
    ) yield {
      assert(lock.isEmpty)
    }
  }


  "lock " should "wait safe time" in {

    val lockManager = new RedisLockManageImpl()
    val key = UUID.randomUUID().toString
    val value = UUID.randomUUID().toString
    for {
      _ <- lockManager.tryLock(key, value);
      lock2 <- lockManager.lock(key, value)
    } yield {
      assert(lock2.isEmpty)
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
      assert(lockAgain.contains(Lock(key, value)))
    }
  }


  "now " should "be a long" in {
    val lockManager = new RedisLockManageImpl()
    for{
      now <- lockManager.now()
    }yield {
      println(now)
      assert(now > 1000)
    }
  }
}
