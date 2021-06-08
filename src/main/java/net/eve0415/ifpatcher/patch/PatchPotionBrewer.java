package net.eve0415.ifpatcher.patch;

import net.eve0415.ifpatcher.IFPatcher;
import net.eve0415.ifpatcher.Patch;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.ndrei.teslacorelib.inventory.BoundingRectangle;
import net.ndrei.teslacorelib.inventory.ColoredItemHandler;
import net.ndrei.teslacorelib.inventory.LockableItemHandler;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

public class PatchPotionBrewer extends Patch {

  public PatchPotionBrewer(final byte[] inputClass) {
    super(inputClass);
  }

  public static IItemHandler patchIngredientItemsHandler(
    final LockableItemHandler inputIngredients) {
    return new ColoredItemHandler(inputIngredients, EnumDyeColor.GREEN, "Ingredients items",
      new BoundingRectangle(18 * 4 + 10, 25, 5 * 18, 18)) {
      @Override
      public boolean canInsertItem(final int slot, final ItemStack stack) {
        if (inputIngredients.getLocked()) {
          return super.canInsertItem(slot, stack);
        }
        return !stack.getItem().equals(Items.GLASS_BOTTLE);
      }

      @Override
      public boolean canExtractItem(final int slot) {
        return false;
      }
    };
  }

  @Override
  protected boolean patch() {
    AbstractInsnNode insertionPoint = null;
    final InsnList initInputInventories = findMethod("initInputInventories").instructions;

    for (final ListIterator<AbstractInsnNode> it = initInputInventories.iterator(); it.hasNext(); ) {
      final AbstractInsnNode insnNode = it.next();
      if ((insnNode instanceof FieldInsnNode)
        && ((FieldInsnNode) insnNode).name.equals("inputIngredients")) {
        it.next();
        while (it.hasNext()) {
          final AbstractInsnNode insn = it.next();
          if ((insn instanceof MethodInsnNode)
            && ((MethodInsnNode) insn).name.equals("addInventory")) {
            insertionPoint = insn;
            break;
          }
          initInputInventories.remove(insn);
        }
        break;
      }
    }

    if (insertionPoint == null) {
      IFPatcher.LOGGER.warn("Could not find target instructions to patch. Skipping.");
      return false;
    }

    final InsnList newInst = new InsnList();
    newInst.add(new VarInsnNode(ALOAD, 0));
    newInst.add(new InsnNode(DUP));
    String hook = "com/buuz135/industrial/tile/magic/PotionEnervatorTile";
    newInst.add(new FieldInsnNode(GETFIELD, hook, "inputIngredients",
      "Lnet/ndrei/teslacorelib/inventory/LockableItemHandler;"));
    newInst.add(new MethodInsnNode(INVOKESTATIC, hookClass, "patchIngredientItemsHandler",
      "(Lnet/ndrei/teslacorelib/inventory/LockableItemHandler;)Lnet/minecraftforge/items/IItemHandler;",
      false));
    initInputInventories.insertBefore(insertionPoint, newInst);
    IFPatcher.LOGGER
      .info("Potion Brewer can now brew potion that does not start from Nether Wart!");

    return true;
  }
}
