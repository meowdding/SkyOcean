package codes.cookies.skyocean.mixins;

import codes.cookies.skyocean.events.BlockModelEvent;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI;

@Pseudo
@Mixin(targets = "net/caffeinemc/mods/sodium/client/render/chunk/compile/tasks/ChunkBuilderMeshingTask")
public class Sodium_ChunkBuilderMeshingTask {

    @ModifyArg(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockModelShaper;getBlockModel(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/renderer/block/model/BlockStateModel;"))
    private BlockState execute(BlockState blockState, @Local(name = "blockPos") BlockPos.MutableBlockPos blockPos) {
        final BlockModelEvent blockModelEvent = new BlockModelEvent(blockState, blockPos);
        blockModelEvent.post(SkyBlockAPI.getEventBus());
        return blockModelEvent.getState();
    }

}
