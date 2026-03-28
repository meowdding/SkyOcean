//~named_identifier
package me.owdding.skyocean.mixins;

import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.resources.Identifier;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {

    @Accessor("resourcePool")
    CrossFrameResourcePool getResourcePool();

    @Invoker("setPostEffect")
    void invokeSetPostEffect(Identifier identifier);
}
