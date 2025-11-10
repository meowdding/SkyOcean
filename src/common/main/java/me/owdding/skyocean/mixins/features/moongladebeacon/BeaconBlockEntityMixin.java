package me.owdding.skyocean.mixins.features.moongladebeacon;

import me.owdding.skyocean.features.foraging.galatea.MoongladeBeacon;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BeaconBeamOwner;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin extends BlockEntity {

    public BeaconBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(method = "getBeamSections", at = @At("HEAD"), cancellable = true)
    public void getBeamSections(CallbackInfoReturnable<List<BeaconBeamOwner.Section>> cir) {
        if (!MoongladeBeacon.isBlockPos(worldPosition)) return;
        cir.setReturnValue(MoongladeBeacon.getSection());
    }

}
