package net.eve0415.ifpatcher.patch;

import com.buuz135.industrial.proxy.BlockRegistry;
import com.buuz135.industrial.utils.BlockUtils;
import com.buuz135.industrial.utils.ItemStackUtils;
import com.buuz135.industrial.utils.WorkUtils;
import net.eve0415.ifpatcher.IFPatcher;
import net.eve0415.ifpatcher.Patch;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.objectweb.asm.tree.*;

import java.util.*;

public class PatchPump extends Patch {
    public static IFluidTank tank;
    public static String fluid;
    public static World world;
    public static BlockPos pos;

    public PatchPump(final byte[] inputClass) {
        super(inputClass);
    }

    public static void beforeWork(final IFluidTank t, final String f, final World w, final BlockPos p) {
        tank = t;
        fluid = f;
        world = w;
        pos = p;
    }

    public static float work(final Block pump, final AxisAlignedBB area) {
        if (WorkUtils.isDisabled(pump) || fluid == null || tank.getFluidAmount() + 1000 > tank.getCapacity()) return 0;

        if (!isBlockSameFluid(pos.down())) return 0;

        final BlockPos farthestFluid = getFarthestFluid(area);
        if (farthestFluid == null) return 0;

        final FluidStack willPump = Objects.requireNonNull(FluidUtil.getFluidHandler(world, farthestFluid, null)).drain(1000, true);
        if (BlockRegistry.fluidPumpBlock.isReplaceFluidWithCobble()) {
            world.setBlockState(farthestFluid, Blocks.COBBLESTONE.getDefaultState());
        } else {
            world.setBlockToAir(farthestFluid);
        }
        tank.fill(willPump, true);
        return 1;
    }

    public static BlockPos getFarthestFluid(final AxisAlignedBB area) {
        int offsetY = 0;
        List<BlockPos> blocks;
        do {
            offsetY--;
            blocks = BlockUtils.getBlockPosInAABB(area.offset(0, offsetY, 0));
            blocks.removeIf(pos -> world.isOutsideBuildHeight(pos) || !isFullFluid(pos) || !isBlockSameFluid(pos));
        } while (!blocks.isEmpty());

        offsetY++;
        final int y = pos.getY() + offsetY;
        final Queue<BlockPos> queue = new PriorityQueue<>(Comparator
                .comparingDouble(value -> ((BlockPos) value).distanceSqToCenter(pos.getX(), y, pos.getZ()))
                .reversed());
        queue.addAll(BlockUtils.getBlockPosInAABB(area.offset(0, offsetY, 0)));
        queue.removeIf(pos -> world.isOutsideBuildHeight(pos) || !isFullFluid(pos) || !isBlockSameFluid(pos));

        return queue.poll();
    }

    public static boolean isBlockSameFluid(final BlockPos pos) {
        final Fluid f = FluidRegistry.lookupFluidForBlock(world.getBlockState(pos).getBlock());
        return f != null && f.getName().equals(fluid);
    }

    public static boolean isFullFluid(final BlockPos pos) {
        final IFluidHandler f = FluidUtil.getFluidHandler(world, pos, null);
        if (f == null) return false;
        return Objects.requireNonNull(FluidUtil.getFluidHandler(world, pos, null)).drain(1000, false) != null;
    }

    public static boolean acceptsFluidItem(final ItemStack stack) {
        return ItemStackUtils.acceptsFluidItem(stack);
    }

    public static void processFluidItems(final IFluidTank tank, final ItemStackHandler fluidItems) {
        ItemStackUtils.fillItemFromTank(fluidItems, tank);
    }

    @Override
    protected boolean patch() {
        // Accepting fluid items. Such as buckets.
        final MethodNode acceptsFluidItem = new MethodNode(ACC_PROTECTED, "acceptsFluidItem", "(Lnet/minecraft/item/ItemStack;)Z", null, null);
        acceptsFluidItem.instructions.add(new VarInsnNode(ALOAD, 1));
        acceptsFluidItem.instructions.add(new MethodInsnNode(INVOKESTATIC, hookClass, "acceptsFluidItem", "(Lnet/minecraft/item/ItemStack;)Z", false));
        acceptsFluidItem.instructions.add(new InsnNode(IRETURN));
        classNode.methods.add(acceptsFluidItem);
        IFPatcher.LOGGER.info("Pump now accepts fluid items!");

        // Filling liquid to item
        final MethodNode processFluidItems = new MethodNode(ACC_PROTECTED, "processFluidItems", "(Lnet/minecraftforge/items/ItemStackHandler;)V", null, null);
        processFluidItems.instructions.add(new VarInsnNode(ALOAD, 0));
        processFluidItems.instructions.add(new InsnNode(DUP));
        String hook = "com/buuz135/industrial/tile/world/FluidPumpTile";
        processFluidItems.instructions.add(new FieldInsnNode(GETFIELD, hook, "tank", "Lnet/minecraftforge/fluids/IFluidTank;"));
        processFluidItems.instructions.add(new VarInsnNode(ALOAD, 1));
        processFluidItems.instructions.add(new MethodInsnNode(INVOKESTATIC, hookClass, "processFluidItems", "(Lnet/minecraftforge/fluids/IFluidTank;Lnet/minecraftforge/items/ItemStackHandler;)V", false));
        processFluidItems.instructions.add(new InsnNode(RETURN));
        classNode.methods.add(processFluidItems);
        IFPatcher.LOGGER.info("Pump now fill fluids to items!");

        // Making it work perfectly.
        final InsnList work = findMethod("work").instructions;
        work.clear();
        work.add(new VarInsnNode(ALOAD, 0));
        work.add(new FieldInsnNode(GETFIELD, hook, "tank", "Lnet/minecraftforge/fluids/IFluidTank;"));
        work.add(new VarInsnNode(ALOAD, 0));
        work.add(new FieldInsnNode(GETFIELD, hook, "fluid", "Ljava/lang/String;"));
        work.add(new VarInsnNode(ALOAD, 0));
        work.add(new FieldInsnNode(GETFIELD, hook, getName("world", "field_145850_b"), "Lnet/minecraft/world/World;"));
        work.add(new VarInsnNode(ALOAD, 0));
        work.add(new FieldInsnNode(GETFIELD, hook, getName("pos", "field_174879_c"), "Lnet/minecraft/util/math/BlockPos;"));
        work.add(new MethodInsnNode(INVOKESTATIC, hookClass, "beforeWork", "(Lnet/minecraftforge/fluids/IFluidTank;Ljava/lang/String;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V", false));
        work.add(new VarInsnNode(ALOAD, 0));
        work.add(new MethodInsnNode(INVOKEVIRTUAL, hook, getName("getBlockType", "func_145838_q"), "()Lnet/minecraft/block/Block;", false));
        work.add(new VarInsnNode(ALOAD, 0));
        work.add(new MethodInsnNode(INVOKEVIRTUAL, hook, "getWorkingArea", "()Lnet/minecraft/util/math/AxisAlignedBB;", false));
        work.add(new MethodInsnNode(INVOKESTATIC, hookClass, "work", "(Lnet/minecraft/block/Block;Lnet/minecraft/util/math/AxisAlignedBB;)F", false));
        work.add(new InsnNode(FRETURN));
        IFPatcher.LOGGER.info("Pump now works properly!");

        return true;
    }
}
