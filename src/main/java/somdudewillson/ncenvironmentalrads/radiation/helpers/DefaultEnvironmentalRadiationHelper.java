package main.java.somdudewillson.ncenvironmentalrads.radiation.helpers;

import org.apache.logging.log4j.Logger;

import nc.config.NCConfig;
import net.minecraft.block.material.Material;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import main.java.somdudewillson.ncenvironmentalrads.EnvironmentalRads;
import main.java.somdudewillson.ncenvironmentalrads.config.NCERConfig;

public class DefaultEnvironmentalRadiationHelper implements
		IEnvironmentalRadiationHelper {
	
	private static final double percent_absorbed_per_hardness = NCERConfig.percent_absorbed_per_hardness;
	
	private Logger log = EnvironmentalRads.logger;

	@Override
	public double getRadsFromSky(BlockPos pos, World world, String dimKey) {
		double top_rads = NCERConfig.dimSpecific.sky_max_rads.get(dimKey);
		int top_height = NCERConfig.dimSpecific.sky_origin_height.get(dimKey);
		
		//If player is above the top height, we know environmental rads are at max
		if (pos.getY() >= top_height) {return top_rads; }
		
		int bottom_height = top_height-NCERConfig.dimSpecific.atmospheric_absorption_thickness.get(dimKey);		
		
		//If player is at or below the bottom height, we know environmental rads are 0
		if (pos.getY() <= bottom_height) {return 0.0; }
		
		double air_absorption = NCERConfig.dimSpecific.use_atmospheric_absorption.get(dimKey) ? 
				NCERConfig.dimSpecific.air_absorption.get(dimKey) :
				0.0;
		
		air_absorption = Math.max(air_absorption,0.0);
		
		//If player has LoS to the sky, no occlusion checks are necessary
		if (world.canSeeSky(pos)) {
			return top_rads * Math.pow((1-air_absorption), (top_height-pos.getY()));
		}
		
		double bottom_rads = NCConfig.radiation_lowest_rate;
		
		//If the player lacks LoS to the sky, loop down from the lowest block that does
		//and compute absorbed radiation from block hardnesses
		int startingHeight = world.getHeight(pos.getX(), pos.getZ());
		int airLayers = Math.max((top_height-startingHeight),0);
		double remainingRads = top_rads * Math.pow((1-air_absorption), airLayers);
		BlockPos projectPos = new BlockPos(pos.getX(), startingHeight-1, pos.getZ());
		
		double rads = projectRadsFromPoint(projectPos, EnumFacing.DOWN, remainingRads,
				world, pos,
				air_absorption, bottom_rads);
		
		return rads;
	}

	@Override
	public double getRadsFromBedrock(BlockPos pos, World world, String dimKey) {
		double top_rads = NCERConfig.dimSpecific.bedrock_max_rads.get(dimKey);
		int bottom_height = NCERConfig.dimSpecific.bedrock_origin_height.get(dimKey);
		
		//If player is at or below the bottom height, we know environmental rads are at max
		if (pos.getY() <= bottom_height) {return top_rads; }
		
		double air_absorption = NCERConfig.dimSpecific.use_atmospheric_absorption.get(dimKey) ? 
				NCERConfig.dimSpecific.air_absorption.get(dimKey) :
				0.0;
		
		double bottom_rads = NCConfig.radiation_lowest_rate;
		
		//Loop up from the lowest block
		//and compute absorbed radiation from block hardnesses
		BlockPos projectPos = new BlockPos(pos.getX(), bottom_height, pos.getZ());
		double rads = projectRadsFromPoint(projectPos, EnumFacing.UP, top_rads,
				world, pos,
				air_absorption, bottom_rads);
		
		return rads;
	}

	@Override
	public double projectRadsFromPoint(BlockPos pos, EnumFacing direction,
			double sourceRads, World world, BlockPos targetPos,
			double air_absorption, double bottom_rads) {
		
		//-----Calculate initial variable values
		int startingHeight = pos.getY();
		double rads = sourceRads;
		Chunk thisChunk = world.getChunkFromBlockCoords(pos);
		//-----
		
		String debugStack = "\n----------\n";
		debugStack += "Direction: "+direction+"\n";
		debugStack += "Top block: "+startingHeight+"\n";
		debugStack += "Initial rads: "+rads+"\n";
		
		for (BlockPos testPos = pos;
				testPos.getY()!=targetPos.getY();testPos = testPos.offset(direction)) {
			
			debugStack += "\tLayer "+testPos.getY()+" ";
			debugStack += "["+thisChunk.getBlockState(testPos).getBlock().getLocalizedName()+"] ";
			
			if (thisChunk.getBlockState(testPos).getMaterial() == Material.AIR) {
				//If it's air, apply air absorption
				debugStack += "("+(air_absorption*100);
				rads *= 1-air_absorption;
			} else {
				double blockAbsorption = Math.max(
						0,
						thisChunk.getBlockState(testPos).getBlockHardness(world, testPos)*percent_absorbed_per_hardness);
				debugStack += "{hardness "+thisChunk.getBlockState(testPos).getBlockHardness(world, testPos)+"} = ";
				debugStack += "("+(100*blockAbsorption);
				rads *= 1-(blockAbsorption);
			}
			debugStack += "%): "+rads+" rads\n";
			
			//If cumulative rads have reached min value, no point in continuing calculation
			if (rads < bottom_rads) {
				rads = 0.0;
				debugStack += "\tRads have hit minimum value ("+bottom_rads+").\n";
				break;
			}
		}
		debugStack += "\tFinal Rads: "+rads+"\n";
		debugStack += "====";
		if (NCERConfig.debug_output) { log.debug(debugStack); }
		
		return rads;
	}

	@Override
	public boolean tryAddNewDimension(World world) {
		String key = world.provider.getDimensionType().getName();
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

}
