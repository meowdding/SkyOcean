package me.owdding.skyocean.utils

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.brigadier.context.CommandContext
import kotlinx.coroutines.runBlocking
import me.owdding.skyocean.SkyOcean
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.json.Json.readJson
import java.nio.file.Files
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

// TODO: surely better name maybe?
object Utils {
    inline fun <reified T : Any> loadFromRepo(file: String) = runBlocking {
        try {
            SkyOcean.SELF.findPath("repo/$file.json").orElseThrow()?.let(Files::readString)?.readJson<T>() ?: return@runBlocking null
        } catch (e: Exception) {
            SkyOcean.error("Failed to load $file from repo", e)
            null
        }
    }


    infix fun Int.exclusiveInclusive(other: Int) = (this + 1)..other
    infix fun Int.exclusiveExclusive(other: Int) = (this + 1)..(other - 1)

    operator fun Item.contains(stack: ItemStack): Boolean = stack.item == this

    inline fun <reified T> CommandContext<*>.getArgument(name: String): T? = this.getArgument(name, T::class.java)

    @OptIn(ExperimentalContracts::class)
    inline fun PoseStack.atCamera(task: PoseStack.() -> Unit) {
        contract {
            callsInPlace(task, InvocationKind.EXACTLY_ONCE)
        }

        val camera = McClient.self.gameRenderer.mainCamera
        this.pushPose()
        this.translate(-camera.position.x, -camera.position.y, -camera.position.z)
        this.task()
        this.popPose()
    }
}
