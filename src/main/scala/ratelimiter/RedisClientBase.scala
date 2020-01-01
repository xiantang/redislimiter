package ratelimiter

import akka.actor.ActorSystem
import redis.{RedisClientPool, RedisServer}

/**
 * @author zhujingdi
 * @since 2020/1/1
 */
case class RedisClientBase(redisServer: RedisServer) {
  implicit val akkaSystem: ActorSystem = akka.actor.ActorSystem()
  val redisClient = RedisClientPool(Seq(redisServer))

}
