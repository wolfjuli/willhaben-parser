package solutions.lykos.willhaben.parser.backend.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.encoder.LayoutWrappingEncoder
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy
import ch.qos.logback.core.util.FileSize
import org.slf4j.LoggerFactory
import solutions.lykos.willhaben.parser.backend.config.logging.ConsoleAppenderConfiguration
import solutions.lykos.willhaben.parser.backend.config.logging.FileAppenderConfiguration
import solutions.lykos.willhaben.parser.backend.config.logging.LoggingConfiguration

object LoggingConfigurator {
    fun configure(config: LoggingConfiguration): Logger {
        val currentLogger = LoggerFactory.getLogger(config.name)
        val loggerContext = (currentLogger as Logger).loggerContext
        val rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME)

        loggerContext.reset()
        rootLogger.detachAndStopAllAppenders()
        rootLogger.level = Level.toLevel(config.level, Level.INFO)

        config.appenders.forEach {
            val layout = PatternLayout()
            layout.context = loggerContext
            layout.pattern = it.logFormat
            layout.start()

            val encoder = LayoutWrappingEncoder<ILoggingEvent>()
            encoder.context = loggerContext
            encoder.layout = layout

            val appender =
                when (it) {
                    is ConsoleAppenderConfiguration -> {
                        ConsoleAppender()
                    }

                    is FileAppenderConfiguration -> {
                        RollingFileAppender<ILoggingEvent>().also { appender ->

                            appender.context = loggerContext
                            appender.file = it.currentLogFilename

                            val policy = TimeBasedRollingPolicy<ILoggingEvent>()
                            policy.context = loggerContext
                            policy.fileNamePattern = it.archivedLogFilenamePattern
                            policy.maxHistory = it.archivedFileCount
                            policy.setParent(appender)
                            policy.setTotalSizeCap(FileSize.valueOf(it.totalSizeCap))

                            appender.rollingPolicy = policy
                            policy.start()
                            appender.isImmediateFlush = true
                        }
                    }

                    else -> throw NoSuchMethodException(it::class.qualifiedName)
                }

            appender.context = loggerContext
            appender.addFilter(MarkerLoggingFilter(it.filters))
            appender.encoder = encoder
            appender.start()
            rootLogger.addAppender(appender)
        }

        return currentLogger
    }
}
