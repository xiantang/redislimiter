package lock


/**
 * @author zhujingdi
 * @since 2019/12/29
 */


import java.util.UUID

import org.scalatest._
import redis.RedisServer

class LockManagerSpec extends AsyncFlatSpec {
  "acquire" should "can't again" in {
    val lockManager = new RedisLockManageImpl(RedisServer("localhost", 6379))
    val key = UUID.randomUUID().toString

    val lock = lockManager.tryLock(key)
    for (
      some <- lock
    ) yield {
      assert(some.isDefined)
    }

    for (
      lock <- lockManager.tryLock(key)
    ) yield {
      assert(lock.isEmpty)
    }
  }


  "lock " should "wait safe time" in {

    val lockManager = new RedisLockManageImpl(RedisServer("localhost", 6379))
    val key = UUID.randomUUID().toString
    for {
      _ <- lockManager.tryLock(key);
      lock2 <- lockManager.lock(key)
    } yield {
      assert(lock2.isEmpty)
    }
  }

  "release" should "can acquire again" in {
    val lockManager = new RedisLockManageImpl(RedisServer("localhost", 6379))
    val key = UUID.randomUUID().toString

    for (
      lock <- lockManager.tryLock(key);
      _ <- lockManager.unLock(lock.get);
      lockAgain <- lockManager.tryLock(key)

    ) yield {
      assert(lockAgain.isDefined)
    }
  }


  "now " should "be a long" in {
    val lockManager = new RedisLockManageImpl(RedisServer("localhost", 6379))
    for{
      now <- lockManager.now()
    }yield {
      println(now)
      assert(now > 1000)
    }
  }
}
