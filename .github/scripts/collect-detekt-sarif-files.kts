import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.appendLines
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.notExists
import kotlin.io.path.readLines

val versions = listOf("1.21.5", "1.21.8", "1.21.10")

val finalFile = Path("detekt_output.txt")

var createFakeReportFile = false

versions.map {
    Path("versions/$it/build/reports/detekt") to it
}.forEach { (path, version) ->
    path.listDirectoryEntries("*.sarif").forEach {
        val outputFile = it.resolveSibling(it.nameWithoutExtension + "_processed.txt")
        Runtime.getRuntime().exec("./.github/scripts/process_detekt_sarif.sh ${it.toAbsolutePath()} | tee ${outputFile.absolutePathString()}".split(" ").toTypedArray()).waitFor()

        if (outputFile.exists()) {
            createFakeReportFile = true
            if (finalFile.notExists()) finalFile.createFile()
            finalFile.appendLines(outputFile.readLines().filter { line -> line.isNotBlank() })
        }
    }
}

if (createFakeReportFile) {
    Path("detekt_failures").createFile()
}
