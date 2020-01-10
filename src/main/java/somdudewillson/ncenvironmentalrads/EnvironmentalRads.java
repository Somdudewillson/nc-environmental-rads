package main.java.somdudewillson.ncenvironmentalrads;

import main.java.somdudewillson.ncenvironmentalrads.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import org.apache.logging.log4j.Logger;

@Mod(modid = EnvironmentalRads.MODID, name = EnvironmentalRads.NAME, version = EnvironmentalRads.VERSION)
public class EnvironmentalRads
{
    public static final String MODID = "ncenvironmentalrads";
    public static final String NAME = "Environmental Rads : NuclearCraft Addon";
    public static final String VERSION = "1.12.2-1.0.0.0";

	public static final String CLIENT_PROXY = "main.java.somdudewillson.ncenvironmentalrads.proxy.ClientProxy";
	public static final String COMMON_PROXY = "main.java.somdudewillson.ncenvironmentalrads.proxy.CommonProxy";
    
    @Instance
    public static EnvironmentalRads instance = new EnvironmentalRads();
    
	@SidedProxy(clientSide = CLIENT_PROXY, serverSide = COMMON_PROXY)
	public static CommonProxy proxy;

    private static Logger logger;

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
		proxy.postInit(postEvent);
	}
}