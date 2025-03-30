package codes.cookies.skyocean.modules

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.writeTo

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Module

/**
 * Ty sophie :3
 */
class Processor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    private var ran = false

    private fun validateSymbol(symbol: KSAnnotated): KSClassDeclaration? {
        if (!symbol.validate()) {
            logger.warn("Symbol is not valid: $symbol")
            return null
        }

        if (symbol !is KSClassDeclaration || symbol.classKind != ClassKind.OBJECT) {
            logger.error("@Module is only valid on objects", symbol)
            return null
        }
        return symbol
    }

    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (ran) return emptyList()
        ran = true

        val annotated = resolver.getSymbolsWithAnnotation(Module::class.qualifiedName!!).toList()
        val validModules = annotated.mapNotNull { validateSymbol(it) }

        logger.warn("--- Module Processor ---")
        logger.warn("Found ${validModules.size} modules")
        logger.warn("Generating Modules.kt")

        val file = FileSpec.builder("codes.cookies.skyocean.generated", "Modules")
            .indent("    ")
            .addType(
                TypeSpec.objectBuilder("Modules").apply {
                    this.addModifiers(KModifier.INTERNAL)
                    this.addFunction(
                        FunSpec.builder("load")
                            .addCode(
                                CodeBlock.builder()
                                    .addStatement("val bus = tech.thatgravyboat.skyblockapi.api.SkyBlockAPI.eventBus").apply {
                                        validModules.forEach { module ->
                                            addStatement("bus.register(${module.qualifiedName!!.asString()})")
                                        }
                                    }.build(),
                            )
                            .build(),
                    )
                }.build(),
            )

        file.build().writeTo(
            codeGenerator,
            Dependencies(true, *validModules.mapNotNull(KSClassDeclaration::containingFile).toTypedArray()),
        )

        return emptyList()
    }
}

class ModuleProvider : SymbolProcessorProvider {
    override fun create(
        environment: SymbolProcessorEnvironment,
    ): SymbolProcessor = Processor(environment.codeGenerator, environment.logger)
}
