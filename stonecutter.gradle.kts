plugins {
    id("dev.kikugie.stonecutter")
    id("net.fabricmc.fabric-loom-remap") apply false
    id("net.fabricmc.fabric-loom") apply false
}

stonecutter active "26.1"

stonecutter parameters {
    // Used for temporarily removing classes from the latest version.
    constants["TODO"] = current.version != "1.21.11"

    swaps["mod_version"] = "\"" + property("version") + "\";"
    swaps["minecraft"] = "\"" + node.metadata.version + "\";"
    replacements.string {
        direction = eval(current.version, "> 1.21.5")
        replace("// moj_import <", "//!moj_import <")
    }
    replacements.regex {
        direction = eval(current.version, "< 1.21.9")
        replace(
            "import net.minecraft.client.renderer.entity.state.AvatarRenderState(?!;)",
            "import net.minecraft.client.renderer.entity.state.PlayerRenderState as AvatarRenderState",
            "import net.minecraft.client.renderer.entity.state.PlayerRenderState as AvatarRenderState",
            "import net.minecraft.client.renderer.entity.state.AvatarRenderState"
        )
    }

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

}
