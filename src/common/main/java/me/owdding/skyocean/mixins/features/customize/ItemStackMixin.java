package me.owdding.skyocean.mixins.features.customize;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.owdding.skyocean.accessors.customize.ItemStackAccessor;
import me.owdding.skyocean.features.item.custom.CustomItems;
import me.owdding.skyocean.features.item.custom.CustomItemsHelper;
import me.owdding.skyocean.features.item.custom.data.ItemKey;
import me.owdding.skyocean.helpers.MixinHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ItemStack.class)
public class ItemStackMixin implements ItemStackAccessor {
    @Unique
    private final static ThreadLocal<Boolean> COPYING = ThreadLocal.withInitial(() -> false);

    @Unique
    private ItemKey key;

    @Inject(method = "<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/core/component/PatchedDataComponentMap;)V", at = @At("RETURN"), order = 1200)
    public void init(ItemLike itemLike, int count, PatchedDataComponentMap patches, CallbackInfo ci) {
        if (!MixinHelper.isStarted() || COPYING.get()) {
            return;
        }
        key = CustomItems.INSTANCE.createKey(self());
        CustomItems.INSTANCE.loadVanilla(self(), key);
    }

    @ModifyReturnValue(method = "getStyledHoverName", at = @At("RETURN"))
    public Component getStylizedHoverName(Component original) {
        return Objects.requireNonNullElse(CustomItemsHelper.getData(self(), DataComponents.CUSTOM_NAME), original);
    }

    @WrapOperation(method = "copy", at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/core/component/PatchedDataComponentMap;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack copy(ItemLike item, int count, PatchedDataComponentMap patches, Operation<ItemStack> original) {
        COPYING.set(true);
        var other = original.call(item, count, patches);
        ((ItemStackMixin) (Object) other).key = key;
        COPYING.set(false);
        return other;
    }


    @Unique
    private ItemStack self() {
        return (ItemStack) (Object) this;
    }

    @Override
    public @Nullable ItemKey skyocean$getItemKey() {
        return key;
    }
}
