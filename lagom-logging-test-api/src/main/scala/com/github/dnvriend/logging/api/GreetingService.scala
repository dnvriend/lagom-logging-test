package com.github.dnvriend.logging.api

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json.{Format, Json}

object GreetingService {
  final val Name = "GreetingService"
}
trait GreetingService extends Service {

  def hello(name: String): ServiceCall[NotUsed, String]

  override final def descriptor = {
    import Service._
    // @formatter:off
    named(GreetingService.Name).withCalls(
      pathCall("/api/hello/:name", hello _)
    ).withAutoAcl(true)
    // @formatter:on
  }
}

case class GreetingMessage(message: String)

object GreetingMessage {
  implicit val format: Format[GreetingMessage] = Json.format[GreetingMessage]
}
