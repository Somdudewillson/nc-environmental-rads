package somdudewillson.ncenvironmentalrads.radiation.helpers;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;

import nc.config.NCConfig;
import net.minecraft.block.material.Material;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import somdudewillson.ncenvironmentalrads.EnvironmentalRads;
import somdudewillson.ncenvironmentalrads.config.NCERConfig;
import zmaster587.advancedRocketry.api.dimension.IDimensionProperties;
import zmaster587.advancedRocketry.api.dimension.solar.IGalaxy;
import zmaster587.advancedRocketry.api.dimension.solar.StellarBody;
import zmaster587.advancedRocketry.dimension.DimensionManager;

public class AREnvironmentalRadiationHelper implements
		IEnvironmentalRadiationHelper {
	
	private static final double Stefan_Boltzmann_constant = 5.6703 * Math.pow(10, -8);
	private static final double Plancks_constant = 6.62607004 * Math.pow(10, -34);//h
	private static final double c = 299792458;
	//private static final double Boltzmanns_constant = 0.0000000000000000000000138;//k
	
	private static final double gravitational_constant = 6.674*Math.pow(10, -11);
	
	private Logger log = EnvironmentalRads.logger;
	
	@Override
	public double getRadsFromSky(BlockPos pos, World world, String dimKey, String biomeKey) {
		int dimID = world.provider.getDimension();
		IGalaxy galaxy = DimensionManager.getInstance();
		dimKey = NCERConfig.dimSpecific.environmental_radiation_enabled.containsKey(dimKey) ? 
			dimKey : "overworld";

		int top_height = NCERConfig.dimSpecific.sky_origin_height.get(dimKey);
		double top_rads;
		if (NCERConfig.arSettings.solar_radiation_origin && galaxy.isDimensionCreated(dimID)) {
			top_rads = getRadsFromSystemStar(galaxy.getDimensionProperties(dimID));
			if (NCERConfig.dimSpecific.sky_respect_daynight.get(dimKey)) { top_rads *= world.getSunBrightnessFactor(0); }
		} else {
			top_rads = NCERConfig.dimSpecific.sky_max_rads.get(dimKey);
		}
		top_rads *= NCERConfig.arSettings.magnetic_deflection.getOrDefault(dimKey, 1.0);
		top_rads = getAdjustedRadsFromSky(top_rads, biomeKey);
		
		//If player is above the top height, we know environmental rads are at max
		if (pos.getY() >= top_height) {return top_rads; }
		
		double air_absorption = getAirAbsorption(top_height, world);

		boolean is_raining = world.isRaining();
		//If player has LoS to the sky, no occlusion checks are necessary
		if (world.canSeeSky(pos)) {
			if (NCERConfig.dimSpecific.sky_alternate_rain.get(dimKey) && is_raining) {
				return top_rads;
			} else {
				return top_rads * Math.pow((1-air_absorption), (top_height-pos.getY()));
			}
		}
		if (NCERConfig.dimSpecific.sky_alternate_rain.get(dimKey) && is_raining) { return 0; }
		
		double bottom_rads = NCConfig.radiation_lowest_rate;
		
		//If the player lacks LoS to the sky, loop down from the lowest block that does
		//and compute absorbed radiation from block hardnesses
		BlockPos projectPos = new BlockPos(pos.getX(), top_height-1, pos.getZ());
		
		int startingHeight = world.getHeight(pos.getX(), pos.getZ());
		int airLayers = Math.max((top_height-startingHeight),0);
		
		double remainingRads = top_rads * Math.pow((1-air_absorption), airLayers);
		projectPos = new BlockPos(pos.getX(), startingHeight-1, pos.getZ());
			
		double rads = projectRadsFromPoint(projectPos, EnumFacing.DOWN, remainingRads,
				world, pos,
				0.0, bottom_rads);
		
		return rads;
	}

	@Override
	public double getRadsFromBedrock(BlockPos pos, World world, String dimKey, String biomeKey) {
		dimKey = NCERConfig.dimSpecific.environmental_radiation_enabled.containsKey(dimKey) ? 
				dimKey : "overworld";
		
		double top_rads = NCERConfig.dimSpecific.bedrock_max_rads.get(dimKey);
		top_rads = getAdjustedRadsFromBedrock(top_rads, biomeKey);
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
				air_absorption = getAirAbsorption(testPos.getY(), world);
				rads *= 1-air_absorption;
			} else {
				rads = getRadsThroughBlock(rads, thisChunk.getBlockState(testPos), world, targetPos);
			}
			debugStack += rads+" rads\n";
			
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
	public String getDimensionKey(World world) {
		IGalaxy galaxy = DimensionManager.getInstance();
		int dimID = world.provider.getDimension();
		
		return galaxy.isDimensionCreated(dimID) ? 
				galaxy.getDimensionProperties(dimID).getName():
				world.provider.getDimensionType().getName();
	}
	
	@Override
	public Iterable<String> getDimensionKeys() {
		Iterable<String> defaultKeys = IEnvironmentalRadiationHelper.super.getDimensionKeys();
		List<String> dimKeys = new ArrayList<>();
		for (String defaultKey : defaultKeys) { dimKeys.add(defaultKey); }
		
		IGalaxy galaxy = DimensionManager.getInstance();
		for (Integer dimID : galaxy.getRegisteredDimensions()) {
			dimKeys.add(galaxy.getDimensionProperties(dimID).getName());
		}
		
		return dimKeys;
	}
	
	@Override
	public boolean tryAddNewDimension(String key) {
		boolean result = IEnvironmentalRadiationHelper.super.tryAddNewDimension(key);
		if (result) {
			
		    if (!NCERConfig.arSettings.magnetic_deflection.containsKey(key)) {
		    	NCERConfig.arSettings.magnetic_deflection.put(key, new Double(1));
		    }
		    
		}
		return result;
	}
	
	//==========Utility Functions
	private double getRadsFromSystemStar(IDimensionProperties dim) {
		double rads = 0.0;
		StellarBody star = dim.getStar();
		
		if (star.isBlackHole()) {
			double mass = (((star.getSize()*1391000000)/2)*c*c)/(2*gravitational_constant);
			
			rads = blackHoleEmittedRads(1, 38, mass, NCERConfig.arSettings.solar_radiation_sampling);
		} else {
			double surface_area = 4*Math.PI*Math.pow((star.getSize()*1391000000)/2, 2);
			
			//Calculate rads at star
			rads = emittedRadsOverWavelengthInterval(1, 38, star.getTemperature()*57.78, surface_area, NCERConfig.arSettings.solar_radiation_sampling);
			rads *= NCERConfig.arSettings.solar_radiation_scale;
		}
		
		return rads;
	}
	
	/**
	 * 
	 * @param wavelengthStart The lower end of the wavelength interval to be scanned, in nm.
	 * @param wavelengthEnd The upper end of the wavelength interval to be scanned, in nm.
	 * @param temp Kelvin (?)
	 * @param area M^2
	 * @param tests The number of wavelengths between the two ends of the interval to sample. 
	 * @return
	 */
	private double emittedRadsOverWavelengthInterval(int wavelengthStart, int wavelengthEnd,
			double temp, double area, double tests) {		
		double part1 = 2 * Math.PI * Plancks_constant * c * c;
		double part2 = 1239.84 / (8.617385 * Math.pow(10, -5));
		
		double waveInc= (wavelengthEnd - wavelengthStart) / tests;
		double curWave = wavelengthStart - waveInc;
		double watts = 0.0;
		for (int i = 0; i < tests; i++) {
			curWave -= (-1) * waveInc;
		    watts += part1 * waveInc * area * Math.pow(10, -9)
		    		* (1 / (Math.pow(curWave * Math.pow(10, -9), 5)
		    		* (Math.exp(part2 / (curWave * temp)) - 1)));
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
	
	/**
	 * 
	 * @param altitude Currently UNUSED.
	 * @param world
	 * @return
	 */
	private double getAirAbsorption(int altitude, World world) {
		int dimID = world.provider.getDimension();
		String dimKey = world.provider.getDimensionType().getName();
		dimKey = NCERConfig.dimSpecific.environmental_radiation_enabled.containsKey(dimKey) ? 
				dimKey : "overworld";
		IGalaxy galaxy = DimensionManager.getInstance();
		
		if (!NCERConfig.dimSpecific.use_atmospheric_absorption.get(dimKey)) { return 0.0; }
		
		double base_absorption = NCERConfig.arSettings.atmosphere_type_absorption.get(
				galaxy.getDimensionProperties(dimID).getAtmosphere().getUnlocalizedName());
		double air_density;
		if (galaxy.isDimensionCreated(dimID)) {
			base_absorption = NCERConfig.arSettings.atmosphere_type_absorption.get(
					galaxy.getDimensionProperties(dimID).getAtmosphere().getUnlocalizedName());
			
			air_density = galaxy.getDimensionProperties(dimID).getAtmosphereDensity();
			
		} else {
			base_absorption = NCERConfig.dimSpecific.air_absorption.get(dimKey);
			air_density = 1;
		}
		
		return Math.max(base_absorption*air_density, 0.0);
	}
	//==========
}
