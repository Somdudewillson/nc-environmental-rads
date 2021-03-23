package somdudewillson.ncenvironmentalrads.radiation;

import static nc.config.NCConfig.radiation_player_tick_rate;

import org.apache.logging.log4j.Logger;

import nc.capability.radiation.entity.IEntityRads;
import nc.config.NCConfig;
import nc.network.PacketHandler;
import nc.network.radiation.PlayerRadsUpdatePacket;
import nc.radiation.RadiationHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.stats.StatList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import somdudewillson.ncenvironmentalrads.EnvironmentalRads;
import somdudewillson.ncenvironmentalrads.config.NCERConfig;
import somdudewillson.ncenvironmentalrads.radiation.helpers.IEnvironmentalRadiationHelper;

public class PlayerEnvironmentalRadiationHandler {
	private IEnvironmentalRadiationHelper helper;
	
	public PlayerEnvironmentalRadiationHandler(IEnvironmentalRadiationHelper helper) {
		this.helper = helper;
	}
	
	@SubscribeEvent(priority=EventPriority.LOW)
	public void applyEnvironmentalRadiation(TickEvent.PlayerTickEvent event) {
		
		if (!NCConfig.radiation_enabled_public) { return; }
		
		if (event.phase != TickEvent.Phase.START ||
				((event.player.world.getTotalWorldTime() + event.player.getUniqueID().hashCode()) % 
						radiation_player_tick_rate) != 0) { return; }
		
		if (event.side == Side.SERVER && event.player instanceof EntityPlayerMP) {			
			EntityPlayerMP player = (EntityPlayerMP)event.player;
			String dimKey = helper.getDimensionKey(player.world);
			String biomeKey = player.world.getBiome(event.player.getPosition()).getRegistryName().toString();
			
			Logger log = EnvironmentalRads.logger;
			
			//Add missing dimensions
			if (!NCERConfig.dimSpecific.environmental_radiation_enabled.containsKey(dimKey)) {
				log.warn("Unregistered Dimension Encountered, attemting auto-register of "+dimKey+"...");
				boolean success = helper.tryAddNewDimension(player.world);
				log.warn(success ? "Auto-registry succeeded." : "Auto-registry failed.");
			}
			//Add missing biomes
			if (!NCERConfig.biomeSpecific.biome_effects_enabled.containsKey(biomeKey)) {
				log.warn("Unregistered Biome Encountered, attemting auto-register of "+biomeKey+"...");
				boolean success = helper.tryAddNewBiome(player.world, player.getPosition());
				log.warn(success ? "Auto-registry succeeded." : "Auto-registry failed.");
			}
			
			//If environmental radiation is disabled in the current dimension, skip calculations
			if (!NCERConfig.dimSpecific.environmental_radiation_enabled.getOrDefault(dimKey, false)) { return; }
			//If the player is protected by the grace period, skip calculations
			if (player.getStatFile().readStat(StatList.PLAY_ONE_MINUTE) <= NCERConfig.grace_period_minutes) { return; }
			
			IEntityRads playerRads = RadiationHelper.getEntityRadiation(player);
			
			double calculatedRads = getRadsAtPos(player.getPosition(), player.world, dimKey, biomeKey);
			double appliedRads = RadiationHelper.addRadsToEntity(playerRads, player, calculatedRads, false, false, radiation_player_tick_rate);
			playerRads.setRadiationLevel(playerRads.getRadiationLevel() + appliedRads);
			
			PacketHandler.instance.sendTo(new PlayerRadsUpdatePacket(playerRads), player);
		}
	}
	
	private double getRadsAtPos(BlockPos pos, World world, String dimKey, String biomeKey) {
		double rads = 0.0;
		dimKey = NCERConfig.dimSpecific.environmental_radiation_enabled.containsKey(dimKey) ? 
			dimKey : "overworld";
		
		//Config fixer
		if (!NCERConfig.dimSpecific.verified.contains(dimKey)) {
			helper.tryAddNewDimension(dimKey);
			NCERConfig.dimSpecific.verified.add(dimKey);
		}
		
		if (NCERConfig.dimSpecific.sky_radiation.get(dimKey)) {//If sky radiation is enabled, calculate it
			rads += helper.getRadsFromSky(pos, world, dimKey, biomeKey);
		}
		if (NCERConfig.dimSpecific.bedrock_radiation.get(dimKey)) {//If bedrock radiation is enabled, calculate it
			rads += helper.getRadsFromBedrock(pos, world, dimKey, biomeKey);
		}
		
		return rads;
	}
}
