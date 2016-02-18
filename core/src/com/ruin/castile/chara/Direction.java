package com.ruin.castile.chara;

import java.util.EnumMap;

/**
 * Created by Prin on 2016/02/17.
 */
public enum Direction {
    NORTH(false),
    NORTHEAST(true),
    EAST(true),
    SOUTHEAST(true),
    SOUTH(false),
    SOUTHWEST(false),
    WEST(false),
    NORTHWEST(false);

    private final boolean shouldFlipSprite;

    Direction(boolean shouldFlipSprite) {
        this.shouldFlipSprite = shouldFlipSprite;
    }

    private static EnumMap<Direction, Direction> reverseMap;
    
    // if an animation only has four directions, map all eight directions to the four
    private static EnumMap<Direction, Direction> eightDirToFourDirMap;

    static {
        reverseMap = new EnumMap<>(Direction.class);
        reverseMap.put(NORTHEAST, NORTHWEST);
        reverseMap.put(NORTHWEST, NORTHEAST);
        reverseMap.put(EAST, WEST);
        reverseMap.put(WEST, EAST);
        reverseMap.put(SOUTHEAST, SOUTHWEST);
        reverseMap.put(SOUTHWEST, SOUTHEAST);
        reverseMap.put(NORTH, NORTH);
        reverseMap.put(SOUTH, SOUTH);

        eightDirToFourDirMap = new EnumMap<>(Direction.class);
        eightDirToFourDirMap.put(NORTHEAST, NORTHEAST);
        eightDirToFourDirMap.put(NORTHWEST, NORTHWEST);
        eightDirToFourDirMap.put(EAST, SOUTHEAST);
        eightDirToFourDirMap.put(WEST, NORTHWEST);
        eightDirToFourDirMap.put(SOUTHEAST, SOUTHEAST);
        eightDirToFourDirMap.put(SOUTHWEST, SOUTHWEST);
        eightDirToFourDirMap.put(NORTH, NORTHEAST);
        eightDirToFourDirMap.put(SOUTH, SOUTHWEST);
    }

    public boolean shouldFlipSprite(){
        return shouldFlipSprite;
    }

    public Direction getReverseSpriteDirection() {
        return reverseMap.get(this);
    }

    public Direction convertToFourDirections() { return eightDirToFourDirMap.get(this); }
}
