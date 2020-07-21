package somdudewillson.ncenvironmentalrads.radiation.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import it.unimi.dsi.fastutil.ints.IntSortedSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import somdudewillson.ncenvironmentalrads.EnvironmentalRads;
import somdudewillson.ncenvironmentalrads.config.NCERConfig;
import somdudewillson.ncenvironmentalrads.utils.NameUtils;

public interface IEnvironmentalRadiationHelper {	
	public default double getAdjustedRadsFromSky(double dimRads, String biomeKey) {
		
		double finalRads = dimRads;
		//If biome radiation modifiers are enabled for current biome
		if (NCERConfig.biomeSpecific.biome_effects_enabled.get(biomeKey)) {
			if (NCERConfig.biomeSpecific.sky_shift.get(biomeKey)>0) {
				finalRads *= NCERConfig.biomeSpecific.sky_multiplier.get(biomeKey);
				finalRads += NCERConfig.biomeSpecific.sky_shift.get(biomeKey);
			} else {
				finalRads += NCERConfig.biomeSpecific.sky_shift.get(biomeKey);
				finalRads *= NCERConfig.biomeSpecific.sky_multiplier.get(biomeKey);
			}
		}
		
		return Math.max(finalRads, 0);
	}
	public default double getAdjustedRadsFromBedrock(double dimRads, String biomeKey) {
		
		double finalRads = dimRads;
		//If biome radiation modifiers are enabled for current biome
		if (NCERConfig.biomeSpecific.biome_effects_enabled.get(biomeKey)) {
			if (NCERConfig.biomeSpecific.bedrock_shift.get(biomeKey)>0) {
				finalRads *= NCERConfig.biomeSpecific.bedrock_multiplier.get(biomeKey);
				finalRads += NCERConfig.biomeSpecific.bedrock_shift.get(biomeKey);
			} else {
				finalRads += NCERConfig.biomeSpecific.bedrock_shift.get(biomeKey);
				finalRads *= NCERConfig.biomeSpecific.bedrock_multiplier.get(biomeKey);
			}
		}
		
		return Math.max(finalRads, 0);
	}
	
	public double projectRadsFromPoint(BlockPos pos, EnumFacing direction, double sourceRads,
			World world, BlockPos targetPos,
			double air_absorption, double bottom_rads);
	
	public double getRadsFromSky(BlockPos pos, World world, String dimKey, String biomeKey);
	public double getRadsFromBedrock(BlockPos pos, World world, String dimKey, String biomeKey);
	
	public default Iterable<String> getDimensionKeys() {
		List<String> dimKeys = new ArrayList<>();
		
		for (Entry<DimensionType, IntSortedSet> entry : DimensionManager.getRegisteredDimensions().entrySet()) {
		    String key = entry.getKey().getName();
		    
		    dimKeys.add(key);
		}
		
		return dimKeys;
	}
	
	public String getDimensionKey(World world);
	public default boolean tryAddNewDimension(World world) {
		return this.tryAddNewDimension(this.getDimensionKey(world));
	}
	public default boolean tryAddNewDimension(String key) {
		if (key.length() < 1) { return false;}
		
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
		NCERConfig.updateAirAbsorption();
		ConfigManager.sync(EnvironmentalRads.MODID, Config.Type.INSTANCE);
		
		return true;
	}
	public default boolean tryAddNewBiome(World world, BlockPos pos) {
		return this.tryAddNewBiome(world.getBiome(pos).getRegistryName().toString());
	}
	public default boolean tryAddNewBiome(String key) {
		if (key.length() < 1) { return false;}
		
	    //==========Settings which apply to all radiation sources
	    if (!NCERConfig.biomeSpecific.biome_effects_enabled.containsKey(key)) {
	    	NCERConfig.biomeSpecific.biome_effects_enabled.put(key, false);
	    }
	    
	    //-----Sky-specific settings
	    if (!NCERConfig.biomeSpecific.sky_multiplier.containsKey(key)) {
	    	NCERConfig.biomeSpecific.sky_multiplier.put(key, new Double(1));
	    }
	    if (!NCERConfig.biomeSpecific.sky_shift.containsKey(key)) {
	    	NCERConfig.biomeSpecific.sky_shift.put(key, new Double(0));
	    }
	    //-----
	    
	    //-----Bedrock-specific settings
	    if (!NCERConfig.biomeSpecific.bedrock_multiplier.containsKey(key)) {
	    	NCERConfig.biomeSpecific.bedrock_multiplier.put(key, new Double(1));
	    }
	    if (!NCERConfig.biomeSpecific.bedrock_shift.containsKey(key)) {
	    	NCERConfig.biomeSpecific.bedrock_shift.put(key, new Double(0));
	    }
	    //-----
	    
		ConfigManager.sync(EnvironmentalRads.MODID, Config.Type.INSTANCE);
		
		return true;
	}
	
	public default double getRadsThroughBlock(double startRads, IBlockState blockState,
			World world, BlockPos pos) {
		String blockKey = NameUtils.getBlockKey(world, blockState);
		double absorptionPercent;
		
		if (NCERConfig.blockSettings.rad_absorption.containsKey(blockKey)) {
			absorptionPercent = NCERConfig.blockSettings.rad_absorption.get(blockKey);
		} else {
			absorptionPercent = blockState.getBlockHardness(world, pos)*NCERConfig.percent_absorbed_per_hardness;
		}
		
//		System.out.println(blockKey+" absorbed "
//				+(absorptionPercent*100)+"% of "
//				+startRads+" resulting in "+
//				(startRads * (1-absorptionPercent))+" final rads.");
		
		return startRads * (1-absorptionPercent);
	}
}
