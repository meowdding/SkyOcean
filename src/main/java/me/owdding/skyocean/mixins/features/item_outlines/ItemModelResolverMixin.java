package me.owdding.skyocean.mixins.features.item_outlines;

import me.owdding.skyocean.features.item.RarityOutlines;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemModelResolver.class)
public class ItemModelResolverMixin {
    @Inject(
        method = "appendItemLayers", at = @At("HEAD")
    )
    private void attachExtra(
        ItemStackRenderState output,
        ItemStack item,
        ItemDisplayContext displayContext,
        Level level,
        ItemOwner owner,
        int seed,
        CallbackInfo ci) {
        RarityOutlines.attachData(output, item);
    }
}
