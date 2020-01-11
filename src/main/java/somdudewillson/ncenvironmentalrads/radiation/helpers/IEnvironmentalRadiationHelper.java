package main.java.somdudewillson.ncenvironmentalrads.radiation.helpers;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IEnvironmentalRadiationHelper {	
	public double getRadsFromSky(BlockPos pos, World world, String dimKey);
	public double getRadsFromBedrock(BlockPos pos, World world, String dimKey);
	public double projectRadsFromPoint(BlockPos pos, EnumFacing direction, double sourceRads,
			World world, BlockPos targetPos,
			double air_absorption, double bottom_rads);
}
