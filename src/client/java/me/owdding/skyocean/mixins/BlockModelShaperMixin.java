package me.owdding.skyocean.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import me.owdding.skyocean.events.BlockModelEvent;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI;

@Mixin(BlockModelShaper.class)
public class BlockModelShaperMixin {

    @Inject(method = "getBlockModel", at = @At("HEAD"))
    private void getBlockModel(CallbackInfoReturnable<BlockModel> cir, @Local(argsOnly = true) LocalRef<BlockState> blockState) {
        final BlockModelEvent blockModelEvent = new BlockModelEvent(blockState.get());
        blockModelEvent.post(SkyBlockAPI.getEventBus());
        final BlockState state = blockModelEvent.getState();
        if (state != blockState.get()) {
            blockState.set(state);
        }
    }

}
