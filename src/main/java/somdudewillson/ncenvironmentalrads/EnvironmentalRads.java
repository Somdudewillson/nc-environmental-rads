package somdudewillson.ncenvironmentalrads;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import somdudewillson.ncenvironmentalrads.commands.BiomeConfigCommand;
import somdudewillson.ncenvironmentalrads.commands.BlockConfigCommand;
import somdudewillson.ncenvironmentalrads.commands.DimensionConfigCommand;
import somdudewillson.ncenvironmentalrads.proxy.CommonProxy;

@Mod(modid = EnvironmentalRads.MODID, name = EnvironmentalRads.NAME, version = EnvironmentalRads.VERSION,
		dependencies = "required-after:nuclearcraft;after:advancedrocketry;")
public class EnvironmentalRads {
    public static final String MODID = "ncenvironmentalrads";
    public static final String NAME = "Environmental Rads : NuclearCraft Addon";
    public static final String VERSION = "1.12.2-1.0.6.7";
    /* MCVERSION-MAJORMOD.MAJORAPI.MINOR.PATCH
     * 		MCVERSION
	 * Always matches the Minecraft version the mod is for.
	 * 		MAJORMOD
	 * Removing items, blocks, tile entities, etc.
	 * Changing or removing previously existing mechanics.
	 * Updating to a new Minecraft version.
	 * 		MAJORAPI
	 * Changing the order or variables of enums.
	 * Changing return types of methods.
	 * Removing public methods altogether.
	 * 		MINOR
	 * Adding items, blocks, tile entities, etc.
	 * Adding new mechanics.
	 * Deprecating public methods. (This is not a MAJORAPI increment since it doesn't break an API.)
	 * 		PATCH
	 * Bugfixes.
     */
    
    
	public static final String CLIENT_PROXY = "somdudewillson.ncenvironmentalrads.proxy.ClientProxy";
	public static final String COMMON_PROXY = "somdudewillson.ncenvironmentalrads.proxy.CommonProxy";
    
    @Instance
    public static EnvironmentalRads instance = new EnvironmentalRads();
    
	@SidedProxy(clientSide = CLIENT_PROXY, serverSide = COMMON_PROXY)
	public static CommonProxy proxy;

    public static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // some example code
    }
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent postEvent) {
		logger.info("Postinit Started...");
		proxy.setLogger(logger);
		proxy.postInit(postEvent);
	}
	
	@EventHandler
	public void serverLoad(FMLServerStartingEvent event) {
	    // register server commands
		event.registerServerCommand(new BlockConfigCommand());
		event.registerServerCommand(new BiomeConfigCommand());
		event.registerServerCommand(new DimensionConfigCommand());

		//event.registerServerCommand(new DebugCommand());
	}
}
