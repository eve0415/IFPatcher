package net.eve0415.ifpatcher.patch;

import com.buuz135.industrial.utils.ItemStackUtils;

import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.eve0415.ifpatcher.IFPatcher;
import net.eve0415.ifpatcher.Patch;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.ItemStackHandler;

public class PatchPump extends Patch {
    public PatchPump(final byte[] inputClass) {
        super(inputClass);
    }

    @Override
    protected boolean patch() {
        // Accepting fluid items. Such as buckets.
        final MethodNode acceptsFluidItem = new MethodNode(ASM5, ACC_PUBLIC | ACC_SYNTHETIC, "acceptsFluidItem",
                "(Lnet/minecraft/item/ItemStack;)Z", null, null);
        acceptsFluidItem.instructions.add(new VarInsnNode(ALOAD, 1));
        acceptsFluidItem.instructions.add(new MethodInsnNode(INVOKESTATIC, hookClass, "acceptsFluidItem",
                "(Lnet/minecraft/item/ItemStack;)Z", false));
        acceptsFluidItem.instructions.add(new InsnNode(IRETURN));
        classNode.methods.add(acceptsFluidItem);
        IFPatcher.LOGGER.info("Pump now accepts fluid items!");

        // Filling some liquids to item
        final MethodNode processFluidItems = new MethodNode(ASM5, ACC_PUBLIC | ACC_SYNTHETIC, "processFluidItems",
                "(Lnet/minecraftforge/items/ItemStackHandler;)V", null, null);
        processFluidItems.instructions.add(new VarInsnNode(ALOAD, 0));
        processFluidItems.instructions.add(new InsnNode(DUP));
        processFluidItems.instructions.add(new FieldInsnNode(GETFIELD,
                "com/buuz135/industrial/tile/world/FluidPumpTile", "tank", "Lnet/minecraftforge/fluids/IFluidTank;"));
        processFluidItems.instructions.add(new VarInsnNode(ALOAD, 1));
        processFluidItems.instructions.add(new MethodInsnNode(INVOKESTATIC, hookClass, "processFluidItems",
                "(Lnet/minecraftforge/fluids/IFluidTank;Lnet/minecraftforge/items/ItemStackHandler;)V", false));
        processFluidItems.instructions.add(new InsnNode(RETURN));
        classNode.methods.add(processFluidItems);
        IFPatcher.LOGGER.info("Pump now fill fluids to items!");

        return true;
    }

    public static boolean acceptsFluidItem(final ItemStack stack) {
        return ItemStackUtils.acceptsFluidItem(stack);
    }

    public static void processFluidItems(final IFluidTank tank, final ItemStackHandler fluidItems) {
        ItemStackUtils.fillItemFromTank(fluidItems, tank);
    }
}
