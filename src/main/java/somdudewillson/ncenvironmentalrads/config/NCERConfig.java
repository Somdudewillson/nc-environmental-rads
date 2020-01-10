package main.java.somdudewillson.ncenvironmentalrads.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import nc.config.NCConfig;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import main.java.somdudewillson.ncenvironmentalrads.EnvironmentalRads;

@Config(modid = EnvironmentalRads.MODID, name = "NCEnvironmentalRads")
public class NCERConfig {
	@Config.Comment("Dimension-Specific Settings.")
	@Config.Name("dimension-specific")
	public static DimSettings dimSpecific = new DimSettings();
	
	//Global Settings
	@Config.Comment("Percentage of incoming radiation that a block will absorb per unit of hardness it has.")
	@Config.Name("% absorption per hardness")
	public static double percent_absorbed_per_hardness = 0.01;
	
	//Dimension-Specific Settings
	public static class DimSettings {
		@Config.Comment({"If dimensions will have environmental radiation.",
		"If set to false, all other values will be ignored."})
		public final Map<String, Boolean> environmental_radiation_enabled = new HashMap<>();
		
		@Config.Comment({"If dimensions will simulate the absorption of radiation by an atmosphere.",
			"If set to false, bottom_rads, top_height, and bottom_height will be ignored."})
		public final Map<String, Boolean> use_atmospheric_absorption = new HashMap<>();

		@Config.Comment({"The height at which environmental radiation is at its highest value.",
			"Will be ignored if atmospheric absorption is disabled."})
		public final Map<String, Integer> top_height = new HashMap<>();

		@Config.Comment({"The height at which environmental radiation is at its lowest value.",
			"Will be ignored if atmospheric absorption is disabled."})
		public final Map<String, Integer> bottom_height = new HashMap<>();

		@Config.Comment({"The maximum rads/t."})
		public final Map<String, Double> top_rads = new HashMap<>();

		@Config.Comment({"The minimum rads/t.",
			"Will be ignored if atmospheric absorption is disabled.",
			"Set to 0 to use NuclearCraft's configured minimum rads/t value."})
		public final Map<String, Double> bottom_rads = new HashMap<>();

		@Config.Ignore
		public final Map<String, Double> air_absorption = new HashMap<>();
	}


	@Mod.EventBusSubscriber(modid = EnvironmentalRads.MODID)
	private static class EventHandler {

		/**
		 * Inject the new values and save to the config file when the config has been changed from the GUI.
		 *
		 * @param event The event
		 */
		@SubscribeEvent
		public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
			if (event.getModID().equals(EnvironmentalRads.MODID)) {
				updateAirAbsorption();
				ConfigManager.sync(EnvironmentalRads.MODID, Config.Type.INSTANCE);
			}
		}
	}
	
	public static void updateAirAbsorption() {
		
		for (Entry<String, Boolean> entry : dimSpecific.environmental_radiation_enabled.entrySet()) {
			String key = entry.getKey();
			Boolean value = entry.getValue();
		    
		    if (value) {
		    	double top_rads = NCERConfig.dimSpecific.top_rads.get(key);
				double bottom_rads = NCERConfig.dimSpecific.bottom_rads.get(key) == 0.0 ? NCConfig.radiation_lowest_rate : NCERConfig.dimSpecific.bottom_rads.get(key);
				int top_height = NCERConfig.dimSpecific.top_height.get(key);
				int bottom_height = NCERConfig.dimSpecific.bottom_height.get(key);
				
				double air_absorption = 1-Math.pow((bottom_rads/top_rads), 1.0/(top_height-bottom_height));
				
				NCERConfig.dimSpecific.air_absorption.put(key, air_absorption);
		    }
		}
		
	}
}
