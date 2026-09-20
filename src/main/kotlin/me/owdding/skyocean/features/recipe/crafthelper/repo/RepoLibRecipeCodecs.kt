package me.owdding.skyocean.features.recipe.crafthelper.repo

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.RecordBuilder
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.owdding.ktcodecs.IncludedCodec
import me.owdding.skyocean.features.recipe.Ingredient
import me.owdding.skyocean.features.recipe.IngredientType
import me.owdding.skyocean.features.recipe.SkyOceanItemIngredient
import me.owdding.skyocean.generated.DispatchHelper
import net.minecraft.util.ExtraCodecs
import tech.thatgravyboat.repolib.api.recipes.CraftingRecipe
import tech.thatgravyboat.repolib.api.recipes.ForgeRecipe
import tech.thatgravyboat.repolib.api.recipes.KatRecipe
import tech.thatgravyboat.repolib.api.recipes.Recipe
import tech.thatgravyboat.repolib.api.recipes.ShopRecipe
import tech.thatgravyboat.repolib.api.recipes.ingredient.AttributeIngredient
import tech.thatgravyboat.repolib.api.recipes.ingredient.CraftingIngredient
import tech.thatgravyboat.repolib.api.recipes.ingredient.EnchantmentIngredient
import tech.thatgravyboat.repolib.api.recipes.ingredient.ItemIngredient
import tech.thatgravyboat.repolib.api.recipes.ingredient.CurrencyIngredient
import tech.thatgravyboat.repolib.api.recipes.ingredient.PetIngredient
import tech.thatgravyboat.repolib.api.recipes.ingredient.PotionIngredient
import tech.thatgravyboat.repolib.api.recipes.ingredient.RuneIngredient
import kotlin.reflect.KClass

object RepoLibRecipeCodecs {

    val ingredients: Codec<CraftingIngredient> = buildMap {
        put(
            "pet",
            RecordCodecBuilder.mapCodec {
                it.group(
                    Codec.STRING.fieldOf("id").forGetter(PetIngredient::id),
                    Codec.STRING.fieldOf("tier").forGetter(PetIngredient::tier),
                    Codec.INT.fieldOf("count").forGetter(PetIngredient::count),
                ).apply(it, ::PetIngredient)
            },
        )
        put(
            "item",
            RecordCodecBuilder.mapCodec {
                it.group(
                    Codec.STRING.fieldOf("id").forGetter(ItemIngredient::id),
                    Codec.INT.fieldOf("count").forGetter(ItemIngredient::count),
                ).apply(it, ::ItemIngredient)
            },
        )
        put(
            "enchantment",
            RecordCodecBuilder.mapCodec {
                it.group(
                    Codec.STRING.fieldOf("id").forGetter(EnchantmentIngredient::id),
                    Codec.INT.fieldOf("level").forGetter(EnchantmentIngredient::level),
                    Codec.INT.fieldOf("count").forGetter(EnchantmentIngredient::count),
                ).apply(it, ::EnchantmentIngredient)
            },
        )
        put(
            "rune",
            RecordCodecBuilder.mapCodec {
                it.group(
                    Codec.STRING.fieldOf("id").forGetter(RuneIngredient::id),
                    Codec.INT.fieldOf("tier").forGetter(RuneIngredient::tier),
                    Codec.INT.fieldOf("count").forGetter(RuneIngredient::count),
                ).apply(it, ::RuneIngredient)
            },
        )
        put(
            "attribute",
            RecordCodecBuilder.mapCodec {
                it.group(
                    Codec.STRING.fieldOf("id").forGetter(AttributeIngredient::id),
                    Codec.INT.fieldOf("count").forGetter(AttributeIngredient::count),
                ).apply(it, ::AttributeIngredient)
            },
        )
        put(
            "currency",
            RecordCodecBuilder.mapCodec {
                it.group(
                    Codec.STRING.fieldOf("currency").forGetter(CurrencyIngredient::currency),
                    Codec.INT.fieldOf("count").forGetter(CurrencyIngredient::count),
                ).apply(it, ::CurrencyIngredient)
            },
        )
        put(
            "potion",
            RecordCodecBuilder.mapCodec {
                it.group(
                    Codec.STRING.fieldOf("id").forGetter(PotionIngredient::id),
                    Codec.INT.fieldOf("level").forGetter(PotionIngredient::level),
                    Codec.INT.fieldOf("count").forGetter(PotionIngredient::count),
                ).apply(it, ::PotionIngredient)
            },
        )
        put(
            "unknown",
            RecordCodecBuilder.mapCodec {
                it.group(
                    Codec.STRING.fieldOf("id").forGetter(PotionIngredient::id),
                    Codec.INT.fieldOf("level").forGetter(PotionIngredient::level),
                    Codec.INT.fieldOf("count").forGetter(PotionIngredient::count),
                ).apply(it, ::PotionIngredient)
            },
        )
    }.let { map ->
        val mapper = ExtraCodecs.LateBoundIdMapper<String, MapCodec<out CraftingIngredient>>()
        map.forEach(mapper::put)

        mapper.codec(Codec.STRING).dispatch({ map[it.type()]!! } ) { it }
    }

    @IncludedCodec
    val codec: MapCodec<Recipe<*>> = buildMap {
        put(
            Recipe.Type.CRAFTING,
            RecordCodecBuilder.mapCodec {
                it.group(
                    ingredients.listOf().fieldOf("inputs").forGetter(CraftingRecipe::inputs),
                    ingredients.fieldOf("result").forGetter(CraftingRecipe::result)
                ).apply(it, ::CraftingRecipe)
            }
        )
        put(
            Recipe.Type.FORGE,
            RecordCodecBuilder.mapCodec {
                it.group(
                    ingredients.listOf().fieldOf("inputs").forGetter(ForgeRecipe::inputs),
                    Codec.INT.fieldOf("coins").forGetter(ForgeRecipe::coins),
                    Codec.INT.fieldOf("time").forGetter(ForgeRecipe::time),
                    ingredients.fieldOf("result").forGetter(ForgeRecipe::result)
                ).apply(it, ::ForgeRecipe)
            }
        )
        put(
            Recipe.Type.KAT,
            RecordCodecBuilder.mapCodec {
                it.group(
                    ingredients.fieldOf("input").forGetter(KatRecipe::input),
                    ingredients.listOf().fieldOf("items").forGetter(KatRecipe::items),
                    Codec.INT.fieldOf("coins").forGetter(KatRecipe::coins),
                    Codec.INT.fieldOf("time").forGetter(KatRecipe::time),
                    ingredients.fieldOf("result").forGetter(KatRecipe::output)
                ).apply(it, ::KatRecipe)
            }
        )
        put(
            Recipe.Type.SHOP,
            RecordCodecBuilder.mapCodec {
                it.group(
                    Codec.STRING.fieldOf("npc").forGetter(ShopRecipe::npc),
                    ingredients.listOf().fieldOf("items").forGetter(ShopRecipe::inputs),
                    ingredients.fieldOf("result").forGetter(ShopRecipe::result)
                ).apply(it, ::ShopRecipe)
            }
        )
    }.let { map ->
        val mapper = ExtraCodecs.LateBoundIdMapper<Recipe.Type<*>, MapCodec<out Recipe<*>>>()
        map.forEach(mapper::put)

        val types: HashBiMap<String, Recipe.Type<*>> = HashBiMap.create(listOf(Recipe.Type.CRAFTING, Recipe.Type.KAT,Recipe.Type.FORGE,Recipe.Type.SHOP).associateBy { it.type })

        mapper.codec(Codec.STRING.xmap(types::getValue, types.inverse()::get)).dispatchMap({ map[it.type()]!! } ) { it }
    }

}
