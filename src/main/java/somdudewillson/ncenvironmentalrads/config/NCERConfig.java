package main.java.somdudewillson.ncenvironmentalrads.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import main.java.somdudewillson.ncenvironmentalrads.EnvironmentalRads;
import nc.config.NCConfig;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = EnvironmentalRads.MODID, name = "NCEnvironmentalRads")
public class NCERConfig {
	@Config.Comment("Dimension-Specific Settings.")
	@Config.Name("dimension-specific")
	public static DimSettings dimSpecific = new DimSettings();
	
	@Config.Comment("Advanced Rocketry Compatibility Settings.")
	@Config.Name("ar-compat")
	public static ARSettings arSettings = new ARSettings();
	
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
			"If set to false, all atmospheric falloff settings will be ignored."})
		public final Map<String, Boolean> use_atmospheric_absorption = new HashMap<>();
		
		@Config.Comment({"The thickness (in m) of airblocks needed to reduce environmental radiation to 0.",
			"Will be ignored if atmospheric absorption is disabled."})
		public final Map<String, Integer> atmospheric_absorption_thickness = new HashMap<>();
		
		//-----Sky-related settings
		@Config.Comment({"If dimensions will simulate radiation originating from the sky.",
			"If set to false, all sky-related settings will be ignored."})
		public final Map<String, Boolean> sky_radiation = new HashMap<>();
		
		@Config.Comment({"The maximum rads/t from the sky.",
			"ALWAYS used if atmospheric absorption is enabled, as part of the calculation for how much radiation air blocks absorb."})
		public final Map<String, Double> sky_max_rads = new HashMap<>();

		@Config.Comment({"The height at which sky-sourced radiation is at its full value.",
			"Will be ignored if radiation from the sky is disabled."})
		public final Map<String, Integer> sky_origin_height = new HashMap<>();
		//-----
		
		//-----Bedrock-related Settings
		@Config.Comment({"If dimensions will simulate radiation originating from the bedrock.",
			"If set to false, all bedrock-related settings will be ignored."})
		public final Map<String, Boolean> bedrock_radiation = new HashMap<>();
		
		@Config.Comment({"The maximum rads/t from the bedrock.",
			"Will be ignored if radiation from the bedrock is disabled."})
		public final Map<String, Double> bedrock_max_rads = new HashMap<>();
	
		@Config.Comment({"The height at which sky-sourced radiation is at its full value.",
			"Will be ignored if radiation from the bedrock is disabled."})
		public final Map<String, Integer> bedrock_origin_height = new HashMap<>();
		//-----
		
		@Config.Ignore
		public final Map<String, Double> air_absorption = new HashMap<>();
	}
	
	//Advanced Rocketry Compat Settings
	public static class ARSettings {
		@Config.Comment({"If sky radiation should be calculated from the distance from the star.",
		"If set to true, all relevant per-dimension radiation amount settings will be overridden.",
		"If set to false, Advanced Rocketry dimensions will use overworld radiation settings."})
		public boolean solar_radiation_origin = false;
		
		@Config.Comment({"How much the automatic radiation from stars should be scaled.",
		"1.0 = 100%"})
		public double solar_radiation_scale = 1.0;
		
		@Config.Comment({"How many samples should be used to calculate the radiation amount from stars.",
		"Highly recommended to not reduce below 100."})
		public int solar_radiation_sampling = 1000;
		
		@Config.Comment({"How much the automatic radiation from black holes should be scaled.",
		"1.0 = 100%"})
		public double accretion_radiation_scale = 1.0;
		
		@Config.Comment({"If atmospheric density curves or flat planetary atmospheric density should be used."})
		public boolean use_atmosphere_density_curve = false;
		
		@Config.Comment({"Absorption % per block of air at 1 atm for each atmosphere type.",
			"-1 will use calculated values for the overworld."})
		public final Map<String, Double> atmosphere_type_absorption = new HashMap<>();
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
		    
	    	double top_rads = NCERConfig.dimSpecific.sky_max_rads.get(key);
			double bottom_rads = NCConfig.radiation_lowest_rate;
			int thickness = NCERConfig.dimSpecific.atmospheric_absorption_thickness.get(key);
			
			double air_absorption = 1-Math.pow((bottom_rads/top_rads), 1.0/thickness);
			
			NCERConfig.dimSpecific.air_absorption.put(key, air_absorption);
		}
		
	}
}
