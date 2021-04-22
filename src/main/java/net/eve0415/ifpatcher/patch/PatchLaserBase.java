package net.eve0415.ifpatcher.patch;

import java.util.ListIterator;
import org.objectweb.asm.Handle;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import net.eve0415.ifpatcher.IFPatcher;
import net.eve0415.ifpatcher.Patch;

public class PatchLaserBase extends Patch {
    public PatchLaserBase(final byte[] inputClass) {
        super(inputClass);
    }

    @Override
    protected boolean patch() {
        String foundHookMethod = null;
        final InsnList innerUpdate = findMethod("innerUpdate").instructions;

        for (final ListIterator<AbstractInsnNode> it = innerUpdate.iterator(); it.hasNext();) {
            final AbstractInsnNode insnNode = it.next();
            if (insnNode.getOpcode() == INVOKEDYNAMIC) {
                for (final Object bsm : ((InvokeDynamicInsnNode) insnNode).bsmArgs) {
                    if (bsm instanceof Handle) {
                        if (((Handle) bsm).getName().contains("lambda")) {
                            foundHookMethod = ((Handle) bsm).getName();
                            break;
                        }
                    }
                }
            }
        }

        if (foundHookMethod == null) {
            IFPatcher.LOGGER.warn("Could not find target method to patch. Skipping.");
            return false;
        }

        final InsnList patchMethod = findMethod(foundHookMethod).instructions;

        for (final ListIterator<AbstractInsnNode> it = patchMethod.iterator(); it.hasNext();) {
            final AbstractInsnNode insnNode = it.next();
            if (insnNode.getOpcode() == INVOKEVIRTUAL && insnNode instanceof MethodInsnNode) {
                if (((MethodInsnNode) insnNode).name.equals("getLenseChanceIncrease")) {
                    if (it.next().getOpcode() != IADD)
                        continue;
                    while (it.hasNext()) {
                        final AbstractInsnNode insn = it.next();
                        if (insn.getOpcode() == GOTO) {
                            patchMethod.remove(insn);
                            while (insn.getNext() != null) {
                                final AbstractInsnNode insnTwo = insn.getNext();
                                patchMethod.remove(insnTwo);
                                if (insnTwo.getOpcode() == POP)
                                    break;
                            }
                            break;
                        }
                    }
                    break;
                }
            }
        }
        IFPatcher.LOGGER.info("Laser Base does not make Minecraft crash on any conditions!");

        return true;
    }
}
