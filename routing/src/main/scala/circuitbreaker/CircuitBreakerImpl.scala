package circuitbreaker
import nl.vroste.rezilience.{CircuitBreaker, TrippingStrategy}
import nl.vroste.rezilience.CircuitBreaker.{CircuitBreakerCallError, State}
import zio._

class MyCircuitBreakerImpl(circuitBreaker: CircuitBreaker[Any])
    extends MyCircuitBreaker {
  override def run[R, E, A](
      effect: ZIO[R, E, A]
  ): ZIO[R with MyCircuitBreaker, CircuitBreakerCallError[E], A] =
    circuitBreaker(effect)
}

object MyCircuitBreakerImpl {
  val live = ZLayer.fromZIO {
    for {
      cb <- CircuitBreaker.make(
        TrippingStrategy.failureCount(5),
        zio.Schedule.exponential(10.second),
        onStateChange = (s: State) => ZIO.logInfo(s"State change to $s").ignore
      )
    } yield new MyCircuitBreakerImpl(cb)
  }
}
