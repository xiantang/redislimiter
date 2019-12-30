package ratelimiter

import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json.{JsPath, JsValue, Json, Reads, Writes}

import play.api.libs.functional.syntax._
/**
 * @author zhujingdi
 * @since 2019/12/29
 */
case class RedisPermits(
                    name:String,
                    maxPermits: Long,
                    storePermits: Long,
                    intervalMillis: Long,
                    nextFreeTicketMillis: Long
                  ) {


}
object RedisPermits extends LazyLogging {
  implicit val write = new Writes[RedisPermits] {
    override def writes(o: RedisPermits): JsValue = Json.obj(
      "name" -> o.name,
      "maxPermits" -> o.maxPermits,
      "storePermits" -> o.storePermits,
      "intervalMillis" -> o.intervalMillis,
      "nextFreeTicketMillis" -> o.nextFreeTicketMillis
    )
  }

  implicit val locationReads: Reads[RedisPermits] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "maxPermits").read[Long] and
      (JsPath \ "storePermits").read[Long] and
      (JsPath \ "intervalMillis").read[Long] and
      (JsPath \ "nextFreeTicketMillis").read[Long]
    )(RedisPermits.apply _)
}