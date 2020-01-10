package main.java.somdudewillson.ncenvironmentalrads.radiation;

import static nc.config.NCConfig.radiation_player_tick_rate;
import main.java.somdudewillson.ncenvironmentalrads.config.NCERConfig;
import nc.capability.radiation.entity.IEntityRads;
import nc.config.NCConfig;
import nc.network.PacketHandler;
import nc.network.radiation.PlayerRadsUpdatePacket;
import nc.radiation.RadiationHelper;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class EnvironmentalRadiationHandler {
	
	private static final double percent_absorbed_per_hardness = NCERConfig.percent_absorbed_per_hardness;
	
	@SubscribeEvent(priority=EventPriority.LOW)
	public void applyEnvironmentalRadiation(TickEvent.PlayerTickEvent event) {
		
		if (!NCConfig.radiation_enabled_public) { return; }
		
		if (event.phase != TickEvent.Phase.START ||
				((event.player.world.getTotalWorldTime() + event.player.getUniqueID().hashCode()) % radiation_player_tick_rate) != 0) { return; }
		
		if (event.side == Side.SERVER && event.player instanceof EntityPlayerMP) {			
			EntityPlayerMP player = (EntityPlayerMP)event.player;
			String dimKey = player.world.provider.getDimensionType().getName();
			//If environmental radiation is disabled in the current dimension, skip calculations
			if (!NCERConfig.dimSpecific.environmental_radiation_enabled.get(dimKey)) { return; }
			
			IEntityRads playerRads = RadiationHelper.getEntityRadiation(player);
			
			double appliedRads = RadiationHelper.addRadsToEntity(playerRads, player, getRadsAtPos(player.getPosition(), player.world, dimKey), false, false, radiation_player_tick_rate);
			playerRads.setRadiationLevel(playerRads.getRadiationLevel() + appliedRads);
			
			PacketHandler.instance.sendTo(new PlayerRadsUpdatePacket(playerRads), player);
		}
	}
	
	private double getRadsAtPos(BlockPos pos, World world, String dimKey) {
		double top_rads = NCERConfig.dimSpecific.top_rads.get(dimKey);
		int top_height = NCERConfig.dimSpecific.top_height.get(dimKey);
		
		//If player is above the top height, we know environmental rads are at max
		if (pos.getY() >= top_height) {return top_rads; }
		
		int bottom_height = NCERConfig.dimSpecific.bottom_height.get(dimKey);		
		
		//If player is at or below the bottom height, we know environmental rads are 0
		if (pos.getY() <= bottom_height) {return 0.0; }
		
		double air_absorption = NCERConfig.dimSpecific.use_atmospheric_absorption.get(dimKey) ? 
				NCERConfig.dimSpecific.air_absorption.get(dimKey) :
				0.0;
		
		//If player has LoS to the sky, no occlusion checks are necessary
		if (world.canSeeSky(pos)) {
			return top_rads * Math.pow((1-air_absorption), (top_height-pos.getY()));
		}
		
		double bottom_rads = NCERConfig.dimSpecific.bottom_rads.get(dimKey) == 0.0 ? NCConfig.radiation_lowest_rate : NCERConfig.dimSpecific.bottom_rads.get(dimKey);
		
		//If the player lacks LoS to the sky, loop down from the lowest block that does
		//and compute absorbed radiation from block hardnesses
		int topBlock = world.getHeight(pos.getX(), pos.getZ());
		int airLayers = Math.max((top_height-topBlock),0);
		double rads = top_rads * Math.pow((1-air_absorption), airLayers);
		Chunk thisChunk = world.getChunkFromBlockCoords(pos);
		
		String debugStack = "----------\n";
		debugStack += "Top block: "+topBlock+"\n";
		debugStack += "Top air ("+airLayers+" layers): "+rads+" rads\n";
		for (BlockPos testPos = new BlockPos(pos.getX(), topBlock-1, pos.getZ());
				testPos.getY()>=pos.getY();testPos = testPos.down()) {
			debugStack += "\tLayer "+testPos.getY()+" ";
			debugStack += "["+thisChunk.getBlockState(testPos).getBlock().getLocalizedName()+"] (";
			if (thisChunk.getBlockState(testPos).getMaterial() == Material.AIR) {
				//If it's air, apply air absorption
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
		System.out.println(debugStack);
		
		return rads;
	}
}
