package com.github.dnvriend.logging.application

import com.github.dnvriend.logging.adapters.services.GreetingServiceImpl
import com.github.dnvriend.logging.api.GreetingService
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LagomApplicationContext, LagomServer}
import com.softwaremill.macwire.wire
import com.typesafe.scalalogging.Logger
import play.api.libs.ws.ahc.AhcWSComponents

abstract class LoggingApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  val log: Logger = Logger[LoggingApplication]
  log.debug("Launching...")

    override lazy val lagomServer: LagomServer = LagomServer.forServices(
      bindService[GreetingService].to(wire[GreetingServiceImpl])
    )
  }
