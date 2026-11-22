package me.owdding.skyocean.config.hidden

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

//Holds the sb ids picked in the sack editor
object SackHudStorage : CategoryKt("sack_hud") {
    override val hidden: Boolean = true
    var items by strings()
}
