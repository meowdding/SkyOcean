package me.owdding.skyocean.features.recipe.crafthelper.visitors

import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperEntry
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperLeafNode
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperNode
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperParentNode
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperRecipeNode
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperTree
import java.util.function.BiFunction
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

interface TreeVisitor<ReturnType, ContextType> {

    fun combine(one: ReturnType, two: ReturnType): ReturnType
    fun <Type : CraftHelperNode> Type.visitAny(context: ContextType): ReturnType = when (this) {
        is CraftHelperRecipeNode -> this.visitRecipeNode(context)
        is CraftHelperLeafNode -> this.visitLeafNode(context)
    }

    fun CraftHelperEntry.visitNode(context: ContextType): ReturnType
    fun CraftHelperParentNode.visitParentNode(context: ContextType): ReturnType {
        return buildList {
            add(visitNode(context))
            addAll(this@visitParentNode.nodes.map { it.visitAny(context) })
        }.reduce(::combine)
    }

    fun CraftHelperLeafNode.visitLeafNode(context: ContextType): ReturnType = visitNode(context)

    fun CraftHelperRecipeNode.visitRecipeNode(context: ContextType): ReturnType = visitParentNode(context)
    fun CraftHelperTree.visitRootNode(context: ContextType): ReturnType = visitParentNode(context)

}

interface TreeTransformerVoid : TreeTransformer<Unit> {

    context(node: Node)
    fun <Node : CraftHelperNode> transformNode(): CraftHelperNode = node

    context(node: Node)
    override fun <Node : CraftHelperNode> transformNode(data: Unit): CraftHelperNode = transformNode()


    context(node: CraftHelperLeafNode)
    fun transformLeafNode(): CraftHelperNode = transformNode(Unit)

    context(node: CraftHelperLeafNode)
    override fun transformLeafNode(data: Unit): CraftHelperNode = transformLeafNode().transformChildren(Unit)


    context(node: CraftHelperRecipeNode)
    fun transformRecipeNode(): CraftHelperNode = transformNode(Unit).transformChildren(Unit)

    context(node: CraftHelperRecipeNode)
    override fun transformRecipeNode(data: Unit): CraftHelperNode = transformRecipeNode()


    context(node: CraftHelperTree)
    fun transformRootNode(): CraftHelperTree = node.transformChildren(Unit)

    context(node: CraftHelperTree)
    override fun transformRootNode(data: Unit): CraftHelperTree = transformRootNode()

    override fun apply(data: Unit, tree: CraftHelperTree): CraftHelperTree = context(tree) { transformRootNode() }
}

@OptIn(ExperimentalContracts::class)
private inline fun <reified Type, Receiver> Receiver.applyIf(runnable: Type.() -> Unit): Receiver {
    contract {
        callsInPlace(runnable, InvocationKind.AT_MOST_ONCE)
    }

    if (this is Type) {
        this.runnable()
    }

    return this
}

interface TreeTransformer<DataType> : BiFunction<DataType, CraftHelperTree, CraftHelperTree> {


    private inline operator fun <Type, ReturnType> Type.invoke(a: context(Type) () -> ReturnType): ReturnType = a()

    private fun <Node : CraftHelperNode> transformAny(node: Node, data: DataType): CraftHelperNode = when (node) {
        is CraftHelperRecipeNode -> node { transformRecipeNode(data) }
        is CraftHelperLeafNode -> node { transformLeafNode(data) }
    }

    context(node: Node)
    fun <Node : CraftHelperNode> transformNode(data: DataType): CraftHelperNode = node

    context(node: CraftHelperLeafNode)
    fun transformLeafNode(data: DataType): CraftHelperNode = this.transformNode(data)

    fun <Node : CraftHelperEntry> Node.transformChildren(data: DataType): Node = this.applyIf<CraftHelperParentNode, _> {
        val newNodes = this.nodes.map { transformAny(it, data) }
        this.nodes.clear()
        this.nodes.addAll(newNodes)
        this.recalculateChildren()
    }

    context(node: CraftHelperRecipeNode)
    fun transformRecipeNode(data: DataType): CraftHelperNode = this.transformNode(data).transformChildren(data)

    context(node: CraftHelperTree)
    fun transformRootNode(data: DataType): CraftHelperTree = node.transformChildren(data)

    override fun apply(data: DataType, tree: CraftHelperTree): CraftHelperTree = context(tree) { transformRootNode(data) }

}
