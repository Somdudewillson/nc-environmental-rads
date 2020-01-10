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
		    
		    if (!NCERConfig.dimSpecific.environmental_radiation_enabled.containsKey(key.getName())) {
		    	NCERConfig.dimSpecific.environmental_radiation_enabled.put(key.getName(), false);
		    }
		    if (!NCERConfig.dimSpecific.use_atmospheric_absorption.containsKey(key.getName())) {
		    	NCERConfig.dimSpecific.use_atmospheric_absorption.put(key.getName(), false);
		    }
		    
		    if (!NCERConfig.dimSpecific.top_height.containsKey(key.getName())) {
		    	NCERConfig.dimSpecific.top_height.put(key.getName(), new Integer(255));
		    }
		    if (!NCERConfig.dimSpecific.bottom_height.containsKey(key.getName())) {
		    	NCERConfig.dimSpecific.bottom_height.put(key.getName(), new Integer(0));
		    }
		    
		    if (!NCERConfig.dimSpecific.top_rads.containsKey(key.getName())) {
		    	NCERConfig.dimSpecific.top_rads.put(key.getName(), new Double(0));
		    }
		    if (!NCERConfig.dimSpecific.bottom_rads.containsKey(key.getName())) {
		    	NCERConfig.dimSpecific.bottom_rads.put(key.getName(), new Double(0));
		    }
		}
		NCERConfig.updateAirAbsorption();
		ConfigManager.sync(EnvironmentalRads.MODID, Config.Type.INSTANCE);
	}
}