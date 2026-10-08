package me.owdding.skyocean.mixins;

import me.owdding.skyocean.accessors.AbstractRecipeBookScreenAccessor;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractRecipeBookScreen.class)
public class AbstractRecipeBookScreenMixin implements AbstractRecipeBookScreenAccessor {
    @Shadow
    @Final
    private RecipeBookComponent<?> recipeBookComponent;

    @Override
    public RecipeBookComponent<?> skyocean$getRecipeBookComponent() {
        return this.recipeBookComponent;
    }
}
