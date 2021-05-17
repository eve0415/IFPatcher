package net.eve0415.ifpatcher;

import java.util.ListIterator;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import net.minecraft.launchwrapper.Launch;

public abstract class Patch implements Opcodes {
  protected ClassNode classNode;
  byte[] inputClassBytes;

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
    final ClassWriter classWriter =
        new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
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

  protected final String hookClass =
      getName(getClass()).replaceAll("patch/(.+)Patch", "hook/$1Hook");

  protected MethodNode findMethod(final String methodName) {
    for (final MethodNode methodNode : classNode.methods) {
      if (methodNode.name.equals(methodName)) {
        return methodNode;
      }
    }
    return null;
  }

  protected AnnotationNode findAnnotation(final MethodNode targetMethod,
      final String annotationDesc) {
    for (final AnnotationNode annotationNode : targetMethod.visibleAnnotations) {
      if (annotationNode.desc.equals(annotationDesc)) {
        return annotationNode;
      }
    }
    return null;
  }

  protected InsnNode findReturn(final MethodNode targetMethod) {
    for (final ListIterator<AbstractInsnNode> it = targetMethod.instructions.iterator(); it
        .hasNext();) {
      final AbstractInsnNode insnNode = it.next();

      if (insnNode.getOpcode() == Opcodes.RETURN) {
        return (InsnNode) insnNode;
      }
    }
    return null;
  }
}
