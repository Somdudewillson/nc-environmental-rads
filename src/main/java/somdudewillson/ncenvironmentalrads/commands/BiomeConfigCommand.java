package somdudewillson.ncenvironmentalrads.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import somdudewillson.ncenvironmentalrads.EnvironmentalRads;
import somdudewillson.ncenvironmentalrads.config.NCERConfig;
import somdudewillson.ncenvironmentalrads.utils.CommandUtils;

public class BiomeConfigCommand extends CommandBase {

	@Override
	public String getName() {
		return "envrads-biomeconfig";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/envrads-biomeconfig "
				+ "(<radiation effects enabled> "
				+ "<sky mult> <sky shift> <bedrock mult> <bedrock shift>)/"
				+ "(\"Remove\")\n"
				+ "~ can be used in place of values, which will keep the current setting value.";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (!(sender.getCommandSenderEntity() instanceof EntityPlayer)) {return;}
		
		EntityPlayer player = (EntityPlayer) sender.getCommandSenderEntity();
		String biomeKey = player.world.getBiome(player.getPosition()).getRegistryName().toString();
		
		switch (args.length) {
		case 0://Get biome rad settings
			String settingsString = "     Current Biome-Specific Settings for: "+biomeKey+"\n";
			
			settingsString += "Biome-specific radiation effects enabled: ";
			settingsString += NCERConfig.biomeSpecific.biome_effects_enabled.getOrDefault(biomeKey, false);
			settingsString += "\n";
			
			settingsString += "Sky Multiplier: ";
			settingsString += NCERConfig.biomeSpecific.sky_multiplier.getOrDefault(biomeKey, 1.0);
			settingsString += "x\n";
			
			settingsString += "Sky Shift: ";
			settingsString += NCERConfig.biomeSpecific.sky_shift.getOrDefault(biomeKey, 0.0);
			settingsString += "\n";
			
			settingsString += "Bedrock Multiplier: ";
			settingsString += NCERConfig.biomeSpecific.bedrock_multiplier.getOrDefault(biomeKey, 1.0);
			settingsString += "x\n";
			
			settingsString += "Sky Shift: ";
			settingsString += NCERConfig.biomeSpecific.bedrock_shift.getOrDefault(biomeKey, 0.0);
			
			CommandUtils.sendInfo(sender,settingsString);
			break;
		case 1://Wipe biome rad settings
			if (args[0].trim().equalsIgnoreCase("Remove")) {
				NCERConfig.biomeSpecific.biome_effects_enabled.remove(biomeKey);
				NCERConfig.biomeSpecific.sky_multiplier.remove(biomeKey);
				NCERConfig.biomeSpecific.sky_shift.remove(biomeKey);
				NCERConfig.biomeSpecific.bedrock_multiplier.remove(biomeKey);
				NCERConfig.biomeSpecific.bedrock_shift.remove(biomeKey);
				
				ConfigManager.sync(EnvironmentalRads.MODID, Config.Type.INSTANCE);
				CommandUtils.sendInfo(sender, "Config entry for "+biomeKey+" removed;");
			} else {
				CommandUtils.sendError(sender,"The only valid single-arg from of this command is "
						+ "'/envrads-biomeconfig Remove', "+
						"which entirely removes the biome from the config file.");
			}
			break;
		case 5://Set biome rad settings
			double newVal;
			String infoString = "     Setting changes for: "+biomeKey+"\n";
			
			//Radiation Effects Enabled-ness
			if (!args[0].trim().equalsIgnoreCase("~")) {
				NCERConfig.biomeSpecific.biome_effects_enabled.put(biomeKey,
						CommandBase.parseBoolean(args[0]));
			}
			infoString += "Biome-specific radiation effects enabled: "+
					NCERConfig.biomeSpecific.biome_effects_enabled.get(biomeKey)+"\n";
			
			//Sky Multiplier
			if (args[1].trim().equalsIgnoreCase("~")) {
				newVal = NCERConfig.biomeSpecific.sky_multiplier.get(biomeKey);
			} else {
				try {
					newVal = Double.parseDouble(args[1]);
				} catch (NumberFormatException e) {
					CommandUtils.sendError(sender,"Provide a valid number for the sky rad multiplier.");
					return;
				}
			}
			
			NCERConfig.biomeSpecific.sky_multiplier.put(biomeKey, newVal);
			infoString += "Sky Multiplier: "+newVal+"x\n";
			
			//Sky Shift
			if (args[2].trim().equalsIgnoreCase("~")) {
				newVal = NCERConfig.biomeSpecific.sky_shift.get(biomeKey);
			} else {
				try {
					newVal = Double.parseDouble(args[2]);
				} catch (NumberFormatException e) {
					CommandUtils.sendError(sender,"Provide a valid number for the sky rad shift.");
					return;
				}
			}
			
			NCERConfig.biomeSpecific.sky_shift.put(biomeKey, newVal);
			infoString += "Sky Shift: "+(newVal>0 ? "+" : "")+newVal+"\n";
			
			//Bedrock Multiplier
			if (args[3].trim().equalsIgnoreCase("~")) {
				newVal = NCERConfig.biomeSpecific.bedrock_multiplier.get(biomeKey);
			} else {
				try {
					newVal = Double.parseDouble(args[3]);
				} catch (NumberFormatException e) {
					CommandUtils.sendError(sender,"Provide a valid number for the bedrock rad multiplier.");
					return;
				}
			}
			
			NCERConfig.biomeSpecific.bedrock_multiplier.put(biomeKey, newVal);
			infoString += "Bedrock Multiplier: "+newVal+"x\n";
			
			//Sky Shift
			if (args[4].trim().equalsIgnoreCase("~")) {
				newVal = NCERConfig.biomeSpecific.bedrock_shift.get(biomeKey);
			} else {
				try {
					newVal = Double.parseDouble(args[4]);
				} catch (NumberFormatException e) {
					CommandUtils.sendError(sender,"Provide a valid number for the bedrock rad shift.");
					return;
				}
			}
			
			NCERConfig.biomeSpecific.bedrock_shift.put(biomeKey, newVal);
			infoString += "Bedrock Shift: "+(newVal>0 ? "+" : "")+newVal;
			
			
			ConfigManager.sync(EnvironmentalRads.MODID, Config.Type.INSTANCE);
			CommandUtils.sendInfo(sender,infoString);
			break;
		default://Invalid arg quantity
			CommandUtils.sendError(sender, 
					"Provide five arguments to set the settings for the current biome, "
					+ "provide one argument 'Remove' to remove all settings for the current biome, "
					+ "or provide no arguments to get the settings for the current biome.");
			return;
		}
		
	}

}
