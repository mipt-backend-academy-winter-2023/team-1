package circuitbreaker

import nl.vroste.rezilience.CircuitBreaker.{CircuitBreakerCallError, State}
import nl.vroste.rezilience.{CircuitBreaker, TrippingStrategy}
import zio.{ZIO, ZLayer, durationInt}

class ZCircuitBreakerImpl(circuitBreaker: CircuitBreaker[Any])
    extends ZCircuitBreaker {

  override def run[R, E, A](
      effect: ZIO[R, E, A]
  ): ZIO[R with ZCircuitBreaker, CircuitBreakerCallError[E], A] =
    circuitBreaker(effect)
}

object ZCircuitBreakerImpl {
  val live = ZLayer.fromZIO {
    CircuitBreaker
      .make(
        TrippingStrategy.failureCount(5),
        zio.Schedule.exponential(10.second),
        onStateChange = state => ZIO.logInfo(s"State change to $state").ignore
      )
      .map(new ZCircuitBreakerImpl(_))
  }
}
