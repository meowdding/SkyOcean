package me.owdding.skyocean.mixins.features.fishing;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.owdding.skyocean.config.features.fishing.FishingConfig;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin extends Projectile {

    public FishingHookMixin(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Unique
    private static boolean isEnabled() {
        return LocationAPI.INSTANCE.isOnSkyBlock() && FishingConfig.INSTANCE.getFixBobber();
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean skyocean$fixLavaBobber(FluidState instance, TagKey<Fluid> tag, Operation<Boolean> original) {
        return original.call(instance, tag) || (isEnabled() && instance.is(FluidTags.LAVA));
    }

    @Unique
    private boolean shouldBlockHook(@Nullable Entity entity) {
        if (entity == null || !isEnabled()) return false;
        return (entity instanceof ArmorStand armorStand) && armorStand.getId() == getId() + 1;
    }

    @WrapOperation(method = "onSyncedDataUpdated", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getEntity(I)Lnet/minecraft/world/entity/Entity;"))
    private Entity skyocean$fixBobberHook(Level instance, int id, Operation<Entity> original) {
        Entity entity = original.call(instance, id);
        return shouldBlockHook(entity) ? null : entity;
    }

}
