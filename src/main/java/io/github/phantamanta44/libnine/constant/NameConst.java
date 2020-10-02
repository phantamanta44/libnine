package io.github.phantamanta44.libnine.constant;

import io.github.phantamanta44.libnine.L9Const;

public class NameConst {

    private static final String INFO_KEY = L9Const.MOD_ID + ".info.";
    public static final String INFO_EMPTY = INFO_KEY + "empty";

    public static final String INFO_BLOCK_SIDE_KEY = INFO_KEY + "block_side.";
    public static final String INFO_BLOCK_SIDE_FRONT = INFO_BLOCK_SIDE_KEY + "front";
    public static final String INFO_BLOCK_SIDE_BACK = INFO_BLOCK_SIDE_KEY + "back";
    public static final String INFO_BLOCK_SIDE_UP = INFO_BLOCK_SIDE_KEY + "up";
    public static final String INFO_BLOCK_SIDE_LEFT = INFO_BLOCK_SIDE_KEY + "left";
    public static final String INFO_BLOCK_SIDE_DOWN = INFO_BLOCK_SIDE_KEY + "down";
    public static final String INFO_BLOCK_SIDE_RIGHT = INFO_BLOCK_SIDE_KEY + "right";

    public static final String INFO_REDSTONE_BEHAVIOUR_KEY = INFO_KEY + "redstone_behaviour.";
    public static final String INFO_REDSTONE_BEHAVIOUR_IGNORED = INFO_REDSTONE_BEHAVIOUR_KEY + "ignored";
    public static final String INFO_REDSTONE_BEHAVIOUR_DIRECT = INFO_REDSTONE_BEHAVIOUR_KEY + "direct";
    public static final String INFO_REDSTONE_BEHAVIOUR_INVERTED = INFO_REDSTONE_BEHAVIOUR_KEY + "inverted";

}
