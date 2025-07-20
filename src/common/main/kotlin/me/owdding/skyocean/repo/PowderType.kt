package me.owdding.skyocean.repo

import net.minecraft.ChatFormatting
import tech.thatgravyboat.skyblockapi.api.remote.hypixel.HypixelSkillAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import tech.thatgravyboat.skyblockapi.utils.text.Text

enum class PowderType(val formatting: ChatFormatting, val skill: HypixelSkillAPI.Skill) {
    MITHRIL(ChatFormatting.DARK_GREEN, HypixelSkillAPI.Skill.MINING),
    GEMSTONE(ChatFormatting.LIGHT_PURPLE, HypixelSkillAPI.Skill.MINING),
    GLACITE(ChatFormatting.AQUA, HypixelSkillAPI.Skill.MINING),

    WHISPER(ChatFormatting.DARK_AQUA, HypixelSkillAPI.Skill.FORAGING)
    ;

    val displayName by lazy {
        if (this.skill == HypixelSkillAPI.Skill.FORAGING) {
            return@lazy Text.of {
                append("Forest ")
                append(name.toTitleCase())
                this.withStyle(formatting)
            }
        }

        Text.of(name.toTitleCase()) {
            append(" Powder")
            this.withStyle(formatting)
        }
    }
}
