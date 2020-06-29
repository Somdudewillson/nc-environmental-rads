package main.java.somdudewillson.ncenvironmentalrads.commands;

import main.java.somdudewillson.ncenvironmentalrads.EnvironmentalRads;
import main.java.somdudewillson.ncenvironmentalrads.config.NCERConfig;
import main.java.somdudewillson.ncenvironmentalrads.utils.CommandUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;

public class DimensionConfigCommand extends CommandBase {

	@Override
	public String getName() {
		return "envrads-dimensionconfig";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/envrads-dimensionconfig "
				+ "(<environmental radiation enabled> <use atmospheric absorption> "
				+ "<atmospheric absorption thickness>"
				+ "<sky radiation enabled> <sky rads> <sky origin height> "
				+ "<bedrock radiation enabled> <bedrock rads> <bedrock origin height>)/"
				+ "(\"Remove\")\n"
				+ "~ can be used in place of values, which will keep the current setting value.";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (!(sender.getCommandSenderEntity() instanceof EntityPlayer)) {return;}
		
		EntityPlayer player = (EntityPlayer) sender.getCommandSenderEntity();
		String dimKey = player.world.provider.getDimensionType().getName();
		
		switch (args.length) {
		case 0://Get biome rad settings
			String settingsString = "     Current Dimension-Specific Settings for: "+dimKey+"\n";
			
			settingsString += "Environmental radiation enabled: ";
			settingsString += NCERConfig.dimSpecific.environmental_radiation_enabled.getOrDefault(dimKey, false);
			settingsString += "\n";
			
			settingsString += "Atmospheric absorption enabled: ";
			settingsString += NCERConfig.dimSpecific.use_atmospheric_absorption.getOrDefault(dimKey, false);
			settingsString += "\n";
			
			settingsString += "Sky radiation enabled: ";
			settingsString += NCERConfig.dimSpecific.sky_radiation.getOrDefault(dimKey, false);
			settingsString += "\n";
			
			settingsString += "Sky radiation: ";
			settingsString += NCERConfig.dimSpecific.sky_max_rads.getOrDefault(dimKey, 0.0);
			settingsString += "\n";
			
			settingsString += "Sky radiation source y-level: ";
			settingsString += NCERConfig.dimSpecific.sky_origin_height.getOrDefault(dimKey, 255);
			settingsString += "\n";
			
			settingsString += "Bedrock radiation enabled: ";
			settingsString += NCERConfig.dimSpecific.bedrock_radiation.getOrDefault(dimKey, false);
			settingsString += "\n";
			
			settingsString += "Bedrock radiation: ";
			settingsString += NCERConfig.dimSpecific.bedrock_max_rads.getOrDefault(dimKey, 0.0);
			settingsString += "\n";
			
			settingsString += "Bedrock radiation source y-level: ";
			settingsString += NCERConfig.dimSpecific.bedrock_origin_height.getOrDefault(dimKey, 0);
			
			CommandUtils.sendInfo(sender,settingsString);
			break;
		case 1://Wipe biome rad settings
			if (args[0].trim().equalsIgnoreCase("Remove")) {
				NCERConfig.dimSpecific.environmental_radiation_enabled.remove(dimKey);
				NCERConfig.dimSpecific.use_atmospheric_absorption.remove(dimKey);
				
				NCERConfig.dimSpecific.sky_radiation.remove(dimKey);
				NCERConfig.dimSpecific.sky_max_rads.remove(dimKey);
				NCERConfig.dimSpecific.sky_origin_height.remove(dimKey);
				
				NCERConfig.dimSpecific.bedrock_radiation.remove(dimKey);
				NCERConfig.dimSpecific.bedrock_max_rads.remove(dimKey);
				NCERConfig.dimSpecific.bedrock_origin_height.remove(dimKey);
				
				ConfigManager.sync(EnvironmentalRads.MODID, Config.Type.INSTANCE);
				CommandUtils.sendInfo(sender, "Config entry for "+dimKey+" removed;");
			} else {
				CommandUtils.sendError(sender,"The only valid single-arg from of this command is "
						+ "'/envrads-biomeconfig Remove', "+
						"which entirely removes the biome from the config file.");
			}
			break;
		case 8://Set dimension rad settings
			double newVal;
			String infoString = "     Setting changes for: "+dimKey+"\n";
			
			//Environmental Radiation Enabled-ness
			if (!args[0].trim().equalsIgnoreCase("~")) {
				NCERConfig.dimSpecific.environmental_radiation_enabled.put(dimKey,
						CommandBase.parseBoolean(args[0]));
			}
			infoString += "Environmental radiation enabled: "+
					NCERConfig.dimSpecific.environmental_radiation_enabled.get(dimKey)+"\n";
			
			//Atmospheric Absorption Enabled-ness
			if (!args[1].trim().equalsIgnoreCase("~")) {
				NCERConfig.dimSpecific.use_atmospheric_absorption.put(dimKey,
						CommandBase.parseBoolean(args[1]));
			}
			infoString += "Atmospheric absorption enabled: "+
					NCERConfig.dimSpecific.use_atmospheric_absorption.get(dimKey)+"\n";
			
			
			//-----Sky Radiation Settings
			//Sky Radiation Enabled-ness
			if (!args[2].trim().equalsIgnoreCase("~")) {
				NCERConfig.dimSpecific.sky_radiation.put(dimKey,
						CommandBase.parseBoolean(args[2]));
			}
			infoString += "Sky radiation enabled: "+
					NCERConfig.dimSpecific.sky_radiation.get(dimKey)+"\n";
			
			//Sky Radiation Amount
			if (args[3].trim().equalsIgnoreCase("~")) {
				newVal = NCERConfig.dimSpecific.sky_max_rads.get(dimKey);
			} else {
				try {
					newVal = Double.parseDouble(args[3]);
				} catch (NumberFormatException e) {
					CommandUtils.sendError(sender,"Provide a valid number for the sky rad amount.");
					return;
				}
			}
			
			NCERConfig.dimSpecific.sky_max_rads.put(dimKey, newVal);
			infoString += "Sky radiation: "+newVal+"\n";
			
			//Sky Radiation Source Y-Level
			if (args[4].trim().equalsIgnoreCase("~")) {
				newVal = NCERConfig.dimSpecific.sky_origin_height.get(dimKey);
			} else {
				try {
					newVal = Integer.parseInt(args[4]);
				} catch (NumberFormatException e) {
					CommandUtils.sendError(sender,"Provide a valid number for the sky rad source y-level.");
					return;
				}
			}
			
			NCERConfig.dimSpecific.sky_origin_height.put(dimKey, (int) newVal);
			infoString += "Sky radiation source y-level: "+newVal+"\n";
			//-----
			
			
			//-----Bedrock Radiation Settings
			//Bedrock Radiation Enabled-ness
			if (!args[5].trim().equalsIgnoreCase("~")) {
				NCERConfig.dimSpecific.bedrock_radiation.put(dimKey,
						CommandBase.parseBoolean(args[5]));
			}
			infoString += "Bedrock radiation enabled: "+
					NCERConfig.dimSpecific.bedrock_radiation.get(dimKey)+"\n";
			
			//Bedrock Radiation Amount
			if (args[6].trim().equalsIgnoreCase("~")) {
				newVal = NCERConfig.dimSpecific.bedrock_max_rads.get(dimKey);
			} else {
				try {
					newVal = Double.parseDouble(args[6]);
				} catch (NumberFormatException e) {
					CommandUtils.sendError(sender,"Provide a valid number for the bedrock rad amount.");
					return;
				}
			}
			
			NCERConfig.dimSpecific.bedrock_max_rads.put(dimKey, newVal);
			infoString += "Bedrock radiation: "+newVal+"\n";
			
			//Bedrock Radiation Source Y-Level
			if (args[7].trim().equalsIgnoreCase("~")) {
				newVal = NCERConfig.dimSpecific.bedrock_origin_height.get(dimKey);
			} else {
				try {
					newVal = Integer.parseInt(args[7]);
				} catch (NumberFormatException e) {
					CommandUtils.sendError(sender,"Provide a valid number for the bedrock rad source y-level.");
					return;
				}
			}
			
			NCERConfig.dimSpecific.bedrock_origin_height.put(dimKey, (int) newVal);
			infoString += "Bedrock radiation source y-level: "+newVal+"\n";
			//-----
			
			
			ConfigManager.sync(EnvironmentalRads.MODID, Config.Type.INSTANCE);
			CommandUtils.sendInfo(sender,infoString);
			break;
		default://Invalid arg quantity
			CommandUtils.sendError(sender, 
					"Provide eight arguments to set the settings for the current dimension, "
					+ "provide one argument 'Remove' to remove all settings for the current dimension, "
					+ "or provide no arguments to get the settings for the current dimension.");
			return;
		}

	}

}
