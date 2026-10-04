package me.owdding.skyocean.accessors;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.helpers.CheckReturnValue;

public interface SafeMutableComponentAccessor {

    @CheckReturnValue
    default MutableComponent skyocean$appendSafe(Component component) {
        throw new NotImplementedException("Implemented in mixins!");
    }

    @CheckReturnValue
    default List<Component> skyocean$mutableSiblings() {
        throw new NotImplementedException("Implemented in mixins!");
    }

    default void skyocean$setContents(ComponentContents contents){
        throw new NotImplementedException("Implemented in mixins!");
    }


}
