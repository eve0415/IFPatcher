package net.eve0415.ifpatcher.patch;

import net.eve0415.ifpatcher.IFPatcher;
import net.eve0415.ifpatcher.Patch;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.objectweb.asm.tree.*;

public class PatchItemStackUtils  extends Patch {

    public PatchItemStackUtils(final byte[] inputClass) {
        super(inputClass);
    }

    public static void fillTankFromItem(final ItemStackHandler fluidItems, final IFluidTank tank) {
        ItemStack stack = fluidItems.getStackInSlot(0).copy();
        if (stack.isEmpty()) return;
        if (!stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) return;

        IFluidHandlerItem cap = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        FluidStack fluidStack = cap.drain(1000, false);
        if (fluidStack == null) return;
        if (fluidStack.amount - tank.fill(fluidStack, false) != 0) return;

        FluidActionResult test = FluidUtil.tryEmptyContainer(stack, (IFluidHandler) tank, 1000, null, false);
        if (test.isSuccess() && (fluidItems.getStackInSlot(1).isEmpty() ||
                (ItemHandlerHelper.canItemStacksStack(test.getResult(), fluidItems.getStackInSlot(1)) && test.getResult().getCount() + fluidItems.getStackInSlot(1).getCount() <= test.getResult().getMaxStackSize()))) {

            FluidActionResult result = FluidUtil.tryEmptyContainer(stack, (IFluidHandler) tank, tank.getCapacity(), null, true);
            if (fluidItems.getStackInSlot(1).isEmpty()) {
                fluidItems.setStackInSlot(1, result.getResult());
            } else {
                fluidItems.getStackInSlot(1).grow(1);
            }
            fluidItems.getStackInSlot(0).shrink(1);
        }
    }

    @Override
    protected boolean patch() {
        final InsnList fillTank = findMethod("fillTankFromItem").instructions;
        fillTank.clear();
        fillTank.add(new VarInsnNode(ALOAD, 0));
        fillTank.add(new VarInsnNode(ALOAD, 1));
        fillTank.add(new MethodInsnNode(INVOKESTATIC, hookClass, "fillTankFromItem", "(Lnet/minecraftforge/items/ItemStackHandler;Lnet/minecraftforge/fluids/IFluidTank;)V", false));
        fillTank.add(new InsnNode(RETURN));

        IFPatcher.LOGGER.info("Patched Item Stack Utils");

        return true;
    }
}
