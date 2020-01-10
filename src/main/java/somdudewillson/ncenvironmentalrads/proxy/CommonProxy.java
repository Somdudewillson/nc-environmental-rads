package main.java.somdudewillson.ncenvironmentalrads.proxy;

import it.unimi.dsi.fastutil.ints.IntSortedSet;

import java.util.Map.Entry;

import main.java.somdudewillson.ncenvironmentalrads.EnvironmentalRads;
import main.java.somdudewillson.ncenvironmentalrads.config.NCERConfig;
import main.java.somdudewillson.ncenvironmentalrads.radiation.EnvironmentalRadiationHandler;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

public class CommonProxy {
	
	public void postInit(FMLPostInitializationEvent postEvent) {
		MinecraftForge.EVENT_BUS.register(new EnvironmentalRadiationHandler());
		
		//Register dimensions in config
		for (Entry<DimensionType, IntSortedSet> entry : DimensionManager.getRegisteredDimensions().entrySet()) {
		    DimensionType key = entry.getKey();
		    
		    System.out.println(key.getName());
		    
		    //==========Settings which apply to all radiation sources
		    if (!NCERConfig.dimSpecific.environmental_radiation_enabled.containsKey(key.getName())) {
		    	NCERConfig.dimSpecific.environmental_radiation_enabled.put(key.getName(), false);
		    }
		    if (!NCERConfig.dimSpecific.use_atmospheric_absorption.containsKey(key.getName())) {
		    	NCERConfig.dimSpecific.use_atmospheric_absorption.put(key.getName(), false);
		    }
		    if (!NCERConfig.dimSpecific.atmospheric_absorption_thickness.containsKey(key.getName())) {
		    	NCERConfig.dimSpecific.atmospheric_absorption_thickness.put(key.getName(), new Integer(255));
		    }
		    
		    //-----Sky-specific settings
		    if (!NCERConfig.dimSpecific.sky_radiation.containsKey(key.getName())) {
		    	NCERConfig.dimSpecific.sky_radiation.put(key.getName(), false);
		    }
		    if (!NCERConfig.dimSpecific.sky_max_rads.containsKey(key.getName())) {
		    	NCERConfig.dimSpecific.sky_max_rads.put(key.getName(), new Double(0));
		    }
		    if (!NCERConfig.dimSpecific.sky_origin_height.containsKey(key.getName())) {
		    	NCERConfig.dimSpecific.sky_origin_height.put(key.getName(), new Integer(255));
		    }
		    //-----
		    
		    //-----Bedrock-specific settings
		    if (!NCERConfig.dimSpecific.bedrock_radiation.containsKey(key.getName())) {
		    	NCERConfig.dimSpecific.bedrock_radiation.put(key.getName(), false);
		    }
		    if (!NCERConfig.dimSpecific.bedrock_max_rads.containsKey(key.getName())) {
		    	NCERConfig.dimSpecific.bedrock_max_rads.put(key.getName(), new Double(0));
		    }
		    if (!NCERConfig.dimSpecific.bedrock_origin_height.containsKey(key.getName())) {
		    	NCERConfig.dimSpecific.bedrock_origin_height.put(key.getName(), new Integer(0));
		    }
		    //-----
		}
		NCERConfig.updateAirAbsorption();
		ConfigManager.sync(EnvironmentalRads.MODID, Config.Type.INSTANCE);
	}
}