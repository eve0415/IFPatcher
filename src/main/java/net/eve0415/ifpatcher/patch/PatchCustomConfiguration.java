package net.eve0415.ifpatcher.patch;

import net.eve0415.ifpatcher.IFPatcher;
import net.eve0415.ifpatcher.Patch;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.ListIterator;

public class PatchCustomConfiguration extends Patch {

  public PatchCustomConfiguration(final byte[] inputClass) {
    super(inputClass);
  }

  @Override
  protected boolean patch() {
    AbstractInsnNode insertionPoint = null;
    final InsnList handlePostInit = findMethod("sync").instructions;

    for (final ListIterator<AbstractInsnNode> it = handlePostInit.iterator(); it.hasNext(); ) {
      final AbstractInsnNode insnNode = it.next();
      if (insnNode.getOpcode() == GOTO) {
        insertionPoint = insnNode.getPrevious();
        break;
      }
    }

    if (insertionPoint == null) {
      IFPatcher.LOGGER.warn("Could not find target instructions to patch. Skipping.");
      return false;
    }

    final InsnList newInst = new InsnList();
    newInst.add(new FieldInsnNode(GETSTATIC, "com/buuz135/industrial/proxy/ItemRegistry", "mobImprisonmentToolItem", "Lcom/buuz135/industrial/item/MobImprisonmentToolItem;"));
    newInst.add(new FieldInsnNode(GETSTATIC, "com/buuz135/industrial/config/CustomConfiguration", "config", "Lnet/minecraftforge/common/config/Configuration;"));
    newInst.add(new MethodInsnNode(INVOKEVIRTUAL, "com/buuz135/industrial/item/MobImprisonmentToolItem", "configuration", "(Lnet/minecraftforge/common/config/Configuration;)V", false));
    handlePostInit.insert(insertionPoint, newInst);
    IFPatcher.LOGGER.info("Patched Custom Configuration");

    return true;
  }
}
