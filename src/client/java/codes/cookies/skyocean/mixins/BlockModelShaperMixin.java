package codes.cookies.skyocean.mixins;

import codes.cookies.skyocean.events.BlockModelEvent;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI;

import java.util.Map;

@Mixin(BlockModelShaper.class)
public class BlockModelShaperMixin {

    @Shadow
    private Map<BlockState, BlockStateModel> modelByStateCache;
    @Final
    @Shadow
    private ModelManager modelManager;

    @Inject(
        method = "getBlockModel",
        at = @At("HEAD"),
        cancellable = true
    )
    private void getBlockModel(CallbackInfoReturnable<BlockStateModel> cir, @Local(argsOnly = true) BlockState blockState) {
        final BlockModelEvent blockModelEvent = new BlockModelEvent(blockState);
        blockModelEvent.post(SkyBlockAPI.getEventBus());

        if (blockModelEvent.getState() != blockState) {
            cir.setReturnValue(orNotFound(modelByStateCache.get(blockState)));
        }
    }

    @Unique
    private BlockStateModel orNotFound(BlockStateModel model) {
        if (model == null) {
            return modelManager.getMissingBlockStateModel();
        }
        return model;
    }

}
