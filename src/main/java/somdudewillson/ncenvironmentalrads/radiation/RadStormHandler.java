package somdudewillson.ncenvironmentalrads.radiation;

import static nc.config.NCConfig.radiation_player_tick_rate;

import java.util.Random;

import nc.config.NCConfig;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import somdudewillson.ncenvironmentalrads.config.NCERConfig;
import somdudewillson.ncenvironmentalrads.cycledata.RadiationStormData;

public class RadStormHandler {
	Random rng = new Random(System.nanoTime());
	
	@SubscribeEvent
	public void applyEnvironmentalRadiation(TickEvent.WorldTickEvent event) {
		
		if (!NCConfig.radiation_enabled_public) { return; }
		
		if (event.phase != TickEvent.Phase.START ||
				(event.world.getTotalWorldTime() % 
						radiation_player_tick_rate) != 0) { return; }
		
		if (event.side == Side.SERVER) {
			World world = event.world;
			String dimKey = world.provider.getDimensionType().getName();
			
			//If environmental radiation is disabled in the current dimension, skip calculations
			if (NCERConfig.dimSpecific.environmental_radiation_enabled.containsKey(dimKey) &&
					!NCERConfig.dimSpecific.environmental_radiation_enabled.get(dimKey)) { return; }
			
			RadiationStormData stormData = RadiationStormData.get(world);
			
			//If current storm, reduce duration counter
			if (stormData.getStormDuration()>0) {
				stormData.setStormDuration(stormData.getStormDuration()-1);
				
				if (stormData.getStormDuration()<=0) {
					//TODO If storm is over, notify clients
				}
			} else if (rng.nextDouble()<=0.00001851851) {
				stormData.setStormDuration(10*60);
				//TODO Notify clients when storm begins
			}
			
			//PacketHandler.instance.sendTo(new PlayerRadsUpdatePacket(playerRads), player);
		}
	}
}
