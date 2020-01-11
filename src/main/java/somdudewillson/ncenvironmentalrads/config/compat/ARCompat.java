package main.java.somdudewillson.ncenvironmentalrads.config.compat;

import main.java.somdudewillson.ncenvironmentalrads.EnvironmentalRads;
import main.java.somdudewillson.ncenvironmentalrads.config.NCERConfig;
import zmaster587.advancedRocketry.api.AdvancedRocketryAPI;
import zmaster587.advancedRocketry.api.IAtmosphere;
import zmaster587.advancedRocketry.api.atmosphere.AtmosphereRegister;
import zmaster587.advancedRocketry.api.dimension.solar.IGalaxy;
import zmaster587.advancedRocketry.atmosphere.AtmosphereType;

import org.apache.logging.log4j.Logger;

public class ARCompat implements ICompatConfigLoader {
	public void updateConfig() {
		Logger log = EnvironmentalRads.logger;
		
		//-----Register atmosphere types
		AtmosphereRegister register = AtmosphereRegister.getInstance();
		//Static
		if (!NCERConfig.arSettings.atmosphere_type_absorption.containsKey(AtmosphereType.AIR.getUnlocalizedName())) {
			NCERConfig.arSettings.atmosphere_type_absorption.put(AtmosphereType.AIR.getUnlocalizedName(), -1.0);
		}
		if (!NCERConfig.arSettings.atmosphere_type_absorption.containsKey(AtmosphereType.PRESSURIZEDAIR.getUnlocalizedName())) {
			NCERConfig.arSettings.atmosphere_type_absorption.put(AtmosphereType.PRESSURIZEDAIR.getUnlocalizedName(), -1.0);
		}
		//Dynamic
		for (IAtmosphere type : register.getAtmosphereList()) {
			String key = type.getUnlocalizedName();
			
		    if (!NCERConfig.arSettings.atmosphere_type_absorption.containsKey(key)) {
		    	NCERConfig.arSettings.atmosphere_type_absorption.put(key, 0.0);
		    }
		}
		//-----
		
		//-----Register planet dimensions
		IGalaxy galaxy = AdvancedRocketryAPI.dimensionManager;
		
	    log.info("Auto-Detecting Advanced Rocketry Dimensions...");
		for (Integer dimID : galaxy.getRegisteredDimensions()) {
			String key = galaxy.getDimensionProperties(dimID).getName();
		    
		    log.info("Detected Dimension: "+key);
		    
		    //-----Settings which apply to all radiation sources
		    if (!NCERConfig.dimSpecific.environmental_radiation_enabled.containsKey(key)) {
		    	NCERConfig.dimSpecific.environmental_radiation_enabled.put(key, false);
		    }
		    if (!NCERConfig.dimSpecific.use_atmospheric_absorption.containsKey(key)) {
		    	NCERConfig.dimSpecific.use_atmospheric_absorption.put(key, false);
		    }
		    if (!NCERConfig.dimSpecific.atmospheric_absorption_thickness.containsKey(key)) {
		    	NCERConfig.dimSpecific.atmospheric_absorption_thickness.put(key, new Integer(255));
		    }
		    //-----
		    
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
	    log.info("Advanced Rocketry Dimension Auto-Detecting Done.");
	    
		NCERConfig.updateAirAbsorption();
		//-----
	}
}