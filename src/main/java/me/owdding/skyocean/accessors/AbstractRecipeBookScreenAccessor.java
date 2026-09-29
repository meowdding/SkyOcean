package me.owdding.skyocean.accessors;

import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.apache.commons.lang3.NotImplementedException;

public interface AbstractRecipeBookScreenAccessor {

    default RecipeBookComponent<?> skyocean$getRecipeBookComponent() {
        throw new NotImplementedException("Implemented via mixins!");
    }
}
