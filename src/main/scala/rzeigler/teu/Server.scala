package rzeigler.teu

import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import cats.implicits._
import fs2.Stream
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit

object Server {

  def stream[F[_]: ConcurrentEffect](implicit
      T: Timer[F],
      C: ContextShift[F]
  ): Stream[F, Nothing] = {
    val httpApp = (
      Routes.transferEncodingRoutes[F] <+>
        Routes.bareRoutes[F]
    ).orNotFound
    BlazeServerBuilder[F](global)
      .withIdleTimeout(FiniteDuration(5, TimeUnit.SECONDS))
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(httpApp)
      .serve
  }.drain
}
