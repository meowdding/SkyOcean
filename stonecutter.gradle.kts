plugins {
    id("dev.kikugie.stonecutter")
    id("fabric-loom") version "1.11-SNAPSHOT" apply false
}

stonecutter active "1.21.10"

stonecutter parameters {
    swaps["mod_version"] = "\"" + property("version") + "\";"
    swaps["minecraft"] = "\"" + node.metadata.version + "\";"
    replacements.string {
        direction = eval(current.version, "> 1.21.5")
        replace("// moj_import <", "//!moj_import <")
    }
    replacements.regex {
        direction = eval(current.version, "< 1.21.9")
        replace("import net.minecraft.client.renderer.entity.state.AvatarRenderState(?!;)", "import net.minecraft.client.renderer.entity.state.PlayerRenderState as AvatarRenderState")
        reverse("import net.minecraft.client.renderer.entity.state.PlayerRenderState as AvatarRenderState", "import net.minecraft.client.renderer.entity.state.AvatarRenderState")
    }

    filters.include("**/*.fsh", "**/*.vsh")
}
