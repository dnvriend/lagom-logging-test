# lagom-logging-test
A small study project on logging with Lagom

## Lagom and slf4j
As described by the [Lagom configuring logging](https://www.lagomframework.com/documentation/1.3.x/scala/SettingsLogger.html)
page, Lagom uses slf4j for logging, backed by Logback as the default logging engine.

## Lagom and default logging configuration
If you don't provide a `logback.xml` or `logback-test.xml` configuration in `src/main/resources`, lagom will use a default
configuration:

```xml
<configuration>
  <conversionRule conversionWord="coloredLevel" converterClass="com.lightbend.lagom.internal.logback.ColoredLevel" />

  <appender name="FILE" class="ch.qos.logback.core.FileAppender">
     <file>${application.home:-.}/logs/application.log</file>
     <encoder>
       <pattern>%date [%level] from %logger in %thread - %message%n%xException</pattern>
     </encoder>
   </appender>

  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%coloredLevel %logger{15} - %message%n%xException{10}</pattern>
    </encoder>
  </appender>

  <logger name="play" level="WARN" />
  <logger name="akka" level="WARN" />
  <logger name="com.lightbend.lagom" level="WARN" />
  <logger name="org.apache.cassandra" level="ERROR" />
  <logger name="com.datastax.driver" level="ERROR" />
  <logger name="com.datastax.driver.core.ControlConnection" level="OFF" />
  <logger name="org.apache.kafka" level="WARN" />

  <root level="INFO">
    <appender-ref ref="STDOUT" />
    <appender-ref ref="FILE" />
  </root>
</configuration>
```

And uses the following configuration for production mode:

```xml
<configuration>
  <conversionRule conversionWord="coloredLevel" converterClass="com.lightbend.lagom.internal.logback.ColoredLevel" />

  <appender name="FILE" class="ch.qos.logback.core.FileAppender">
     <file>${application.home:-.}/logs/application.log</file>
     <encoder>
       <pattern>%date [%level] from %logger in %thread - %message%n%xException</pattern>
     </encoder>
  </appender>

  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%coloredLevel %logger{15} - %message%n%xException{10}</pattern>
    </encoder>
  </appender>

  <appender name="ASYNCFILE" class="ch.qos.logback.classic.AsyncAppender">
    <appender-ref ref="FILE" />
  </appender>

  <appender name="ASYNCSTDOUT" class="ch.qos.logback.classic.AsyncAppender">
    <appender-ref ref="STDOUT" />
  </appender>

  <logger name="play" level="WARN" />
  <logger name="akka" level="WARN" />
  <logger name="com.lightbend.lagom" level="WARN" />
  <logger name="org.apache.cassandra" level="ERROR" />
  <logger name="com.datastax.driver" level="ERROR" />
  <logger name="org.apache.kafka" level="WARN" />

  <root level="WARN">
    <appender-ref ref="ASYNCFILE" />
    <appender-ref ref="ASYNCSTDOUT" />
  </root>
</configuration>
```

## Lagom logging behavior
- Lagom uses a file appender that writes to `logs/application.log`, which means that every sub-project in the lagom project
  will have a logs directory containing an `application.log` file.
- The file logger logs full exception stack traces, while the console logger only logs 10 lines of an exception stack trace.
- Lagom uses ANSI color codes by default in level messages.
- In production, Lagom puts both the console and the file logger behind the logback AsyncAppender.
  For details on the performance implications on this, see [this blog post](https://blog.takipi.com/how-to-instantly-improve-your-java-logging-with-7-logback-tweaks/).

## Custom configuration
For any custom configuration, you need to provide your own Logback configuration file. You can provide a default
logging configuration by creating a `logback.xml` file in the project’s resource folder. Furthermore, for
testing purposes, you can also create a `logback-test.xml` and place it in the `src/test/resources` directory
of your project. When both `logback.xml` and `logback-test.xml` are in the classpath, the `logback.xml` has higher precedence.

## Lagom Issue 534 (Loggin in DevMode)
In DevMode Lagom-Scala doesn't detect a custom logback.xml file. In Lagom-Scala API, we use a custom application loader
that does not configure the logging configurator. In DevMode we should configure it manually. In the `LagomApplicationLoader`
we should configure the LoggerConfigurator so it picks up your custom configuration.

```scala
class HelloLoader extends LagomApplicationLoader {
  // ...
  override def loadDevMode(context: LagomApplicationContext): LagomApplication = {
    val environment = context.playContext.environment
   LoggerConfigurator(environment.classLoader)
     .foreach((loggerConfigurator: LoggerConfigurator) => loggerConfigurator.configure(environment))
    new HelloApplication(context) with LagomDevModeComponents
  }
  // ...
}
```

## LogbackLoggerConfigurator
The 'LoggerConfigurator' that will be configured is the `com.lightbend.lagom.internal.logback.LogbackLoggerConfigurator`
and it will load the custom configuration from the classpath and use it.

## Logger and LoggerFactory
To actually log, you create an instance of an `org.slf4j.Logger` by using the `org.slf4j.LoggerFactory`:

```scala
import org.slf4j.{Logger, LoggerFactory}

val log: Logger = LoggerFactory.getLogger(getClass)
log.debug("Hello World!")
```

## ScalaLogging
[Scala Logging](https://github.com/typesafehub/scala-logging) is a convenient and performant logging library wrapping SLF4J.
It is convenient and performant, because you can simply call log methods, without checking whether the respective log
level is enabled due to Scala macros.

A logger can be created in various ways:

```scala
import com.typesafe.scalalogging.Logger
val logger = Logger("name")
val logger = Logger(classOf[MyClass])
val logger: Logger = Logger[LoggingApplication]

import org.slf4j.LoggerFactory
val logger = Logger(LoggerFactory.getLogger("name"))

import com.typesafe.scalalogging.LazyLogging
class MyClass extends LazyLogging {
  logger.debug("Hi there!")
}
```

## slf4j and noop binding
From slf4j v1.6 and up, when no slf4j binding can be found, instead of throwing an exception and not working, the
slf4j will use the noop binding and not log. To overcome this you should provide a slf4j binding to the classpath
like add `slf4j-simple`.

## sbt and a slf4j binding
There are cases that sbt itself needs a slf4j binding, in that case you can add one by adding the following to
the `project/logging.sbt` file:

```scala
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.24"
```

## Multiple slf4j bindings
