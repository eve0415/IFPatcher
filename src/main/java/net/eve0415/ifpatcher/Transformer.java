package net.eve0415.ifpatcher;

import net.eve0415.ifpatcher.patch.PatchPlantSower;
import net.eve0415.ifpatcher.patch.PatchPotionBrewer;
import net.eve0415.ifpatcher.patch.PatchPump;
import net.minecraft.launchwrapper.IClassTransformer;

public class Transformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        switch (transformedName) {
        case "com.buuz135.industrial.tile.world.FluidPumpTile":
            IFPatcher.LOGGER.info("Patching Fluid Pump from IF");
            return new PatchPump(bytes).apply();

        case "com.buuz135.industrial.tile.agriculture.CropSowerTile":
            IFPatcher.LOGGER.info("Patching Plant Sower from IF");
            return new PatchPlantSower(bytes).apply();

        case "com.buuz135.industrial.tile.magic.PotionEnervatorTile":
            IFPatcher.LOGGER.info("Patching Potion Brewer from IF");
            return new PatchPotionBrewer(bytes).apply();

        default:
            return bytes;
        }
    }
}
