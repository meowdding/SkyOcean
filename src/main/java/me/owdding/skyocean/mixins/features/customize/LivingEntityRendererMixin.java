package me.owdding.skyocean.mixins.features.customize;

import me.owdding.skyocean.features.item.custom.CustomItemsHelper;
import me.owdding.skyocean.features.item.custom.data.CustomItemDataComponents;
import me.owdding.skyocean.utils.Utils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractSkullBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(HumanoidMobRenderer.class)
public abstract class LivingEntityRendererMixin<T extends Mob, S extends HumanoidRenderState, M extends HumanoidModel<S>> extends AgeableMobRenderer<T, S, M> {
    public LivingEntityRendererMixin(
        EntityRendererProvider.Context context,
        M adultModel,
        M babyModel,
        float scale
    ) {
        super(context, adultModel, babyModel, scale);
    }

    @Inject(method = "extractHumanoidRenderState", at = @At("RETURN"))
    private static void replaceSkull(LivingEntity entity, HumanoidRenderState state, float partialTick, ItemModelResolver itemModelResolver, CallbackInfo ci) {
        var item = entity.getItemBySlot(EquipmentSlot.HEAD);
        state.wornHeadProfile = Utils.nonNullElse(CustomItemsHelper.getData(item, DataComponents.PROFILE), state.wornHeadProfile);
        var model = CustomItemsHelper.getCustomData(item, CustomItemDataComponents.model());
        var itemModel = model == null ? item.getItem() : Objects.requireNonNullElse(model.resolveToItem(), item.getItem());
        if (itemModel instanceof BlockItem blockItem && blockItem.getBlock() instanceof AbstractSkullBlock abstractSkullBlock) {
            state.wornHeadType = abstractSkullBlock.getType();
            state.headItem.clear();
            state.headEquipment = ItemStack.EMPTY;
        } else {
            state.headEquipment = item;
            var defaultInstance = itemModel.getDefaultInstance();
            if (!HumanoidArmorLayer.shouldRender(defaultInstance, EquipmentSlot.HEAD)) {
                itemModelResolver.updateForLiving(state.headItem, defaultInstance, ItemDisplayContext.HEAD, entity);
            } else {
                state.headItem.clear();
            }
            state.wornHeadType = null;
            state.wornHeadProfile = null;
        }
    }
}
