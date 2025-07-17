package me.owdding.skyocean.helpers.fakeblocks

// data class FakeBlockModelEntry(
//     val blend: BlendMode? = null,
//     val models: Map<BlockState, BlockStateModel>,
//     val predicate: (BlockState, BlockPos) -> Boolean,
// ) {
//
//     val transform: QuadTransform by lazy {
//         val materials = Renderer.get()?.materialFinder()
//         if (blend != null && materials != null) {
//             QuadTransform { quad ->
//                 quad.material(materials.copyFrom(quad.material()).blendMode(blend).find())
//                 true
//             }
//         } else {
//             QuadTransform { true }
//         }
//     }
//
//     fun isActive(state: BlockState, pos: BlockPos): Boolean {
//         return predicate(state, pos)
//     }
// }

// class FakeBlockModel(
//     val model: BlockStateModel,
//     val alternatives: List<FakeBlockModelEntry>
// ): FabricBlockStateModel by model, BlockStateModel {
//
//     override fun emitQuads(
//         emitter: QuadEmitter,
//         blockView: BlockAndTintGetter,
//         pos: BlockPos,
//         state: BlockState,
//         random: RandomSource,
//         cullTest: Predicate<Direction?>,
//     ) {
//         if (alternatives.isNotEmpty()) {
//             for (entry in alternatives) {
//                 val stateModel = entry.models[state]
//                 if (stateModel != null && entry.isActive(state, pos)) {
//                     emitter.pushTransform(entry.transform)
//                     stateModel.emitQuads(emitter, blockView, pos, state, random, cullTest)
//                     emitter.popTransform()
//                     return
//                 }
//             }
//         }
//
//         model.emitQuads(emitter, blockView, pos, state, random, cullTest)
//     }
//
//     override fun collectParts(randomSource: RandomSource, list: MutableList<BlockModelPart>) {
//         model.collectParts(randomSource, list)
//     }
//
//     override fun particleIcon(): TextureAtlasSprite {
//         return model.particleIcon()
//     }
// }

// class FakeBlockUnbakedModel(
//     val block: Block,
//     val original: BlockStateModel.UnbakedRoot,
//     val entries: List<FakeBlockUnbakedEntry>
// ) : BlockStateModel.UnbakedRoot {
//
//     override fun bake(state: BlockState, baker: ModelBaker): BlockStateModel = FakeBlockModel(
//         original.bake(state, baker),
//         entries.map { (definition, predicate) ->
//             FakeBlockModelEntry(
//                 definition.blend,
//                 definition.instantiate(state.block, baker),
//                 predicate
//             )
//         }
//     )
//
//     override fun visualEqualityGroup(blockState: BlockState): Any? {
//         return original.visualEqualityGroup(blockState)
//     }
//
//     override fun resolveDependencies(resolver: ResolvableModel.Resolver) {
//         original.resolveDependencies(resolver)
//         for ((definition, _) in entries) {
//             for (root in definition.getRoots(this.block).values) {
//                 root.resolveDependencies(resolver)
//             }
//         }
//     }
//
// }
