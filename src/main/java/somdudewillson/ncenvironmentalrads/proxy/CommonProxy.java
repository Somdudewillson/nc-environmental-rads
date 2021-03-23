package somdudewillson.ncenvironmentalrads.proxy;

import java.util.Map.Entry;

import org.apache.logging.log4j.Logger;

import it.unimi.dsi.fastutil.ints.IntSortedSet;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import somdudewillson.ncenvironmentalrads.EnvironmentalRads;
import somdudewillson.ncenvironmentalrads.config.NCERConfig;
import somdudewillson.ncenvironmentalrads.config.compat.ICompatConfigLoader;
import somdudewillson.ncenvironmentalrads.radiation.PlayerEnvironmentalRadiationHandler;
import somdudewillson.ncenvironmentalrads.radiation.helpers.DefaultEnvironmentalRadiationHelper;
import somdudewillson.ncenvironmentalrads.radiation.helpers.IEnvironmentalRadiationHelper;

public class CommonProxy {
	private Logger log = null;
	public static IEnvironmentalRadiationHelper helper;
	
	public void postInit(FMLPostInitializationEvent postEvent) {
		helper = new DefaultEnvironmentalRadiationHelper();
		
		//Register dimensions in config
		updateDimensions();
		updateBiomes();
		
		//-----Compatibilities
		ICompatConfigLoader loader;
		try {
			//Advanced Rocketry
			if (Loader.isModLoaded("advancedrocketry")) {
				log.info("Advanced Rocketry Found, Loading Compat...");
				
				log.info("Switching to Advanced Rocketry Compat RadiationHelper");
				helper = Class.forName("somdudewillson.ncenvironmentalrads.radiation.helpers.AREnvironmentalRadiationHelper").asSubclass(IEnvironmentalRadiationHelper.class).newInstance();
				
				log.info("Loading Advanced Rocketry Compat Config");
				loader = Class.forName("somdudewillson.ncenvironmentalrads.config.compat.ARCompat").asSubclass(ICompatConfigLoader.class).newInstance();
				loader.updateConfig();

				log.info("Advanced Rocketry Compat Loaded.");
			} else {
				log.info("Advanced Rocketry not found, skipping compat load.");
			}
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e) {
			e.printStackTrace();
		}
		//-----
		
		ConfigManager.sync(EnvironmentalRads.MODID, Config.Type.INSTANCE);
		
		//Register handler
		MinecraftForge.EVENT_BUS.register(new PlayerEnvironmentalRadiationHandler(helper));
	}
	
	private void updateDimensions() {
	    log.info("Auto-Detecting Dimensions...");
		for (Entry<DimensionType, IntSortedSet> entry : DimensionManager.getRegisteredDimensions().entrySet()) {
		    String key = entry.getKey().getName();
		    
		    log.info("Detected Dimension: "+key);
		    
		    helper.tryAddNewDimension(key);
		}
	    log.info("Dimension Auto-Detecting Done.");
		NCERConfig.updateAirAbsorption();
	}
	
	private void updateBiomes() {
		log.info("Auto-Detecting Biomes...");
		for (Biome entry : ForgeRegistries.BIOMES.getValuesCollection()) {
		    String key = entry.getRegistryName().toString();
		    
		    log.info("Detected Biome: "+key);
		    
		    helper.tryAddNewBiome(key);
		}
	    log.info("Biome Auto-Detecting Done.");
	}

	public void setLogger(Logger logger) {
		this.log = logger;
	}

}