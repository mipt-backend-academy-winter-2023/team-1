package circuitbreaker

import nl.vroste.rezilience.CircuitBreaker.CircuitBreakerCallError
import zio.ZIO

trait ZCircuitBreaker {
  def run[R, E, A](
      effect: ZIO[R, E, A]
  ): ZIO[R with ZCircuitBreaker, CircuitBreakerCallError[E], A]
}

object ZCircuitBreaker {
  def run[R, E, A](
      effect: ZIO[R, E, A]
  ): ZIO[R with ZCircuitBreaker, CircuitBreakerCallError[E], A] =
    ZIO.serviceWithZIO[ZCircuitBreaker](_.run(effect))
}
