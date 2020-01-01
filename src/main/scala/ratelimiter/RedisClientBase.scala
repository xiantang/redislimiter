package ratelimiter

import akka.actor.ActorSystem
import redis.protocol.Integer
import redis.{RedisClientPool, RedisServer}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * @author zhujingdi
 * @since 2020/1/1
 */
case class RedisClientBase(redisServer: RedisServer) {
  implicit val akkaSystem: ActorSystem = akka.actor.ActorSystem()
  val redisClient = RedisClientPool(Seq(redisServer))
  private val TIME_SCRIPT = "local a=redis.call('TIME') ;return a[1]*1000+a[2]/1000"

  def now(): Future[Long] = {
    redisClient.eval(TIME_SCRIPT, Seq("0")).map {
      case i: Integer =>
        val result = i.toLong
        result
    }
  }
}
