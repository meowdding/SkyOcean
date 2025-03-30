package codes.cookies.skyocean.mixins.accessor;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import tech.thatgravyboat.skyblockapi.api.area.slayer.SlayerAPI;
import tech.thatgravyboat.skyblockapi.api.area.slayer.SlayerInfo;

import java.util.Map;

@Mixin(SlayerAPI.class)
public interface SlayerAPIAccessor {

    @Accessor(value = "slayerBosses", remap = false)
    Map<Entity, SlayerInfo> getSlayerBosses();

}
