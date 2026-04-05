package me.owdding.skyocean.features.recipe.crafthelper.visitors

import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperEntry
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperLeafNode
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperNode
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperRecipeNode
import me.owdding.skyocean.utils.extensions.orElse
import kotlin.math.max
import me.owdding.skyocean.features.recipe.crafthelper.visitors.CompactedResourceCutoffResult as Result


data class CompactedResourceCutoffResult(val mayBeCollapsed: Boolean, val depth: Int) {
    operator fun plus(other: Result) = Result(this.mayBeCollapsed && other.mayBeCollapsed, max(this.depth, other.depth))
}

object CompactedResourceCutoffTreeTransformer : TreeTransformer<Int>, TreeVisitor<Result, Result> {

    private val no = Result(false, 0)
    private val yes = Result(true, 0)

    context(node: CraftHelperRecipeNode)
    override fun transformRecipeNode(data: Int): CraftHelperNode {
        val result = node.visitRecipeNode(no)
        println(result)
        if (result.mayBeCollapsed && result.depth <= data) {
            return CraftHelperLeafNode(node.output)
        }

        return node.transformChildren(data)
    }

    private fun CraftHelperEntry.resolve() = yes.takeIf {
        (this as? CraftHelperRecipeNode)?.nodes?.size.orElse(0) <= 1 || this is CraftHelperLeafNode
    } ?: no


    override fun combine(one: Result, two: Result): Result = one + two
    override fun CraftHelperEntry.visitNode(context: Result): Result = resolve()
    override fun CraftHelperRecipeNode.visitRecipeNode(context: Result): Result = visitParentNode(context).let {
        it.copy(depth = it.depth + 1)
    }
}
