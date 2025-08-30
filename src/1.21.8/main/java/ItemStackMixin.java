import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.owdding.skyocean.accessors.customize.ItemStackAccessor;
import me.owdding.skyocean.features.item.custom.CustomItems;
import me.owdding.skyocean.features.item.custom.CustomItemsHelper;
import me.owdding.skyocean.features.item.custom.data.ItemKey;
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
    private ItemKey key;

    @Inject(method = "<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/core/component/PatchedDataComponentMap;)V", at = @At("RETURN"), order = 1200)
    public void init(ItemLike itemLike, int count, PatchedDataComponentMap patches, CallbackInfo ci) {
        key = CustomItems.INSTANCE.getKey(self());
    }

    @ModifyReturnValue(method = "getStyledHoverName", at = @At("RETURN"))
    public Component getStylizedHoverName(Component original) {
        return Objects.requireNonNullElse(CustomItemsHelper.getNameReplacement(self()), original);
    }

    @Unique
    public ItemStack self() {
        return (ItemStack) (Object) this;
    }

    @Override
    public @Nullable ItemKey skyocean$getItemKey() {
        return key;
    }
}
