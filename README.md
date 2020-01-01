# Quickstart

invoke the api 7 times per second.
```scala
val limiter = RateLimiterFactory.newRateLimiter("rest-limiter", permitsPerSecond = 7,maxPermits = 1)
limiter.acquire(1).flatMap{
  doReqeust()
}
```
