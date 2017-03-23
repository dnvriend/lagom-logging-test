package com.github.dnvriend.logging.impl

import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import com.github.dnvriend.lagomloggingtest.api._
import com.github.dnvriend.logging.api.{GreetingMessage, GreetingService}
import com.github.dnvriend.logging.application.LoggingApplication

class GreetingServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra(true)
  ) { ctx =>
    new LoggingApplication(ctx) with LocalServiceLocator
  }

  val client = server.serviceClient.implement[GreetingService]

  override protected def afterAll() = server.stop()

  "lagom-logging-test service" should {

    "say hello" in {
      client.hello("Alice").invoke().map { answer =>
        answer should ===("Hello, Alice!")
      }
    }

    "allow responding with a custom message" in {
      for {
        _ <- client.useGreeting("Bob").invoke(GreetingMessage("Hi"))
        answer <- client.hello("Bob").invoke()
      } yield {
        answer should ===("Hi, Bob!")
      }
    }
  }
}
