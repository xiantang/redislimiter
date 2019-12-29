package lock

/**
 * @author zhujingdi
 * @since 2019/12/29
 */
case class  Lock(key: String, value: String)
object Lock {
  val empty: Lock = Lock("", "")
}