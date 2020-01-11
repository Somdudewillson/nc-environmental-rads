package main.java.somdudewillson.ncenvironmentalrads.proxy;

import it.unimi.dsi.fastutil.ints.IntSortedSet;

import java.util.Map.Entry;

import org.apache.logging.log4j.Logger;

import main.java.somdudewillson.ncenvironmentalrads.EnvironmentalRads;
import main.java.somdudewillson.ncenvironmentalrads.config.NCERConfig;
import main.java.somdudewillson.ncenvironmentalrads.config.compat.ICompatConfigLoader;
import main.java.somdudewillson.ncenvironmentalrads.radiation.EnvironmentalRadiationHandler;
import main.java.somdudewillson.ncenvironmentalrads.radiation.helpers.DefaultEnvironmentalRadiationHelper;
import main.java.somdudewillson.ncenvironmentalrads.radiation.helpers.IEnvironmentalRadiationHelper;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

public class CommonProxy {
	private Logger log = null;
	
	public void postInit(FMLPostInitializationEvent postEvent) {
		IEnvironmentalRadiationHelper helper = new DefaultEnvironmentalRadiationHelper();
		
		//Register dimensions in config
		updateDimensions();
		
		//-----Compatibilities
		ICompatConfigLoader loader;
		try {
			//Advanced Rocketry
			if (Loader.isModLoaded("advancedrocketry")) {
				log.info("Advanced Rocketry Found, Loading Compat...");
				
				log.info("Loading Advanced Rocketry Compat Config");
				loader = Class.forName("main.java.somdudewillson.ncenvironmentalrads.config.compat.ARCompat").asSubclass(ICompatConfigLoader.class).newInstance();
				loader.updateConfig();
				
				log.info("Switching to Advanced Rocketry Compat RadiationHelper");
				helper = Class.forName("main.java.somdudewillson.ncenvironmentalrads.radiation.helpers.AREnvironmentalRadiationHelper").asSubclass(IEnvironmentalRadiationHelper.class).newInstance();

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
		MinecraftForge.EVENT_BUS.register(new EnvironmentalRadiationHandler(helper));
	}
	
	private void updateDimensions() {
	    log.info("Auto-Detecting Dimensions...");
		for (Entry<DimensionType, IntSortedSet> entry : DimensionManager.getRegisteredDimensions().entrySet()) {
		    String key = entry.getKey().getName();
		    
		    log.info("Detected Dimension: "+key);
		    
		    //==========Settings which apply to all radiation sources
		    if (!NCERConfig.dimSpecific.environmental_radiation_enabled.containsKey(key)) {
		    	NCERConfig.dimSpecific.environmental_radiation_enabled.put(key, false);
		    }
		    if (!NCERConfig.dimSpecific.use_atmospheric_absorption.containsKey(key)) {
		    	NCERConfig.dimSpecific.use_atmospheric_absorption.put(key, false);
		    }
		    if (!NCERConfig.dimSpecific.atmospheric_absorption_thickness.containsKey(key)) {
		    	NCERConfig.dimSpecific.atmospheric_absorption_thickness.put(key, new Integer(255));
		    }
		    
		    //-----Sky-specific settings
		    if (!NCERConfig.dimSpecific.sky_radiation.containsKey(key)) {
		    	NCERConfig.dimSpecific.sky_radiation.put(key, false);
		    }
		    if (!NCERConfig.dimSpecific.sky_max_rads.containsKey(key)) {
		    	NCERConfig.dimSpecific.sky_max_rads.put(key, new Double(0));
		    }
		    if (!NCERConfig.dimSpecific.sky_origin_height.containsKey(key)) {
		    	NCERConfig.dimSpecific.sky_origin_height.put(key, new Integer(255));
		    }
		    //-----
		    
		    //-----Bedrock-specific settings
		    if (!NCERConfig.dimSpecific.bedrock_radiation.containsKey(key)) {
		    	NCERConfig.dimSpecific.bedrock_radiation.put(key, false);
		    }
		    if (!NCERConfig.dimSpecific.bedrock_max_rads.containsKey(key)) {
		    	NCERConfig.dimSpecific.bedrock_max_rads.put(key, new Double(0));
		    }
		    if (!NCERConfig.dimSpecific.bedrock_origin_height.containsKey(key)) {
		    	NCERConfig.dimSpecific.bedrock_origin_height.put(key, new Integer(0));
		    }
		    //-----
		}
	    log.info("Dimension Auto-Detecting Done.");
		NCERConfig.updateAirAbsorption();
	}

	public void setLogger(Logger logger) {
		this.log = logger;
	}

}