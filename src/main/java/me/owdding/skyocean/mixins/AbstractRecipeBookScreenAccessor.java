package me.owdding.skyocean.mixins;

import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractRecipeBookScreen.class)
public interface AbstractRecipeBookScreenAccessor {

    @Accessor("recipeBookComponent")
    default RecipeBookComponent<?> skyocean$getRecipeBookComponent() {
        throw new NotImplementedException("Implemented via mixins!");
    }
}
