package solutions.lykos.willhaben.parser.backend.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory

class MarkerLogger(
    private val mark: String,
    private val logger: Logger = LoggerFactory.getLogger(mark)
) : Logger by logger {
    private val marker = MarkerFactory.getMarker(mark)

    override fun trace(msg: String?) = logger.trace(marker, msg)

    override fun trace(
        format: String?,
        arg: Any?
    ) {
        logger.trace(marker, format, arg)
    }

    override fun trace(
        format: String?,
        arg1: Any?,
        arg2: Any?
    ) {
        logger.trace(marker, format, arg1, arg2)
    }

    override fun trace(
        format: String?,
        vararg arguments: Any?
    ) {
        logger.trace(marker, format, arguments)
    }

    override fun trace(
        msg: String?,
        t: Throwable?
    ) {
        logger.trace(marker, msg, t)
    }

    override fun debug(msg: String?) {
        logger.debug(marker, msg)
    }

    override fun debug(
        format: String?,
        arg: Any?
    ) {
        logger.debug(marker, format, arg)
    }

    override fun debug(
        format: String?,
        arg1: Any?,
        arg2: Any?
    ) {
        logger.debug(marker, format, arg1, arg2)
    }

    override fun debug(
        format: String?,
        vararg arguments: Any?
    ) {
        logger.debug(marker, format, arguments)
    }

    override fun debug(
        msg: String?,
        t: Throwable?
    ) {
        logger.debug(marker, msg, t)
    }

    override fun info(msg: String?) {
        logger.info(marker, msg)
    }

    override fun info(
        format: String?,
        arg: Any?
    ) {
        logger.info(marker, format, arg)
    }

    override fun info(
        format: String?,
        arg1: Any?,
        arg2: Any?
    ) {
        logger.info(marker, format, arg1, arg2)
    }

    override fun info(
        format: String?,
        vararg arguments: Any?
    ) {
        logger.info(marker, format, arguments)
    }

    override fun info(
        format: String?,
        t: Throwable?
    ) {
        logger.info(marker, format, t)
    }

    override fun warn(msg: String?) {
        logger.warn(marker, msg)
    }

    override fun warn(
        format: String?,
        arg: Any?
    ) {
        logger.warn(marker, format, arg)
    }

    override fun warn(
        format: String?,
        vararg arguments: Any?
    ) {
        logger.warn(marker, format, arguments)
    }

    override fun warn(
        format: String?,
        arg1: Any?,
        arg2: Any?
    ) {
        logger.warn(marker, format, arg1, arg2)
    }

    override fun warn(
        msg: String?,
        t: Throwable?
    ) {
        logger.warn(marker, msg, t)
    }

    override fun error(msg: String?) {
        logger.error(marker, msg)
    }

    override fun error(
        format: String?,
        arg: Any?
    ) {
        logger.error(marker, format, arg)
    }

    override fun error(
        format: String?,
        arg1: Any?,
        arg2: Any?
    ) {
        logger.error(marker, format, arg1, arg2)
    }

    override fun error(
        format: String?,
        vararg arguments: Any?
    ) {
        logger.error(marker, format, arguments)
    }

    override fun error(
        msg: String?,
        t: Throwable?
    ) {
        logger.error(marker, msg, t)
    }
}
