package me.owdding.skyocean.accessors;

public interface    WalkAnimationStateAccessor {

    static Integer getMoveStartTime(Object player) {
        if (player instanceof WalkAnimationStateAccessor accessor) {
            return accessor.ocean$getStartMoveTime();
        }
        return null;
    }

    Integer ocean$getStartMoveTime();

}
