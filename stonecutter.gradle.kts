import dev.kikugie.stonecutter.build.config.ReplacementContainer

plugins {
    id("dev.kikugie.stonecutter")
    id("net.fabricmc.fabric-loom-remap") apply false
    id("net.fabricmc.fabric-loom") apply false
}

stonecutter active "26.3"

stonecutter parameters {

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



    fun ReplacementContainer.StringReplacementSpec.rename(
        from: String,
        to: String,
        prefix: String? = null
    ) {
        val prefix = prefix?.plus(".") ?: ""
        replace("import $prefix$from;", "import $prefix$to;")
        replace("import $prefix$from as ${to.substringAfterLast('.')}", "import $prefix$to")
    }
    fun ReplacementContainer.StringReplacementSpec.move(
        from: String,
        to: String,
        prefix: String? = null
    ) {
        val prefix = prefix?.plus(".") ?: ""
        replace("$prefix$from".replace('/', '.'), "$prefix$to".replace('/', '.'))
        replace("$prefix$from".replace('.', '/'), "$prefix$to".replace('.', '/'))
    }

    class PackageMover(val fromPackage: String, val toPackage: String) {
        fun ReplacementContainer.StringReplacementSpec.moveRelative(vararg names: String) {
            names.forEach { name ->
                move("$fromPackage.$name", "$toPackage.$name")
            }
        }
        fun ReplacementContainer.StringReplacementSpec.move(vararg relatives: Pair<String, String>) {
            relatives.forEach { (old, new) ->
                move("$fromPackage.$old", "$toPackage.$new")
            }
        }
        fun ReplacementContainer.StringReplacementSpec.moveAndRename(vararg relatives: Pair<String, String>) {
            relatives.forEach { (old, new) ->
                rename("$fromPackage.$old", "$toPackage.$new")
            }
        }
    }

    fun movePackage(
        from: String,
        to: String,
        callback: PackageMover.() -> Unit
    ) = PackageMover(from, to).callback()

    val minecraft = "net.minecraft"

    replacements {
        string(current.parsed > "26.2") {

            rename("EnderMan", "Enderman", "$minecraft.world.entity.monster")
            rename("DynamicUniformStorage", "DynamicGpuDataStorage", "$minecraft.client.renderer")
            move("me.owdding.lib.platform.screens.*", "net.minecraft.client.input.*")

            move("me.owdding.lib.platform.screens.BaseParentWidget", "earth.terrarium.olympus.client.components.base.BaseParentWidget")

            movePackage("com.mojang.blaze3d", "com.mojang.renderpearl.api") {
                moveRelative(
                    "GpuFormat",
                    "buffers.GpuBuffer",
                    "buffers.GpuBufferSlice",
                    "pipeline.BindGroupLayout",
                    "pipeline.BlendFunction",
                    "pipeline.ColorTargetState",
                    "pipeline.DepthStencilState",
                    "pipeline.RenderPipeline",
                    "textures.FilterMode",
                    "textures.GpuTexture",
                    "textures.GpuTextureView",
                    "vertex.VertexFormat",
                )
                move(
                    "systems.RenderPass" to "commands.RenderPass",
                    "systems.GpuDevice" to "device.GpuDevice",
                    "platform.CompareOp" to "pipeline.CompareOp",
                    "IndexType" to "pipeline.IndexType",
                    "PrimitiveTopology" to "pipeline.PrimitiveTopology",
                    "shaders.UniformType" to "pipeline.UniformType",
                )
            }

            rename("PipelineRenderer", "PipelineSubmit", "earth.terrarium.olympus.client.pipelines.renderer")
        }
    }
}
