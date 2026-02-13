package me.owdding.skyocean.repo.models

import com.mojang.authlib.properties.Property
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.serialization.MapCodec
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.Utils.unsafeCast
import net.minecraft.client.model.`object`.skull.SkullModelBase
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer
import net.minecraft.world.level.block.SkullBlock
import net.minecraft.world.level.block.entity.BlockEntityType
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.platform.GameProfile
import tech.thatgravyboat.skyblockapi.platform.toResolvableProfile

@GenerateCodec
data class PlayerHeadModel(
    override val transform: ModelTransform = ModelTransform.DEFAULT,
    val texture: String,
    val signature: String?,
) : SkyOceanModel {
    companion object {
        val model: SkullModelBase by lazy {
            val renderer: SkullBlockRenderer = McClient.self.blockEntityRenderDispatcher.renderers[BlockEntityType.SKULL].unsafeCast()
            renderer.modelByType.apply(SkullBlock.Types.PLAYER)
        }
        val renderer: SkullBlockRenderer by lazy {
            McClient.self.blockEntityRenderDispatcher.renderers[BlockEntityType.SKULL].unsafeCast()
        }
    }

    override val codec: MapCodec<PlayerHeadModel> = SkyOceanCodecs.getMapCodec()

    val profile = GameProfile {
        put("textures", Property("textures", texture))
        if (signature != null) {
            put("textures", Property("signature", signature))
        }
    }.toResolvableProfile()


    override fun tick() {}

    override fun emit(
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int,
        packedOverlay: Int,
    ) {
        model.renderToBuffer(poseStack, bufferSource.getBuffer(renderer.playerSkinRenderCache.getOrDefault(profile).renderType()), packedLight, packedOverlay)
    }

}
