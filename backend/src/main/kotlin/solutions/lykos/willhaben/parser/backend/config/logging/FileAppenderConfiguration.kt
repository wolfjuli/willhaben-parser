package solutions.lykos.willhaben.parser.backend.config.logging

class FileAppenderConfiguration(
    var currentLogFilename: String = "logfile.log",
    var archivedLogFilenamePattern: String = "logfile-%d.log",
    var archivedFileCount: Int = 7,
    var totalSizeCap: String = "3GB"
) : BaseAppenderConfiguration()
