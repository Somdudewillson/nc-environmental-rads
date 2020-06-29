package main.java.somdudewillson.ncenvironmentalrads.radiation.helpers;

import main.java.somdudewillson.ncenvironmentalrads.config.NCERConfig;
import main.java.somdudewillson.ncenvironmentalrads.utils.NameUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
	
	public boolean tryAddNewDimension(World world);
	public boolean tryAddNewBiome(World world, BlockPos pos);
	
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
