package me.owdding.skyocean.utils.boundingboxes

import net.minecraft.core.BlockPos
import net.minecraft.world.level.levelgen.structure.BoundingBox
import kotlin.math.max

class Octree(val boxes: List<BoundingBox>) {

    constructor(vararg boxes: BoundingBox) : this(listOf(*boxes))

    private val root: Branch

    init {
        val encapsulatingBox = BoundingBox.encapsulatingBoxes(boxes).get()
        val center = encapsulatingBox.center
        val span = max(encapsulatingBox.xSpan, encapsulatingBox.zSpan) / 2 + 5
        root = Branch(BoundingBox(center).inflatedBy(span, span, span), boxes)
    }

    override fun toString(): String {
        return root.toString()
    }

    fun visitNode(visitor: (Node, Int) -> Unit) {
        root.visit(visitor, 0)
    }

    fun findLeaf(pos: BlockPos): Leaf? {
        if (!root.getBox().isInside(pos)) {
            return null
        }
        return root.getNode(pos)
    }

    fun isInside(pos: BlockPos): Boolean {
        return findLeaf(pos)?.isInside(pos) == true
    }

    operator fun contains(pos: BlockPos): Boolean = isInside(pos)
}

interface Node {
    fun getBox(): BoundingBox
    fun visit(visitor: (Node, Int) -> Unit, depth: Int)
    fun getNode(pos: BlockPos): Leaf?
}

class CompoundLeaf(leafBox: BoundingBox, private val boxes: List<BoundingBox>) : Leaf(leafBox, leafBox) {
    override fun isInside(pos: BlockPos) = boxes.any { it.isInside(pos) }
}

open class Leaf(private val leafBox: BoundingBox, private val box: BoundingBox) : Node {

    override fun getBox(): BoundingBox = leafBox
    override fun toString(): String {
        return "Leaf($leafBox)"
    }

    override fun visit(visitor: (Node, Int) -> Unit, depth: Int) {
        visitor(this, depth)
    }

    override fun getNode(pos: BlockPos) = this

    open fun isInside(pos: BlockPos) = box.isInside(pos)
}

class Branch(private val boundingBox: BoundingBox, boxes: List<BoundingBox>) : Node {
    private val nodes = Array<Node?>(8) { null }
    private val centerX = (this.boundingBox.minX() + this.boundingBox.xSpan / 2)
    private val centerY = (this.boundingBox.minY() + this.boundingBox.ySpan / 2)
    private val centerZ = (this.boundingBox.minZ() + this.boundingBox.zSpan / 2)

    init {
        List(8) { it to getChildBox(it) }
            .associate { it.first to boxes.filter { box -> it.second.intersects(box) } }
            .mapValues { it.value.toMutableList() }.forEach { (index, boxes) ->
                if (boxes.isEmpty()) {
                    return@forEach
                }
                if (this.areChildrenLeaves()) {
                    nodes[index] = if (boxes.size > 1) {
                        CompoundLeaf(getChildBox(index), boxes)
                    } else {
                        Leaf(getChildBox(index), boxes.first())
                    }
                    return@forEach
                }
                if (boxes.size == 1) {
                    nodes[index] = Leaf(getChildBox(index), boxes.first())
                    return@forEach
                }
                val branch = Branch(getChildBox(index), boxes)
                nodes[index] = branch
            }
    }

    fun getIndex(pos: BlockPos) = getIndex(pos.x, pos.y, pos.z)

    private fun getIndex(x: Int, y: Int, z: Int): Int {
        val negativeX = x - centerX < 0
        val negativeY = y - centerY < 0
        val negativeZ = z - centerZ < 0
        return getIndex(negativeX, negativeY, negativeZ)
    }

    fun getChildBox(index: Int): BoundingBox {
        val positiveZ = index < 4
        val positiveY = (index % 4) < 2
        val positiveX = (index % 2) == 0

        val minX: Int
        val minY: Int
        val minZ: Int
        val maxX: Int
        val maxY: Int
        val maxZ: Int

        if (positiveX) {
            minX = centerX
            maxX = boundingBox.maxX()
        } else {
            minX = boundingBox.minX()
            maxX = centerX - 1
        }
        if (positiveY) {
            minY = centerY
            maxY = boundingBox.maxY()
        } else {
            minY = boundingBox.minY()
            maxY = centerY - 1
        }
        if (positiveZ) {
            minZ = centerZ
            maxZ = boundingBox.maxZ()
        } else {
            minZ = boundingBox.minZ()
            maxZ = centerZ - 1
        }
        return BoundingBox(minX, minY, minZ, maxX, maxY, maxZ)
    }


    fun getIndex(x: Boolean, y: Boolean, z: Boolean): Int {
        var index = 0
        if (x) {
            index += 1
        }
        if (y) {
            index += 2
        }
        if (z) {
            index += 4
        }
        return index
    }

    private fun areChildrenLeaves(): Boolean {
        return this.boundingBox.xSpan <= 8
    }

    override fun getBox() = boundingBox
    override fun visit(visitor: (Node, Int) -> Unit, depth: Int) {
        visitor(this, depth)
        for (node in nodes) {
            node?.visit(visitor, depth + 1)
        }
    }

    override fun getNode(pos: BlockPos): Leaf? {
        return nodes[getIndex(pos)]?.getNode(pos)
    }

    override fun toString(): String {
        return "Branch(bb=$boundingBox; nodes={${nodes.mapIndexed { index, b -> "$index=$b" }.joinToString()}})"
    }
}
