package net.eve0415.ifpatcher.patch;

import net.eve0415.ifpatcher.IFPatcher;
import net.eve0415.ifpatcher.Patch;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.ListIterator;

public class PatchConveyorInsertionUpgrade extends Patch {
    public PatchConveyorInsertionUpgrade(final byte[] inputClass) {
        super(inputClass);
    }

    public static void insertLine(final Entity entity) {
        ((EntityItem) entity).setItem(ItemStack.EMPTY);
    }

    @Override
    protected boolean patch() {
        AbstractInsnNode insertionPoint = null;
        final InsnList handleEntity = findMethod("handleEntity").instructions;

        for (final ListIterator<AbstractInsnNode> it = handleEntity.iterator(); it.hasNext(); ) {
            final AbstractInsnNode insnNode = it.next();
            if ((insnNode instanceof MethodInsnNode) && ((MethodInsnNode) insnNode).name.equals(getName("isEmpty", "func_190926_b"))) {
                insertionPoint = insnNode.getNext();
                break;
            }
        }

        if (insertionPoint == null) {
            IFPatcher.LOGGER.warn("Could not find target instructions to patch. Skipping.");
            return false;
        }

        final InsnList newInst = new InsnList();
        newInst.add(new VarInsnNode(ALOAD, 1));
        newInst.add(new MethodInsnNode(INVOKESTATIC, hookClass, "insertLine", "(Lnet/minecraft/entity/Entity;)V", false));
        handleEntity.insert(insertionPoint, newInst);
        IFPatcher.LOGGER.info("Using 'Insertion Conveyor Upgrade' will no longer duplicate items!");

        return true;
    }
}
