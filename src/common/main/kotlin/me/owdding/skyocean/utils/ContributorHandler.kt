package me.owdding.skyocean.utils

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktmodules.Module
import me.owdding.skyocean.generated.CodecUtils
import net.minecraft.core.ClientAsset
import net.minecraft.core.UUIDUtil
import java.util.*

@Module
object ContributorHandler {

    val contributors: MutableMap<UUID, ContributorData> =
        Utils.loadRepoData<ContributorData, MutableMap<UUID, ContributorData>>("contributors") { c -> CodecUtils.map(UUIDUtil.STRING_CODEC, c) }

    @GenerateCodec
    data class ContributorData(
        val cape: ClientAsset,
    )
}
