package main.java.somdudewillson.ncenvironmentalrads.radiation.helpers;

import zmaster587.advancedRocketry.api.AdvancedRocketryAPI;
import zmaster587.advancedRocketry.api.dimension.IDimensionProperties;
import zmaster587.advancedRocketry.api.dimension.solar.IGalaxy;
import zmaster587.advancedRocketry.api.dimension.solar.StellarBody;
import main.java.somdudewillson.ncenvironmentalrads.config.NCERConfig;
import nc.config.NCConfig;
import net.minecraft.block.material.Material;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class AREnvironmentalRadiationHelper implements
		IEnvironmentalRadiationHelper {
	
	private static final double percent_absorbed_per_hardness = NCERConfig.percent_absorbed_per_hardness;
	
	private static final double Stefan_Boltzmann_constant = 5.6703 * Math.pow(10, -8);
	private static final double Plancks_constant = 6.62607004 * Math.pow(10, -34);//h
	private static final double c = 299792458;
	//private static final double Boltzmanns_constant = 0.0000000000000000000000138;//k
	
	private static final double gravitational_constant = 6.674*Math.pow(10, -11);
	
	@Override
	public double getRadsFromSky(BlockPos pos, World world, String dimKey) {
		int dimID = world.provider.getDimension();
		IGalaxy galaxy = AdvancedRocketryAPI.dimensionManager;
		dimKey = NCERConfig.dimSpecific.environmental_radiation_enabled.containsKey(dimKey) ? 
			dimKey : "overworld";
		
		double top_rads;
		if (NCERConfig.arSettings.solar_radiation_origin && galaxy.isDimensionCreated(dimID)) {
			top_rads = getRadsFromSystemStar(galaxy.getDimensionProperties(dimID));
		} else {
			top_rads = NCERConfig.dimSpecific.sky_max_rads.get(dimKey);
		}
		int top_height = NCERConfig.dimSpecific.sky_origin_height.get(dimKey);
		
		//If player is above the top height, we know environmental rads are at max
		if (pos.getY() >= top_height) {return top_rads; }
		
		double air_absorption;
		if (NCERConfig.dimSpecific.use_atmospheric_absorption.get(dimKey)) {
			if (!galaxy.isDimensionCreated(dimID)) {
				air_absorption = NCERConfig.dimSpecific.air_absorption.get(dimKey);
			} else if (NCERConfig.arSettings.atmosphere_type_absorption.get(
							galaxy.getDimensionProperties(dimID).getAtmosphere().getUnlocalizedName()) == -1) {
				air_absorption = NCERConfig.dimSpecific.air_absorption.get("overworld");
			} else {
				air_absorption = NCERConfig.arSettings.atmosphere_type_absorption.get(
						galaxy.getDimensionProperties(dimID).getAtmosphere().getUnlocalizedName());
			}
		} else {
			air_absorption = 0.0;
		}
		
		if (!NCERConfig.arSettings.use_atmosphere_density_curve) {
			//If player has LoS to the sky, no occlusion checks are necessary
			if (world.canSeeSky(pos)) {
				double adjusted_air_absorption = air_absorption*galaxy.getDimensionProperties(dimID)
						.getAtmosphereDensity();
				return top_rads * Math.pow((1-adjusted_air_absorption), (top_height-pos.getY()));
			}
		}
		
		double bottom_rads = NCConfig.radiation_lowest_rate;
		
		//If the player lacks LoS to the sky, loop down from the lowest block that does
		//and compute absorbed radiation from block hardnesses
		BlockPos projectPos = new BlockPos(pos.getX(), top_height-1, pos.getZ());
		double remainingRads = top_rads;
		if (!NCERConfig.arSettings.use_atmosphere_density_curve) {
			int startingHeight = world.getHeight(pos.getX(), pos.getZ());
			int airLayers = Math.max((top_height-startingHeight),0);
			
			remainingRads = top_rads * Math.pow((1-air_absorption), airLayers);
			projectPos = new BlockPos(pos.getX(), startingHeight-1, pos.getZ());
		}
		double rads = projectRadsFromPoint(projectPos, EnumFacing.DOWN, remainingRads,
				world, pos,
				0.0, bottom_rads);
		
		return rads;
	}

	@Override
	public double getRadsFromBedrock(BlockPos pos, World world, String dimKey) {
		dimKey = NCERConfig.dimSpecific.environmental_radiation_enabled.containsKey(dimKey) ? 
				dimKey : "overworld";
		
		double top_rads = NCERConfig.dimSpecific.bedrock_max_rads.get(dimKey);
		int bottom_height = NCERConfig.dimSpecific.bedrock_origin_height.get(dimKey);
		
		//If player is at or below the bottom height, we know environmental rads are at max
		if (pos.getY() <= bottom_height) {return top_rads; }
		
		double bottom_rads = NCConfig.radiation_lowest_rate;
		
		//Loop up from the lowest block
		//and compute absorbed radiation from block hardnesses
		BlockPos projectPos = new BlockPos(pos.getX(), bottom_height, pos.getZ());
		double rads = projectRadsFromPoint(projectPos, EnumFacing.UP, top_rads,
				world, pos,
				0.0, bottom_rads);
		
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
		
		@SuppressWarnings("unused")
		String debugStack = "\n----------\n";
		debugStack += "Direction: "+direction+"\n";
		debugStack += "Top block: "+startingHeight+"\n";
		
		for (BlockPos testPos = pos;
				testPos.getY()!=targetPos.getY();testPos = testPos.offset(direction)) {
			
			debugStack += "\tLayer "+testPos.getY()+" ";
			debugStack += "["+thisChunk.getBlockState(testPos).getBlock().getLocalizedName()+"] (";
			
			if (thisChunk.getBlockState(testPos).getMaterial() == Material.AIR) {
				//If it's air, apply air absorption
				air_absorption = getAirAbsorption(testPos.getY(), world);
				debugStack += (air_absorption*100);
				rads *= 1-air_absorption;
			} else {
				double blockAbsorption = Math.max(
						0,
						thisChunk.getBlockState(testPos).getBlockHardness(world, testPos)*percent_absorbed_per_hardness);
				debugStack += 100*blockAbsorption;
				rads *= 1-(thisChunk.getBlockState(testPos).getBlockHardness(world, testPos)*percent_absorbed_per_hardness);
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
		//System.out.println(debugStack);
		
		return rads;
	}
	
	
	//==========Utility Functions
	private double getRadsFromSystemStar(IDimensionProperties dim) {
		double rads = 0.0;
		StellarBody star = dim.getStar();
		
		if (star.isBlackHole()) {
			double mass = (((star.getSize()*12742000)/2)*c*c)/(2*gravitational_constant);
			
			rads = blackHoleEmittedRads(1, 38, mass, NCERConfig.arSettings.solar_radiation_sampling);
		} else {
			double surface_area = 4*Math.PI*Math.pow((star.getSize()*12742000)/2, 2);
			
			//Calculate rads at star
			rads = emittedRadsOverWavelengthInterval(1, 38, star.getTemperature(), surface_area, NCERConfig.arSettings.solar_radiation_sampling);
			rads *= NCERConfig.arSettings.solar_radiation_scale;
		}
		
		return rads;
	}
	
	private double emittedRadsOverWavelengthInterval(int wavelengthStart, int wavelengthEnd,
			double temp, double area, double tests) {		
		double part1 = 2 * Math.PI * Plancks_constant * c * c;
		double part2 = 1239.84 / (8.617385 * Math.pow(10, -5));
		
		double waveInc= (wavelengthEnd - wavelengthStart) / tests;
		double curWave = wavelengthStart - waveInc;
		double watts = 0.0;
		for (int i = 0; i < tests; i++) {
			curWave -= (-1) * waveInc;
		    watts += part1 * waveInc * area * Math.pow(10, -9) * (1 / (Math.pow(curWave * Math.pow(10, -9), 5) * (Math.exp(part2 / (curWave * temp)) - 1)));
		};
		
		return (watts/88)/60;
	}
	
	private double blackHoleEmittedRads(int wavelengthStart, int wavelengthEnd,
			double mass, int tests) {
		double k = 0.446895;//Estimated mass absorption coefficient of Hydrogen
		double eddingtonLuminosity = (4*Math.PI*gravitational_constant*mass*c)/k;
		double eddingtonAccretion = (10*eddingtonLuminosity)/(c*c);
		double innerRadius = (2*gravitational_constant*mass)/(c*c);
		double outerRadius = innerRadius*10;
		
		double part1 = Math.pow((3*gravitational_constant*mass*eddingtonAccretion)/
				(8*Math.PI*Stefan_Boltzmann_constant*Math.pow(innerRadius, 3)), 0.25);
		double part2 = Math.pow(outerRadius/innerRadius, -0.75);
		
		double temperature = part1*part2;
		double surfaceArea = 4*Math.PI*Math.PI*((innerRadius+outerRadius)/2)*((outerRadius-innerRadius)/4);
		
		return emittedRadsOverWavelengthInterval(1, 38, temperature, surfaceArea, NCERConfig.arSettings.solar_radiation_sampling) *
				NCERConfig.arSettings.accretion_radiation_scale;
	}
	
	private double getAirAbsorption(int altitude, World world) {
		int dimID = world.provider.getDimension();
		IGalaxy galaxy = AdvancedRocketryAPI.dimensionManager;
		
		double base_absorption = NCERConfig.arSettings.atmosphere_type_absorption.get(
				galaxy.getDimensionProperties(dimID).getAtmosphere().getUnlocalizedName());
		double air_density;
		if (galaxy.isDimensionCreated(dimID)) {
			base_absorption = NCERConfig.arSettings.atmosphere_type_absorption.get(
					galaxy.getDimensionProperties(dimID).getAtmosphere().getUnlocalizedName());
			if (NCERConfig.arSettings.use_atmosphere_density_curve) {
				air_density = galaxy.getDimensionProperties(dimID).getAtmosphereDensityAtHeight(altitude);
			} else {
				air_density = galaxy.getDimensionProperties(dimID).getAtmosphereDensity();
			}
		} else {
			String dimKey = world.provider.getDimensionType().getName();
			dimKey = NCERConfig.dimSpecific.environmental_radiation_enabled.containsKey(dimKey) ? 
					dimKey : "overworld";
			base_absorption = NCERConfig.dimSpecific.air_absorption.get(dimKey);
			air_density = 1;
		}
		
		return base_absorption*air_density;
	}
	//==========
}
