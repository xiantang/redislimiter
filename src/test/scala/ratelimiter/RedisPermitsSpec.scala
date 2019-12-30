package ratelimiter

import org.scalatest._
import play.api.libs.json.{JsResult, JsValue, Json}

/**
 * @author zhujingdi
 * @since 2019/12/30
 */
class RedisPermitsSpec extends FlatSpec {
  "RedisPermits" should "use writes converters" in {
    val str = Json.toJson(RedisPermits("test", 4, 6, 500, 0)).toString()
    val except = "{\"name\":\"test\",\"maxPermits\":4,\"storePermits\":6,\"intervalMillis\":500,\"nextFreeTicketMillis\":0}"
    assert(except == str)
  }

  "RedisPermits" should "use reads converters" in {
    val raw = "{\"name\":\"test\",\"maxPermits\":4,\"storePermits\":6,\"intervalMillis\":500,\"nextFreeTicketMillis\":0}"
    val json: JsValue = Json.parse(raw)
    val placeResult: JsResult[RedisPermits] = json.validate[RedisPermits]
    val value = placeResult.get
    val except = RedisPermits("test", 4, 6, 500, 0)
    assert(except == value)
  }

}
