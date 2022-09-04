package net.eve0415.ifpatcher;

import net.eve0415.ifpatcher.patch.*;
import net.minecraft.launchwrapper.IClassTransformer;

public class Transformer implements IClassTransformer {
    @Override
    public byte[] transform(final String name, final String transformedName, final byte[] bytes) {
        switch (transformedName) {
            case "com.buuz135.industrial.proxy.CommonProxy":
                IFPatcher.LOGGER.info("Patching CommonProxy from IF");
                return new PatchCommonProxy(bytes).apply();

            case "com.buuz135.industrial.proxy.block.upgrade.ConveyorInsertionUpgrade":
                IFPatcher.LOGGER.info("Patching Laser Base from IF");
                return new PatchConveyorInsertionUpgrade(bytes).apply();

            case "com.buuz135.industrial.config.CustomConfiguration":
                IFPatcher.LOGGER.info("Patching CustomConfiguration from IF");
                return new PatchCustomConfiguration(bytes).apply();

            case "com.buuz135.industrial.utils.ItemStackUtils":
                IFPatcher.LOGGER.info("Patching ItemStackUtils from IF");
                return new PatchItemStackUtils(bytes).apply();

            case "com.buuz135.industrial.tile.world.LaserBaseTile":
                IFPatcher.LOGGER.info("Patching Laser Base from IF");
                return new PatchLaserBase(bytes).apply();

            case "com.buuz135.industrial.item.MobImprisonmentToolItem":
                IFPatcher.LOGGER.info("Patching Mob Imprisonment Tool from IF");
                return new PatchMobImprisonmentTool(bytes).apply();

            case "com.buuz135.industrial.tile.agriculture.CropSowerTile":
                IFPatcher.LOGGER.info("Patching Plant Sower from IF");
                return new PatchPlantSower(bytes).apply();

            case "com.buuz135.industrial.tile.magic.PotionEnervatorTile":
                IFPatcher.LOGGER.info("Patching Potion Brewer from IF");
                return new PatchPotionBrewer(bytes).apply();

            case "com.buuz135.industrial.tile.world.FluidPumpTile":
                IFPatcher.LOGGER.info("Patching Fluid Pump from IF");
                return new PatchPump(bytes).apply();

            default:
                return bytes;
        }
    }
}
