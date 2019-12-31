package ratelimiter

import akka.actor.ActorSystem
import play.api.libs.json.{JsSuccess, JsValue, Json}
import redis.{RedisClientPool, RedisServer}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


/**
 * @author zhujingdi
 * @since 2019/12/30
 */
class RedisPermitsTemplate {


  implicit val akkaSystem: ActorSystem = akka.actor.ActorSystem()
  private val redisClient = RedisClientPool(Seq(RedisServer("localhost", 6379)))


  def exists(permits: RedisPermits):Future[Boolean] = {
    redisClient.exists(permits.name)
  }

  def insertPermits(permits: RedisPermits): Future[Boolean] = {
    val json = Json.toJson(permits).toString()
    redisClient.set(permits.name, json)
  }


  def queryPermits(name: String): Future[RedisPermits] = {
    redisClient.get[String](name).flatMap{
      case Some(result) =>
        val jsonValue :JsValue= Json.parse(result)
        val jsonPermits = jsonValue.validate[RedisPermits]
        jsonPermits match {
          case JsSuccess(permits, _) => Future{permits}
        }
    }
  }


}
