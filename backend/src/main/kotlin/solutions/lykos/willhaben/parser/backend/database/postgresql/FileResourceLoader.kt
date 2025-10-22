package solutions.lykos.willhaben.parser.backend.database.postgresql

import java.io.File
import java.net.URL
import java.util.zip.ZipFile

object FileResourceLoader {
    fun listResources(url: URL): List<URL> {
        val externalForm = url.toExternalForm()
        return when {
            externalForm.startsWith("jar:file:") -> {
                var (zipFile, dir) = externalForm.substring(9).split('!')
                dir = dir.removePrefix("/") + "/"
                ZipFile(zipFile).use { file ->
                    file
                        .entries()
                        .asSequence()
                        .mapNotNull { entry ->
                            if (entry.isDirectory) {
                                null
                            } else {
                                val name = entry.name
                                val suffix = name.removePrefix(dir)
                                if (name != suffix && !suffix.contains('/')) {
                                    URL("jar:file:$zipFile!/$name")
                                } else {
                                    null
                                }
                            }
                        }.toList()
                }
            }

            externalForm.startsWith("file:") -> {
                val path = externalForm.substring(5)
                val files = File(path).listFiles { dir, name -> !File(dir, name).isDirectory } ?: emptyArray<File>()
                files.map { URL("file:$it") }
            }

            else -> {
                throw IllegalArgumentException("Unsupported protocol: ${url.protocol}")
            }
        }
    }
}
