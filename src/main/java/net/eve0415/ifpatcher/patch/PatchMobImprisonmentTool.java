package net.eve0415.ifpatcher.patch;

import com.buuz135.industrial.config.CustomConfiguration;
import net.eve0415.ifpatcher.IFPatcher;
import net.eve0415.ifpatcher.Patch;
import net.minecraftforge.common.config.Configuration;
import org.objectweb.asm.tree.*;

import java.util.Arrays;
import java.util.List;

public class PatchMobImprisonmentTool extends Patch {
    public static List<String> blacklistedEntities;

    public PatchMobImprisonmentTool(final byte[] inputClass) {
        super(inputClass);
    }

    public static void configuration(Configuration config) {
        blacklistedEntities = Arrays.asList(CustomConfiguration.config.getStringList("entityBlacklist", Configuration.CATEGORY_GENERAL + Configuration.CATEGORY_SPLITTER + "mob_imprisonment_tool",
                new String[]{}, "A list of entities blacklist from being captured with the tool. Format: 'modid:entityid'"));
    }

    public static boolean isBlacklisted(String entity) {
        return blacklistedEntities.contains(entity);
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

        return true;
    }
}
