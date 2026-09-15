package com.github.kisaragimikoto.tacticalbackpack.compat.cobblemon;

import com.github.kisaragimikoto.tacticalbackpack.world.BackpackBiomeZoneType;
import com.github.kisaragimikoto.tacticalbackpack.world.ModDimensions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class BackpackCobblemonZoneUtil {

    public static boolean isInBackpackWorld(Level level) {
        return level.dimension().equals(ModDimensions.BACKPACK_INNER_WORLD);
    }

    public static BackpackBiomeZoneType getZoneType(BlockPos pos) {
        int x = pos.getX();
        int z = pos.getZ();

        if (isInside(x, z, -60, 0)) {
            return BackpackBiomeZoneType.PLAINS;
        }

        if (isInside(x, z, -30, 0)) {
            return BackpackBiomeZoneType.FOREST;
        }

        if (isInside(x, z, 0, 0)) {
            return BackpackBiomeZoneType.DESERT;
        }

        if (isInside(x, z, 30, 0)) {
            return BackpackBiomeZoneType.SNOW;
        }

        if (isInside(x, z, 60, 0)) {
            return BackpackBiomeZoneType.OCEAN;
        }

        if (isInside(x, z, -60, 40)) {
            return BackpackBiomeZoneType.CAVE;
        }

        if (isInside(x, z, -30, 40)) {
            return BackpackBiomeZoneType.NETHER;
        }

        if (isInside(x, z, 0, 40)) {
            return BackpackBiomeZoneType.END;
        }

        return BackpackBiomeZoneType.PLAINS;
    }

    private static boolean isInside(int x, int z, int centerX, int centerZ) {
        int radius = 12;

        return x >= centerX - radius
                && x <= centerX + radius
                && z >= centerZ - radius
                && z <= centerZ + radius;
    }
}