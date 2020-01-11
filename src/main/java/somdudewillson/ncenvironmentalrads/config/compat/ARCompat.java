package main.java.somdudewillson.ncenvironmentalrads.config.compat;

import main.java.somdudewillson.ncenvironmentalrads.config.NCERConfig;
import zmaster587.advancedRocketry.api.IAtmosphere;
import zmaster587.advancedRocketry.api.atmosphere.AtmosphereRegister;
import zmaster587.advancedRocketry.atmosphere.AtmosphereType;

public class ARCompat implements ICompatConfigLoader {
	public void updateConfig() {
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
	}
}