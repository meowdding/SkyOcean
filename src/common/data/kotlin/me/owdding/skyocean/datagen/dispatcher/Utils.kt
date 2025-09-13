package me.owdding.skyocean.datagen.dispatcher

import java.io.ByteArrayOutputStream
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.isRegularFile
import kotlin.io.path.readBytes

object Utils {

    fun Sequence<Path>.zip(root: Path): ByteArray {

        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use {
            this.forEach { file ->
                val relative = root.relativize(file).toString() + ("/".takeUnless { file.isRegularFile() } ?: "")
                val entry = ZipEntry(relative)
                it.putNextEntry(entry)
                if (!entry.isDirectory) {
                    it.write(file.readBytes())
                }
                it.closeEntry()
            }
        }

        return output.toByteArray()
    }

    fun zipFile(path: Path, init: FileSystem.(Path) -> Unit) {
        FileSystems.newFileSystem(path, mutableMapOf("create" to "true")).use { system ->
            system.init(system.getPath("/"))
        }
    }

}
