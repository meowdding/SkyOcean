package me.owdding.skyocean.mixins;

import me.owdding.skyocean.accessors.SafeMutableComponentAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

@Mixin(MutableComponent.class)
public abstract class MutableComponentMixin implements SafeMutableComponentAccessor {

    @Shadow
    @Final
    @Mutable
    private List<Component> siblings;

    @Shadow
    public abstract MutableComponent append(Component $$0);

    @Mutable
    @Shadow
    @Final
    private ComponentContents contents;

    @Override
    public MutableComponent skyocean$appendSafe(Component component) {
        skyocean$mutableSiblings();
        return append(component);
    }

    @Override
    public List<Component> skyocean$mutableSiblings() {
        try {
            siblings.addLast(null);
            siblings.removeLast();
            return siblings;
        } catch (UnsupportedOperationException ignored) {
            siblings = new ArrayList<>(siblings);
            return siblings;
        }
    }

    @Override
    public void skyocean$setContents(ComponentContents contents) {
        this.contents = contents;
    }
}
