package ratelimiter

import play.api.libs.json.{JsSuccess, JsValue, Json}
import redis.RedisServer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
 * @author zhujingdi
 * @since 2019/12/30
 */
class RedisPermitsTemplate(redisServer: RedisServer) extends
  RedisClientBase(redisServer) {

  def exists(permits: RedisPermits): Future[Boolean] = {
    redisClient.exists(permits.name)
  }

  def insertPermits(permits: RedisPermits): Future[Boolean] = {
    val json = Json.toJson(permits).toString()
    redisClient.set(permits.name, json)
  }


  def queryPermits(name: String): Future[RedisPermits] = {
    redisClient.get[String](name).flatMap {
      case Some(result) =>
        val jsonValue: JsValue = Json.parse(result)
        val jsonPermits = jsonValue.validate[RedisPermits]
        jsonPermits match {
          case JsSuccess(permits, _) => Future {
            permits
          }
        }
    }
  }


}
