package com.github.kisaragimikoto.tacticalbackpack.menu.util;

public final class MenuConstants {

    private MenuConstants() {}

    public static final int PLAYER_INVENTORY_ROWS = 3;
    public static final int PLAYER_HOTBAR_SIZE = 9;

    public static final int PLAYER_SLOT_COUNT =
            PLAYER_INVENTORY_ROWS * PLAYER_HOTBAR_SIZE
                    + PLAYER_HOTBAR_SIZE;

}