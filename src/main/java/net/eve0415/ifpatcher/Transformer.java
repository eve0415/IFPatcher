package net.eve0415.ifpatcher;

import net.eve0415.ifpatcher.patch.PatchPump;
import net.minecraft.launchwrapper.IClassTransformer;

public class Transformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        switch (transformedName) {
        case "com.buuz135.industrial.tile.world.FluidPumpTile":
            IFPatcher.LOGGER.info("Patching Fluid Pump from IF");
            return new PatchPump(bytes).apply();

        default:
            return bytes;
        }
    }
}
