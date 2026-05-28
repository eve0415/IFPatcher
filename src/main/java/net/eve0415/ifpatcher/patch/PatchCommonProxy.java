package net.eve0415.ifpatcher.patch;

import com.buuz135.industrial.proxy.CommonProxy;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import net.eve0415.ifpatcher.IFPatcher;
import net.eve0415.ifpatcher.Patch;
import org.objectweb.asm.tree.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class PatchCommonProxy extends Patch {

    public PatchCommonProxy(final byte[] inputClass) {
        super(inputClass);
    }

    public static void fetchContributorsAsync() {
        final Thread thread = new Thread(() -> {
            try {
                final URL url = new URL("https://raw.githubusercontent.com/InnovativeOnlineIndustries/Industrial-Foregoing/master/contributors.json");
                final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                final StringBuilder sb = new StringBuilder();
                final char[] buf = new char[1024];
                int read;
                while ((read = reader.read(buf)) != -1) sb.append(buf, 0, read);
                reader.close();

                final JsonArray uuids = new JsonParser().parse(sb.toString()).getAsJsonObject().get("uuid").getAsJsonArray();
                final CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
                uuids.forEach(e -> list.add(e.getAsString()));
                CommonProxy.CONTRIBUTORS = list;
            } catch (final Exception e) {
                IFPatcher.LOGGER.warn("Failed to fetch contributors: {}", e.getMessage());
            }
        }, "IFPatcher-Contributors");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    protected boolean patch() {
        final boolean preInitPatched = patchPreInit();
        final boolean postInitPatched = patchPostInit();
        return preInitPatched || postInitPatched;
    }

    private boolean patchPreInit() {
        final MethodNode preInit = findMethod("preInit");
        if (preInit == null) {
            IFPatcher.LOGGER.warn("Could not find preInit method. Skipping contributors patch.");
            return false;
        }

        AbstractInsnNode jsonParserNew = null;
        AbstractInsnNode forEachCall = null;

        for (final ListIterator<AbstractInsnNode> it = preInit.instructions.iterator(); it.hasNext(); ) {
            final AbstractInsnNode insnNode = it.next();
            if (insnNode instanceof TypeInsnNode && insnNode.getOpcode() == NEW
                    && ((TypeInsnNode) insnNode).desc.equals("com/google/gson/JsonParser")) {
                jsonParserNew = insnNode;
            }
            if (jsonParserNew != null && insnNode instanceof MethodInsnNode
                    && ((MethodInsnNode) insnNode).name.equals("forEach")
                    && ((MethodInsnNode) insnNode).owner.equals("com/google/gson/JsonArray")) {
                forEachCall = insnNode;
                break;
            }
        }

        if (jsonParserNew == null || forEachCall == null) {
            IFPatcher.LOGGER.warn("Could not find contributors fetch block in preInit. Skipping.");
            return false;
        }

        final AbstractInsnNode insertBefore = forEachCall.getNext();

        final List<AbstractInsnNode> toRemove = new ArrayList<>();
        AbstractInsnNode current = jsonParserNew;
        while (current != null) {
            toRemove.add(current);
            if (current == forEachCall) break;
            current = current.getNext();
        }

        for (final AbstractInsnNode node : toRemove) {
            preInit.instructions.remove(node);
        }

        preInit.instructions.insertBefore(insertBefore,
                new MethodInsnNode(INVOKESTATIC, getName(PatchCommonProxy.class), "fetchContributorsAsync", "()V", false));

        IFPatcher.LOGGER.info("Patched preInit contributors fetch to async");
        return true;
    }

    private boolean patchPostInit() {
        AbstractInsnNode insertionPoint = null;
        final MethodNode postInit = findMethod("postInit");
        if (postInit == null) {
            IFPatcher.LOGGER.warn("Could not find postInit method. Skipping config patch.");
            return false;
        }
        final InsnList handlePostInit = postInit.instructions;

        for (final ListIterator<AbstractInsnNode> it = handlePostInit.iterator(); it.hasNext(); ) {
            final AbstractInsnNode insnNode = it.next();
            if ((insnNode instanceof MethodInsnNode) && ((MethodInsnNode) insnNode).name.equals("configuration")) {
                insertionPoint = insnNode;
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
        newInst.add(new FieldInsnNode(GETSTATIC, "com/buuz135/industrial/config/CustomConfiguration", "config", "Lnet/minecraftforge/common/config/Configuration;"));
        newInst.add(new MethodInsnNode(INVOKESTATIC, getName(PatchSludgeRefiner.class), "configuration", "(Lnet/minecraftforge/common/config/Configuration;)V", false));
        handlePostInit.insert(insertionPoint, newInst);
        IFPatcher.LOGGER.info("Patched Common Proxy");

        return true;
    }
}
