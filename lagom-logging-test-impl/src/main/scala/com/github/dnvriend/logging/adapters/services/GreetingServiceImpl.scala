package com.github.dnvriend.logging.adapters.services

import com.github.dnvriend.logging.api.GreetingService
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}

class GreetingServiceImpl(implicit ec: ExecutionContext) extends GreetingService with LazyLogging {
  override def hello(name: String) = ServiceCall { _ =>
    logger.debug(s"Hello $name")
    Future.successful(s"Hello '$name'")
  }
}
