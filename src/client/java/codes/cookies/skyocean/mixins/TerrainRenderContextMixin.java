package codes.cookies.skyocean.mixins;

import codes.cookies.skyocean.events.BlockModelEvent;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.TerrainRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI;

@Mixin(TerrainRenderContext.class)
public class TerrainRenderContextMixin {

    @Inject(method = "bufferModel", at = @At("HEAD"))
    private void modifyModel(
        CallbackInfo ci,
        @Local(argsOnly = true) LocalRef<BlockStateModel> model,
        @Local(argsOnly = true) LocalRef<BlockState> blockState,
        @Local(argsOnly = true)BlockPos blockPos
        ) {
        final BlockModelEvent blockModelEvent = new BlockModelEvent(blockState.get(), blockPos);
        blockModelEvent.post(SkyBlockAPI.getEventBus());
        if (blockModelEvent.getState() != blockState.get()) {
            blockState.set(blockModelEvent.getState());
            model.set(Minecraft.getInstance().getBlockRenderer().getBlockModel(blockState.get()));
        }

    }

}
