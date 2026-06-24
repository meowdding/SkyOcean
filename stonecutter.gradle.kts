plugins {
    id("dev.kikugie.stonecutter")
    id("net.fabricmc.fabric-loom-remap") apply false
    id("net.fabricmc.fabric-loom") apply false
}

stonecutter active "26.2"

stonecutter parameters {
    // Used for temporarily removing classes from the latest version.
    constants["TODO"] = current.version != "1.21.11"

    swaps["mod_version"] = "\"" + property("version") + "\";"
    swaps["minecraft"] = "\"" + node.metadata.version + "\";"

    filters.include("**/*.fsh", "**/*.vsh")

    Replacements.read(project).replacements.forEach { (name, replacement) ->
        when (replacement) {
            is StringReplacement -> replacements.string {
                if (replacement.named) {
                    id = name
                }
                direction = eval(current.version, replacement.condition)
                replace(replacement.from, replacement.to)
            }

            is RegexReplacement -> replacements.regex {
                if (replacement.named) {
                    id = name
                }
                direction = eval(current.version, replacement.condition)
                replace(
                    replacement.regex to replacement.to,
                    replacement.reverseRegex to replacement.reverse
                )
            }
        }
    }


    // Taken from Luna :3
    replacements.string {
        direction = eval(current.version, "<= 26.1")

        val dyeColors = mapOf(
            "black" to "BLACK",
            "blue" to "BLUE",
            "brown" to "BROWN",
            "cyan" to "CYAN",
            "gray" to "GRAY",
            "green" to "GREEN",
            "lightBlue" to "LIGHT_BLUE",
            "lightGray" to "LIGHT_GRAY",
            "lime" to "LIME",
            "magenta" to "MAGENTA",
            "orange" to "ORANGE",
            "pink" to "PINK",
            "purple" to "PURPLE",
            "red" to "RED",
            "white" to "WHITE",
            "yellow" to "YELLOW",
        )
        dyeColors.forEach { (lower, upper) ->
            replace("DYE.$lower()", "${upper}_DYE")
            replace("WOOL.$lower()", "${upper}_WOOL")
            replace("CARPET.$lower()", "${upper}_CARPET")
            replace("STAINED_GLASS.$lower()", "${upper}_STAINED_GLASS")
            replace("STAINED_GLASS_PANE.$lower()", "${upper}_STAINED_GLASS_PANE")
            replace("DYED_TERRACOTTA.$lower()", "${upper}_TERRACOTTA")
            replace("HARNESS.$lower()", "${upper}_HARNESS")
        }
    }
}
