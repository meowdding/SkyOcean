package codes.cookies.skyocean.helpers

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin
import net.minecraft.client.resources.model.UnbakedModel

object SkyOceanPreparableModelLoadingPlugin : PreparableModelLoadingPlugin<List<UnbakedModel>> {
    override fun initialize(data: List<UnbakedModel>, pluginContext: ModelLoadingPlugin.Context) {
        pluginContext.modifyBlockModelAfterBake().register {original, context ->
            SelectBakedModel(original)
        }
    }
}
