package me.owdding.skyocean.accessors;

import kotlin.time.Instant;

public interface    WalkAnimationStateAccessor {

    static Integer getMoveStartTime(Object player) {
        if (player instanceof WalkAnimationStateAccessor accessor) {
            return accessor.ocean$getStartMoveTime();
        }
        return null;
    }

    Integer ocean$getStartMoveTime();

}
