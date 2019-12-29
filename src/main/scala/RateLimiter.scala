import java.util.concurrent.TimeUnit

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * @author zhujingdi
 * @since 2019/12/29
 */
class RateLimiter() {

  def acquire(permits: Int):Future[Double] ={
    val microsToWait = reserve(permits)
    Future{
      TimeUnit.MICROSECONDS.sleep(microsToWait)
      microsToWait
    }
  }

  def reserve(permits: Int) :Long = {
    1000
  }


}
object RateLimiter{

}
