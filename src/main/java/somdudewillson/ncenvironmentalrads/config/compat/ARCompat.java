package somdudewillson.ncenvironmentalrads.config.compat;

import org.apache.logging.log4j.Logger;

import somdudewillson.ncenvironmentalrads.EnvironmentalRads;
import somdudewillson.ncenvironmentalrads.config.NCERConfig;
import somdudewillson.ncenvironmentalrads.proxy.CommonProxy;
import zmaster587.advancedRocketry.api.IAtmosphere;
import zmaster587.advancedRocketry.api.atmosphere.AtmosphereRegister;
import zmaster587.advancedRocketry.api.dimension.solar.IGalaxy;
import zmaster587.advancedRocketry.atmosphere.AtmosphereType;
import zmaster587.advancedRocketry.dimension.DimensionManager;

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
		IGalaxy galaxy = DimensionManager.getInstance();
		
	    log.info("Auto-Detecting Advanced Rocketry Dimensions...");
		for (Integer dimID : galaxy.getRegisteredDimensions()) {
			String key = galaxy.getDimensionProperties(dimID).getName();
		    
		    log.info("Detected Dimension: "+key);
		    
		    CommonProxy.helper.tryAddNewDimension(key);
		}
	    log.info("Advanced Rocketry Dimension Auto-Detecting Done.");
	    
		NCERConfig.updateAirAbsorption();
		//-----
	}
}