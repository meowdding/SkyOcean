package me.owdding.skyocean.accessors;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public interface SafeMutableComponentAccessor {

    MutableComponent skyocean$appendSafe(Component component);

    List<Component> skyocean$mutableSiblings();

    void skyocean$setContents(ComponentContents contents);

}
