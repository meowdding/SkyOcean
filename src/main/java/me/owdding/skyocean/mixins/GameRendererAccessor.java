package me.owdding.skyocean.mixins;

import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
//? if >= 1.21.11 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;
 *///?}

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {

    @Accessor("resourcePool")
    CrossFrameResourcePool getResourcePool();

    @Invoker("setPostEffect")
        //? if >= 1.21.11 {
    void invokeSetPostEffect(Identifier identifier);
    //?} else {
    /*void invokeSetPostEffect(ResourceLocation identifier);
     *///?}
}
