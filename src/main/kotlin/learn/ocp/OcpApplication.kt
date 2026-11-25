package learn.ocp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.nio.file.Files
import java.nio.file.Path

@SpringBootApplication
class OcpApplication

fun main(args: Array<String>) {
    // SQLite does not create missing parent directories.
    Files.createDirectories(Path.of(System.getProperty("ocp.content-dir") ?: ".", "data"))
    runApplication<OcpApplication>(*args)
}
