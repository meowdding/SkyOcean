package me.owdding.skyocean.mixins.features.customize;

import me.owdding.skyocean.features.item.custom.CustomItemsHelper;
import me.owdding.skyocean.utils.Utils;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.SkullBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("RETURN"))
    public <T extends LivingEntity, S extends LivingEntityRenderState> void replaceSkull(T entity, S state, float ticks, CallbackInfo ci) {
        var item = entity.getItemBySlot(EquipmentSlot.HEAD);
        state.wornHeadProfile = Utils.nonNullElse(CustomItemsHelper.getData(item, DataComponents.PROFILE), state.wornHeadProfile);
        if (state.wornHeadProfile != null && state.wornHeadType == null) {
            state.wornHeadType = SkullBlock.Types.PLAYER;
        }
    }

}
