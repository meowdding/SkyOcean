package codes.cookies.skyocean.mixins;

import codes.cookies.skyocean.events.BlockModelEvent;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI;

@Mixin(SectionCompiler.class)
public class SectionCompilerMixin {

    @ModifyArg(method = "compile", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;getBlockModel(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/renderer/block/model/BlockStateModel;"))
    public BlockState execute(BlockState blockState, @Local(ordinal = 2) BlockPos blockPos) {
        final BlockModelEvent blockModelEvent = new BlockModelEvent(blockState, blockPos);
        blockModelEvent.post(SkyBlockAPI.getEventBus());
        return blockModelEvent.getState();
    }

}
