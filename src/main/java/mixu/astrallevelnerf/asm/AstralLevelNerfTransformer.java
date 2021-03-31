package mixu.astrallevelnerf.asm;

import com.typesafe.config.ConfigException;
import mixu.astrallevelnerf.AstralLevelNerf;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.Arrays;

import static org.objectweb.asm.Opcodes.*;

public class AstralLevelNerfTransformer implements IClassTransformer {
    private static final String classToTransform = "hellfirepvp.astralsorcery.common.constellation.perk.PerkLevelManager";
    private static final String astralFunction = "ensureLevels";

    private static boolean isStartNode(AbstractInsnNode node) {
        if (node == null || node.getNext() == null) return false;
        return  node.getOpcode() == LDC &&
                node.getNext().getOpcode() == LADD &&
                node.getNext().getNext().getOpcode() == LDC;
    }

    private static boolean isEndNode(AbstractInsnNode node) {
        if (node == null || node.getNext() == null) return false;
        return  node.getOpcode() == LADD &&
                node.getNext().getOpcode() == INVOKESTATIC;
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] classBeingTransformed) {
        if (transformedName.equals(classToTransform)) {
            return transform(transformedName, classBeingTransformed);
        }
        return classBeingTransformed;
    }

    private byte[] transform(String className, byte[] classBeingTransformed) {
        boolean ok = true;
        try {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(classBeingTransformed);
            classReader.accept(classNode, 0);

            transformAstral(classNode);
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(classWriter);
            return classWriter.toByteArray();
        } catch (Exception e) {
            ok = false;
            AstralLevelNerfPlugin.logger.error("Failed to transform astral level function!");
            AstralLevelNerfPlugin.logger.error(e);
        } finally {
            if (ok)
                AstralLevelNerfPlugin.logger.info("Transformed astral level function successfully");
        }
        return classBeingTransformed;
    }

    private void transformAstral(ClassNode clazz) {
        for (MethodNode method : clazz.methods)
        {
            if (method.name.equals(astralFunction))
            {
                AbstractInsnNode start = null;
                AbstractInsnNode end = null;
                for (AbstractInsnNode instruction : method.instructions.toArray())
                {
                    if (isStartNode(instruction))
                    {
                        AstralLevelNerfPlugin.logger.info("Found start");
                        start = instruction; //LDC 150
                        continue;
                    }
                    if (isEndNode(instruction))
                    {
                        AstralLevelNerfPlugin.logger.info("Found end");
                        end = instruction.getNext().getNext(); //INVOKEINTERFACE map put
                        break;
                    }
                }
                if (start == null || end == null)
                {
                    throw new NullPointerException("Start or end node not found!");
                }
                {
                    boolean started = false;
                    for (AbstractInsnNode instruction : method.instructions.toArray())
                    {
                        if (isStartNode(instruction) || started)
                        {
                            start = start.getOpcode() == LLOAD ? start : start.getPrevious(); //LLOAD 2
                            started = started || isStartNode(instruction);

                            if (instruction.getOpcode() == INVOKEINTERFACE)
                            {
                                end = instruction;
                                break;
                            }

                            method.instructions.remove(instruction);
                        }
                    }
                }
                /*
                    ILOAD 1 - loop index
                    LLOAD 2 - prev

                    tree now
                    ALOAD 0
                    GETFIELD expMap
                    ILOAD 1
                    INVOKESTATIC integer I, loop index
                    (ILOAD 1)
                    LLOAD 2
                    (list here)
                    INVOKEINTERFACE expMap.put
                */
                InsnList index = new InsnList();
                index.add(new VarInsnNode(ILOAD, 1));
                index.add(new InsnNode(NOP));
                //index.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false));

                InsnList list = new InsnList();
                list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(AstralLevelNerf.class), "getExpRequired", "(IJ)J", false));
                list.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false));

                method.instructions.insertBefore(start, index);
                method.instructions.insertBefore(end, list);
            }
        }
    }
}
