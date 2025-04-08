package codes.cookies.skyocean.helpers

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin

object SkyOceanModelLoadingPlugin : ModelLoadingPlugin {
    override fun initialize(pluginContext: ModelLoadingPlugin.Context) {

        pluginContext.modifyBlockModelAfterBake().register { original, context ->
            SelectBakedModel(original)
        }

    }


}
