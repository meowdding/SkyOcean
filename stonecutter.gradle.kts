plugins {
    id("dev.kikugie.stonecutter")
    id("fabric-loom") version "1.14-SNAPSHOT" apply false
}

stonecutter active "1.21.11"

//region 1.21.11 animal package changes
private val animalReplacementPrefix = "import net.minecraft.world.entity."
private val animalsReplacements = mapOf(
    "monster.skeleton.Bogged" to "monster.Bogged",
    "monster.zombie.Zombie" to "monster.Zombie",
    "animal.feline.Cat" to "animal.Cat",
    "animal.chicken.Chicken" to "animal.Chicken",
    "animal.cow.Cow" to "animal.Cow",
    "animal.fox.Fox" to "animal.Fox",
    "animal.happyghast.HappyGhast" to "animal.HappyGhast",
    "animal.golem.IronGolem" to "animal.IronGolem",
    "animal.equine." to "animal.horse.",
    "animal.cow.MushroomCow" to "animal.MushroomCow",
    "animal.panda.Panda" to "animal.Panda",
    "animal.parrot.Parrot" to "animal.Parrot",
    "animal.pig.Pig" to "animal.Pig",
    "animal.rabbit.Rabbit" to "animal.Rabbit",
    "animal.fish.Salmon" to "animal.Salmon",
    "animal.golem.SnowGolem" to "animal.SnowGolem",
    "animal.fish.TropicalFish" to "animal.TropicalFish",
    "npc.villager.Villager" to "npc.Villager",
    "monster.zombie.ZombieVillager" to "monster.ZombieVillager",
    "animal.golem.CopperGolem" to "animal.coppergolem.CopperGolem",
)
//endregion


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
        replace("import net.minecraft.client.renderer.entity.state.AvatarRenderState(?!;)", "import net.minecraft.client.renderer.entity.state.PlayerRenderState as AvatarRenderState")
        reverse("import net.minecraft.client.renderer.entity.state.PlayerRenderState as AvatarRenderState", "import net.minecraft.client.renderer.entity.state.AvatarRenderState")
    }

    filters.include("**/*.fsh", "**/*.vsh")

    Replacements.read(project).replacements.forEach { (name, replacement) ->
        when (replacement) {
            is StringReplacement if replacement.named -> replacements.string(name) {
                direction = eval(current.version, replacement.condition)
                replace(replacement.from, replacement.to)
            }

            is RegexReplacement if replacement.named -> replacements.regex(name) {
                direction = eval(current.version, replacement.condition)
                replace(replacement.regex, replacement.to)
                reverse(replacement.reverseRegex, replacement.reverse)
            }

            is StringReplacement -> replacements.string {
                direction = eval(current.version, replacement.condition)
                replace(replacement.from, replacement.to)
            }

            is RegexReplacement -> replacements.regex {
                direction = eval(current.version, replacement.condition)
                replace(replacement.regex, replacement.to)
                reverse(replacement.reverseRegex, replacement.reverse)
            }
        }
    }

    animalsReplacements.forEach { new, old ->
        replacements.string {
            direction = eval(current.version, "< 1.21.11")
            replace(animalReplacementPrefix + new, animalReplacementPrefix + old)
        }
    }

}
