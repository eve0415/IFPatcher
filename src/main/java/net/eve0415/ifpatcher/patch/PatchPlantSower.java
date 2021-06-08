package net.eve0415.ifpatcher.patch;

import com.buuz135.industrial.IndustrialForegoing;
import com.buuz135.industrial.utils.ItemStackUtils;
import net.eve0415.ifpatcher.IFPatcher;
import net.eve0415.ifpatcher.Patch;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

public class PatchPlantSower extends Patch {

  public PatchPlantSower(final byte[] inputClass) {
    super(inputClass);
  }

  public static void tillBlock(final World world, final boolean hoeGround, final ItemStack stack,
                               final BlockPos pos) {
    final FakePlayer player = IndustrialForegoing.getFakePlayer(world);
    if (hoeGround && !ItemStackUtils.isChorusFlower(stack)
      && !ItemStackUtils.isStackOreDict(stack, "treeSapling")) {
      final ItemStack hoe = new ItemStack(Items.DIAMOND_HOE);
      player.setHeldItem(EnumHand.MAIN_HAND, hoe);
      hoe.getItem().onItemUse(player, world, pos.down(), EnumHand.MAIN_HAND, EnumFacing.UP, 0.5f,
        0.5f, 0.5f);
    }
  }

  @Override
  protected boolean patch() {
    AbstractInsnNode insertionPoint = null;
    final InsnList work = findMethod("work").instructions;

    for (final ListIterator<AbstractInsnNode> it = work.iterator(); it.hasNext(); ) {
      final AbstractInsnNode insnNode = it.next();
      if ((insnNode instanceof FieldInsnNode)
        && ((FieldInsnNode) insnNode).name.equals("hoeGround")) {
        insertionPoint = insnNode.getPrevious().getPrevious();
        work.remove(insnNode.getPrevious());
        work.remove(insnNode);
        while (it.hasNext()) {
          final AbstractInsnNode insn = it.next();
          work.remove(insn);
          if (insn.getOpcode() == POP)
            break;
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
    String hook = "com/buuz135/industrial/tile/agriculture/CropSowerTile";
    newInst.add(new FieldInsnNode(GETFIELD, hook, getName("world", "field_145850_b"),
      "Lnet/minecraft/world/World;"));
    newInst.add(new VarInsnNode(ALOAD, 0));
    newInst.add(new FieldInsnNode(GETFIELD, hook, "hoeGround", "Z"));
    newInst.add(new VarInsnNode(ALOAD, 4));
    newInst.add(new VarInsnNode(ALOAD, 2));
    newInst.add(new MethodInsnNode(INVOKESTATIC, hookClass, "tillBlock",
      "(Lnet/minecraft/world/World;ZLnet/minecraft/item/ItemStack;Lnet/minecraft/util/math/BlockPos;)V",
      false));
    work.insert(insertionPoint, newInst);
    IFPatcher.LOGGER.info("Plant Sower now till any blocks if possible!");

    return true;
  }
}
