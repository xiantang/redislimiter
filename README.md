# quickly start

```scala
val limiter = RateLimiterFactory.newRateLimiter("rest-limiter", permitsPerSecond = 7,maxPermits = 1)
limiter.acquire(1).flatMap{
  doReqeust()
}
```