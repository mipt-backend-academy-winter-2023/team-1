package circuitbreaker

import nl.vroste.rezilience.CircuitBreaker.CircuitBreakerCallError
import zio.ZIO

trait MyCircuitBreaker {
  def run[R, E, A](
      effect: ZIO[R, E, A]
  ): ZIO[R with MyCircuitBreaker, CircuitBreakerCallError[E], A]
}

object MyCircuitBreaker extends MyCircuitBreaker {
  override def run[R, E, A](
      effect: ZIO[R, E, A]
  ): ZIO[R with MyCircuitBreaker, CircuitBreakerCallError[E], A] =
    ZIO.serviceWithZIO[MyCircuitBreaker](_.run(effect))
}
