package me.owdding.skyocean.features.recipe.custom

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyocean.features.recipe.Ingredient
import me.owdding.skyocean.features.recipe.IngredientType
import me.owdding.skyocean.features.recipe.ItemLikeIngredient
import me.owdding.skyocean.features.recipe.Recipe
import me.owdding.skyocean.features.recipe.RecipeType
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.utils.text.Text

@GenerateCodec
data class CustomRecipe(
    override var output: ItemLikeIngredient?,
    override val inputs: MutableList<Ingredient>,
) : Recipe {
    override val recipeType: RecipeType = RecipeType.CUSTOM
}

@GenerateCodec
data class CustomRoot(override val amount: Int = 1) : ItemLikeIngredient {
    override val id: SkyBlockId get() = SkyBlockId.EMPTY
    override val type: IngredientType get() = IngredientType.ROOT
    override val skyblockId: String get() = "root_ingredient"
    override val item: ItemStack get() = ItemStack.EMPTY
    override val itemName: Component get() = Text.of("Custom Recipe", 0x555555)

    override fun withAmount(amount: Int): Ingredient = copy(amount = amount)
}
