package net.eve0415.ifpatcher;

import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public abstract class Patch implements Opcodes {
    protected final String hookClass = getName(getClass()).replaceAll("patch/(.+)Patch", "hook/$1Hook");
    protected final ClassNode classNode;
    final byte[] inputClassBytes;

    public Patch(final byte[] inputClass) {
        inputClassBytes = inputClass;
        final ClassReader classReader = new ClassReader(inputClass);
        classNode = new ClassNode();
        classReader.accept(classNode, 0);
    }

    public byte[] apply() {
        if (patch()) {
            IFPatcher.LOGGER.info("{} succeeded", this.getClass().getSimpleName());
            return writeClass();
        } else {
            IFPatcher.LOGGER.error("{} failed", this.getClass().getSimpleName());
            return inputClassBytes;
        }
    }

    protected byte[] writeClass() {
        final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    protected abstract boolean patch();

    protected String getName(final Class<?> clazz) {
        return Type.getInternalName(clazz);
    }

    protected String getName(final String name, final String srgName) {
        return (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment") ? name : srgName;
    }

    protected MethodNode findMethod(final String methodName) {
        for (final MethodNode methodNode : classNode.methods) {
            if (methodNode.name.equals(methodName)) return methodNode;
        }
        return null;
    }

}
