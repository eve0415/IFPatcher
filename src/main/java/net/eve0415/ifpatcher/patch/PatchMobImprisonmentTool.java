package net.eve0415.ifpatcher.patch;

import com.buuz135.industrial.config.CustomConfiguration;
import net.eve0415.ifpatcher.IFPatcher;
import net.eve0415.ifpatcher.Patch;
import net.minecraftforge.common.config.Configuration;
import org.objectweb.asm.tree.*;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Pattern;

public class PatchMobImprisonmentTool extends Patch {
    public static Boolean blacklistBosses;
    public static List<String> blacklistedEntities;

    public PatchMobImprisonmentTool(final byte[] inputClass) {
        super(inputClass);
    }

    public static void configuration(Configuration config) {
        blacklistBosses = CustomConfiguration.config.getBoolean("blacklistBosses", Configuration.CATEGORY_GENERAL + Configuration.CATEGORY_SPLITTER + "mob_imprisonment_tool",
                true, "Should bosses be blacklisted from being captured with the tool? entityBlackList will have priority over this setting.");
        blacklistedEntities = Arrays.asList(CustomConfiguration.config.getStringList("entityBlacklist", Configuration.CATEGORY_GENERAL + Configuration.CATEGORY_SPLITTER + "mob_imprisonment_tool",
                new String[]{"minecraft:wither"}, "A list of entities blacklist from being captured with the tool. Format: 'modid:entityid', 'modid:*', 'modid:chicken*'"));
    }

    public static boolean isBlacklisted(String entity) {
        for (String blacklistedEntity : blacklistedEntities) {
            if (!blacklistedEntity.contains("*") && blacklistedEntity.equals(entity)) {
                return true;
            } else if (blacklistedEntity.contains("*")) {
                Pattern pattern = Pattern.compile(blacklistedEntity.replace("*", ".*"));
                if (pattern.matcher(entity).matches()) return true;
            }
        }
        IFPatcher.LOGGER.info("Boss blacklisted: {}", blacklistBosses);
        return false;
    }

    @Override
    protected boolean patch() {
        // Add config settings
        final MethodNode configuration = new MethodNode(ACC_PUBLIC, "configuration", "(Lnet/minecraftforge/common/config/Configuration;)V", null, null);
        configuration.instructions.add(new VarInsnNode(ALOAD, 1));
        configuration.instructions.add(new MethodInsnNode(INVOKESTATIC, hookClass, "configuration", "(Lnet/minecraftforge/common/config/Configuration;)V", false));
        configuration.instructions.add(new InsnNode(RETURN));
        classNode.methods.add(configuration);
        IFPatcher.LOGGER.info("MobImprisonmentTool configuration system added");

        // Implement blacklisted entities
        final InsnList isBlacklisted = findMethod("isBlacklisted").instructions;
        isBlacklisted.clear();
        isBlacklisted.add(new VarInsnNode(ALOAD, 1));
        isBlacklisted.add(new MethodInsnNode(INVOKESTATIC, hookClass, "isBlacklisted", "(Ljava/lang/String;)Z", false));
        isBlacklisted.add(new InsnNode(IRETURN));
        IFPatcher.LOGGER.info("Implement MobImprisonmentTool blacklist");

        // Remove default blacklist of boss
        InsnList itemInteractionForEntity = findMethod(getName("itemInteractionForEntity", "func_111207_a")).instructions;
        for (final ListIterator<AbstractInsnNode> it = itemInteractionForEntity.iterator(); it.hasNext(); ) {
            final AbstractInsnNode insnNode = it.next();
            if (insnNode instanceof MethodInsnNode) {
                final MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;
                if (methodInsnNode.name.equals(getName("isNonBoss", "func_184222_aU"))) {
                    itemInteractionForEntity.remove(methodInsnNode.getPrevious());
                    itemInteractionForEntity.remove(methodInsnNode.getNext());
                    itemInteractionForEntity.remove(methodInsnNode);
                    IFPatcher.LOGGER.info("Remove default blacklist of boss");
                    break;
                }
            }
        }

        // Check if boss should be blacklisted
        itemInteractionForEntity = findMethod(getName("itemInteractionForEntity", "func_111207_a")).instructions;
        for (final ListIterator<AbstractInsnNode> it = itemInteractionForEntity.iterator(); it.hasNext(); ) {
            final AbstractInsnNode insnNode = it.next();
            if (insnNode.getOpcode() == NEW) {
                final InsnList blacklistBosses = new InsnList();
                final LabelNode label = new LabelNode();
                blacklistBosses.add(new FieldInsnNode(GETSTATIC, hookClass, "blacklistBosses", "Ljava/lang/Boolean;"));
                blacklistBosses.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false));
                blacklistBosses.add(new JumpInsnNode(IFEQ, label));
                blacklistBosses.add(new VarInsnNode(ALOAD, 3));
                blacklistBosses.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/entity/EntityLivingBase", getName("isNonBoss", "func_184222_aU"), "()Z", false));//
                blacklistBosses.add(new JumpInsnNode(IFNE, label));
                blacklistBosses.add(new InsnNode(ICONST_0));
                blacklistBosses.add(new InsnNode(IRETURN));
                blacklistBosses.add(label);

                itemInteractionForEntity.insertBefore(insnNode, blacklistBosses);
                IFPatcher.LOGGER.info("Add condition check for boss blacklist");
                break;
            }
        }

        return true;
    }
}
