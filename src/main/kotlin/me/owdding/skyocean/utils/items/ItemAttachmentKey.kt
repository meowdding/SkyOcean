package me.owdding.skyocean.utils.items

interface ItemAttachmentKey<T> {
    fun name(): String

    companion object {
        fun <T> of(name: String) = object : ItemAttachmentKey<T> {
            override fun name() = name
        }
        fun unit(name: String) = of<Unit>(name)
        fun int(name: String) = of<Int>(name)
        fun boolean(name: String) = of<Boolean>(name)
    }
}
