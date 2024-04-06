package net.eve0415.ifpatcher;

import net.eve0415.ifpatcher.internal.Reference;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

@MCVersion("1.12.2")
@IFMLLoadingPlugin.SortingIndex(1001)
@Mod(
        modid = net.eve0415.ifpatcher.internal.Reference.MODID,
        name = net.eve0415.ifpatcher.internal.Reference.NAME,
        version = net.eve0415.ifpatcher.internal.Reference.VERSION,
        dependencies = net.eve0415.ifpatcher.internal.Reference.DEPENDENCIES,
        certificateFingerprint = net.eve0415.ifpatcher.internal.Reference.FINGERPRINT,
        updateJSON = Reference.UPDATEJSON,
        acceptableRemoteVersions = "*"
)
public class IFPatcher implements IFMLLoadingPlugin {
    public static final Logger LOGGER = LogManager.getLogger("IFPatcher");

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{"net.eve0415.ifpatcher.Transformer"};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(final Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
