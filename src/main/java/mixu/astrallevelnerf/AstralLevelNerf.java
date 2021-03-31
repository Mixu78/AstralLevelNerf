package mixu.astrallevelnerf;

import com.fathzer.soft.javaluator.DoubleEvaluator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Logger;


@Mod(
        modid = AstralLevelNerf.MODID,
        name = AstralLevelNerf.MODNAME,
        version = AstralLevelNerf.VERSION,
        dependencies = "required-after:astralsorcery",
        useMetadata = true
)
public class AstralLevelNerf {

    public static final String MODID = "astral-level-nerf";
    public static final String MODNAME = "Astral Level Nerf";
    public static final String VERSION = "1.0.0";

    public static Logger logger;

    private static final DoubleEvaluator evaluator = new DoubleEvaluator();

    /**
     * This is the instance of your mod as created by Forge. It will never be null.
     */
    @Mod.Instance(MODID)
    public static AstralLevelNerf INSTANCE;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        logger = event.getModLog();
    }

    @SubscribeEvent
    public void onConfigChanged(OnConfigChangedEvent event) {
        if (event.getModID().equals(MODID)) {
            ConfigManager.sync(MODID, Type.INSTANCE);
        }
    }

    @SuppressWarnings("unused")
    public static long getExpRequired(int i, long prev) {
        String formula = AstralLevelNerfConfig.levelFormula.replaceAll("i", Integer.toString(i));
        String replacedFormula = formula
                .replaceAll("i", Integer.toString(i))
                .replaceAll("prev", Double.toString(prev));

        try {
            return evaluator.evaluate(replacedFormula).longValue();
        } catch (IllegalArgumentException e) {
            logger.error("Formula is invalid!");
            return Long.MAX_VALUE - i;
        } catch (Exception e) {
            logger.error(e);
            return Long.MAX_VALUE - i;
        }
    }

    @Config(modid = AstralLevelNerf.MODID, name = MODID)
    public static class AstralLevelNerfConfig {
        @Config.Name("level_formula")
        @Config.Comment(
            "The formula to use when calculating xp required." +
            "You can use \"i\" for the current level and \"prev\" for the previous levels xp"
        )
        public static String levelFormula = "prev + 150 + floor(2^(i/2+3))";
    }
}
