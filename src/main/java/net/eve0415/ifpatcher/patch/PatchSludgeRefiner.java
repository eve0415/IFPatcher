package net.eve0415.ifpatcher.patch;

import net.eve0415.ifpatcher.IFPatcher;
import net.eve0415.ifpatcher.Patch;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.ListIterator;

public class PatchSludgeRefiner extends Patch {
    private static boolean enableDebuffs;

    public PatchSludgeRefiner(final byte[] inputClass) {
        super(inputClass);
    }

    public static void configuration(final Configuration config) {
        enableDebuffs = config.getBoolean("enableDebuffs",
                "machines" + Configuration.CATEGORY_SPLITTER + "sludge_refiner",
                false, "Enable poison and hunger debuffs within 3 blocks of an operating Sludge Refiner");
    }

    public static void applyDebuffs(final TileEntity tile) {
        if (!enableDebuffs) return;
        final World world = tile.getWorld();
        if (world.isRemote) return;
        final BlockPos pos = tile.getPos();
        final AxisAlignedBB aabb = new AxisAlignedBB(pos).grow(3.0);
        for (final EntityLivingBase entity : world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {
            entity.addPotionEffect(new PotionEffect(MobEffects.POISON, 100, 0));
            entity.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 100, 0));
        }
    }

    @Override
    protected boolean patch() {
        AbstractInsnNode insertionPoint = null;
        final InsnList performWork = findMethod("performWork").instructions;

        for (final ListIterator<AbstractInsnNode> it = performWork.iterator(); it.hasNext(); ) {
            final AbstractInsnNode insnNode = it.next();
            if (insnNode.getOpcode() == FCONST_1) {
                insertionPoint = insnNode;
                break;
            }
        }

        if (insertionPoint == null) {
            IFPatcher.LOGGER.warn("Could not find target instructions to patch. Skipping.");
            return false;
        }

        final InsnList newInst = new InsnList();
        newInst.add(new VarInsnNode(ALOAD, 0));
        newInst.add(new MethodInsnNode(INVOKESTATIC, hookClass, "applyDebuffs",
                "(Lnet/minecraft/tileentity/TileEntity;)V", false));
        performWork.insertBefore(insertionPoint, newInst);
        IFPatcher.LOGGER.info("Patched Sludge Refiner debuffs");

        return true;
    }
}
