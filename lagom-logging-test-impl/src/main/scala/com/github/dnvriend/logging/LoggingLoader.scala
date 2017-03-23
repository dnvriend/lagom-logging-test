package com.github.dnvriend.logging

import com.github.dnvriend.logging.api.GreetingService
import com.github.dnvriend.logging.application.LoggingApplication
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server._
import org.slf4j.{Logger, LoggerFactory}
import play.api.LoggerConfigurator

class LoggingLoader extends LagomApplicationLoader {
  val log: Logger = LoggerFactory.getLogger(getClass)
  log.debug("Loading...")

  override def load(context: LagomApplicationContext): LagomApplication =
    new LoggingApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication = {
    val environment = context.playContext.environment
    LoggerConfigurator(environment.classLoader)
      .foreach((loggerConfigurator: LoggerConfigurator) => loggerConfigurator.configure(environment))
    new LoggingApplication(context) with LagomDevModeComponents
  }

  override def describeServices = List(
    readDescriptor[GreetingService]
  )
}


