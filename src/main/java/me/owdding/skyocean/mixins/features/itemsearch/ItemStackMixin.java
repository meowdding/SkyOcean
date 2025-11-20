package me.owdding.skyocean.mixins.features.itemsearch;

import me.owdding.skyocean.events.ItemStackCreateEvent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Unique
    private static boolean inEventExecution = false;

    @Inject(method = "<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/core/component/PatchedDataComponentMap;)V", at = @At("RETURN"), order = 1100)
    void init(CallbackInfo ci) {
        if (inEventExecution) {
            return;
        }
        inEventExecution = true;
        new ItemStackCreateEvent((ItemStack) (Object) this).post(SkyBlockAPI.getEventBus());
        inEventExecution = false;
    }

}
