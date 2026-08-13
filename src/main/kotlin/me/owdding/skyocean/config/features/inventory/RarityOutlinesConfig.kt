package me.owdding.skyocean.config.features.inventory

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.DelegatingConfig
import me.owdding.skyocean.utils.PreInitModule
import me.owdding.skyocean.utils.extensions.createButton
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import javax.xml.crypto.dsig.keyinfo.KeyName
import kotlin.reflect.KMutableProperty

@PreInitModule
object RarityOutlinesConfig : DelegatingConfig(InventoryConfig) {

    override val translationBase: String = "skyocean.config.inventory.rarity_outlines"

    init {
        separator("")
    }

    val enabled by boolean(false)
    val sampleAmount by cachedTransform(
        int(8) {
            this.range = 1..16
            this.slider = true
        },
        Float::toInt, Int::toFloat,
    ).wrap()

    val sampleDistance by float(1f) {
        this.range = 0f..2f
        this.slider = true
    }
    val alphaCutoff by float(0.03f) {
        this.range = 0f..1f
        this.slider = true
    }
    val outlineAlpha by float(1f) {
        this.range = 0f..1f
        this.slider = true
    }
    val baseRarityGlint by boolean(false)
    val kernelType by cachedTransform(
        enum(KernelType.SQUARE),
        KernelType.entries::get, KernelType::ordinal,
    ).wrap()

    var common by color(SkyBlockRarity.COMMON.skyBlockColor)
    var uncommon by color(SkyBlockRarity.UNCOMMON.skyBlockColor)
    var rare by color(SkyBlockRarity.RARE.skyBlockColor)
    var epic by color(SkyBlockRarity.EPIC.skyBlockColor)
    var legendary by color(SkyBlockRarity.LEGENDARY.skyBlockColor)
    var mythic by color(SkyBlockRarity.MYTHIC.skyBlockColor)
    var divine by color(SkyBlockRarity.DIVINE.skyBlockColor)
    var ultimate by color(SkyBlockRarity.ULTIMATE.skyBlockColor)
    var special by color(SkyBlockRarity.SPECIAL.skyBlockColor)
    var verySpecial by color(SkyBlockRarity.VERY_SPECIAL.skyBlockColor)
    var admin by color(SkyBlockRarity.ADMIN.skyBlockColor)

    fun swapToColors(collector: SkyBlockRarity.() -> Int) {
        SkyBlockRarity.entries.forEach {
            colorProperty(it)?.setter?.call(it.collector())
        }
    }

    init {

        button("skyblock_colors") {
            onClick { swapToColors(SkyBlockRarity::skyBlockColor) }
        }
        button("legacy_colors") {
            onClick { swapToColors(SkyBlockRarity::color) }
        }
    }

    fun colorProperty(skyBlockRarity: SkyBlockRarity?): KMutableProperty<Int>? = when (skyBlockRarity) {
        SkyBlockRarity.COMMON -> ::common
        SkyBlockRarity.UNCOMMON -> ::uncommon
        SkyBlockRarity.RARE -> ::rare
        SkyBlockRarity.EPIC -> ::epic
        SkyBlockRarity.LEGENDARY -> ::legendary
        SkyBlockRarity.MYTHIC -> ::mythic
        SkyBlockRarity.DIVINE -> ::divine
        SkyBlockRarity.ULTIMATE -> ::ultimate
        SkyBlockRarity.SPECIAL -> ::special
        SkyBlockRarity.VERY_SPECIAL -> ::verySpecial
        SkyBlockRarity.ADMIN -> ::admin
        else -> null
    }

    fun color(skyBlockRarity: SkyBlockRarity?): Int? = when (skyBlockRarity) {
        SkyBlockRarity.COMMON -> common
        SkyBlockRarity.UNCOMMON -> uncommon
        SkyBlockRarity.RARE -> rare
        SkyBlockRarity.EPIC -> epic
        SkyBlockRarity.LEGENDARY -> legendary
        SkyBlockRarity.MYTHIC -> mythic
        SkyBlockRarity.DIVINE -> divine
        SkyBlockRarity.ULTIMATE -> ultimate
        SkyBlockRarity.SPECIAL -> special
        SkyBlockRarity.VERY_SPECIAL -> verySpecial
        SkyBlockRarity.ADMIN -> admin
        else -> null
    }

    enum class KernelType {
        SQUARE,
        CIRCLE,
        ;
    }
}
