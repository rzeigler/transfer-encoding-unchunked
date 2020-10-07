package rzeigler.teu

import cats.effect.{Concurrent, Timer, ContextShift}
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.headers._
import fs2._
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit
import org.http4s.TransferCoding
import org.http4s.Headers
import org.http4s.Response
import org.http4s.Status

object Routes {

  def slowStream[F[_]: Concurrent: Timer] =
    Stream
      .fixedRate[F](FiniteDuration(500, TimeUnit.MILLISECONDS))
      .map(_ => "..........")
      .intersperse("\n")
      .take(50)
      .through(text.utf8Encode)

  def transferEncodingRoutes[F[_]: Concurrent: Timer: ContextShift]
      : HttpRoutes[F] = {

    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "transferEncoding" =>
        Response[F](
          Status.Ok,
          headers = Headers.of(`Transfer-Encoding`(TransferCoding.chunked))
        )
          .withBodyStream(slowStream)
          .pure[F]
    }
  }

  def bareRoutes[F[_]: Concurrent: Timer: ContextShift]: HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "empty" =>
        Response[F](Status.Ok)
          .withBodyStream(slowStream)
          .pure[F]
    }
  }
}
