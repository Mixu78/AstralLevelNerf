package mixu.astrallevelnerf.asm;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name("AstralLevelNerfPlugin")
public class AstralLevelNerfPlugin implements IFMLLoadingPlugin {
    public static Logger logger = LogManager.getLogger("AstralLevelNerf Transformer");

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{AstralLevelNerfTransformer.class.getName()};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
