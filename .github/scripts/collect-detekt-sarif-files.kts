import kotlin.io.path.*

val versions = listOf("1.21.5", "1.21.8", "1.21.10")

val finalFile = Path("detekt_output.txt")

var createFakeReportFile = false

versions.map {
    Path("versions/$it/build/reports/detekt") to it
}.forEach { (path, version) ->
    println("Scanning $path")
    path.listDirectoryEntries("*.sarif").forEach {
        println("Found sarif file $it")
        val toWrite = mutableListOf<String>()
        Runtime.getRuntime()
            .exec(arrayOf("/bin/bash", *"./.github/scripts/process_detekt_sarif.sh ${it.toAbsolutePath()}".split(" ").toTypedArray()))
            .apply {
                this.waitFor()
                this.inputReader().forEachLine { toWrite.add(it) }
                this.errorReader().forEachLine { println(":: $it") }
            }

        if (toWrite.isNotEmpty()) {
            createFakeReportFile = true
            if (finalFile.notExists()) finalFile.createFile()
            toWrite.forEach(::println)
            finalFile.appendLines(toWrite)
        }
    }
}

if (createFakeReportFile) {
    println("Creating detekt failures file")
    Path("detekt_failures").writeText("test")
}

