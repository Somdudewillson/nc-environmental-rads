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
import somdudewillson.ncenvironmentalrads.proxy.CommonProxy;
import somdudewillson.ncenvironmentalrads.utils.CommandUtils;

public class DimensionConfigCommand extends CommandBase {

	@Override
	public String getName() {
		return "envrads-dimensionconfig";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/envrads-dimensionconfig \n"
				+ "(\"Get\")\n"
				+ "(\"Set\" <environmental radiation enabled> <use atmospheric absorption> "
				+ "<atmospheric absorption thickness>"
				+ "<sky radiation enabled> <sky rads> <sky origin height> <respect day/night cycle>"
				+ "<bedrock radiation enabled> <bedrock rads> <bedrock origin height>)/"
				+ "(\"Remove\")\n"
				+ "(\"Scan\")\n"
				+ "~ can be used in place of values, which will keep the current setting value.";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (!(sender.getCommandSenderEntity() instanceof EntityPlayer)) {return;}
		if (args.length<1) {CommandUtils.sendError(sender, "At least one argument is required!");}
		
		EntityPlayer player = (EntityPlayer) sender.getCommandSenderEntity();
		String dimKey = CommonProxy.helper.getDimensionKey(player.world);
		
		switch (args[0].trim().toLowerCase()) {
		case "get"://Get dim rad settings
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
			
			settingsString += "Sky radiation respects day/night: ";
			settingsString += NCERConfig.dimSpecific.sky_respect_daynight.getOrDefault(dimKey, false);
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
		case "remove"://Wipe biome rad settings
			NCERConfig.dimSpecific.environmental_radiation_enabled.remove(dimKey);
			NCERConfig.dimSpecific.use_atmospheric_absorption.remove(dimKey);
			
			NCERConfig.dimSpecific.sky_radiation.remove(dimKey);
			NCERConfig.dimSpecific.sky_max_rads.remove(dimKey);
			NCERConfig.dimSpecific.sky_origin_height.remove(dimKey);
			NCERConfig.dimSpecific.sky_respect_daynight.remove(dimKey);
			
			NCERConfig.dimSpecific.bedrock_radiation.remove(dimKey);
			NCERConfig.dimSpecific.bedrock_max_rads.remove(dimKey);
			NCERConfig.dimSpecific.bedrock_origin_height.remove(dimKey);
			
			ConfigManager.sync(EnvironmentalRads.MODID, Config.Type.INSTANCE);
			CommandUtils.sendInfo(sender, "Config entry for "+dimKey+" removed;");
			break;
		case "set"://Set dimension rad settings
			if (args.length != 10) { CommandUtils.sendError(sender, "Incorrect number of arguments.");return; }
			
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
			
			//Atmospheric Absorption Thickness
			if (!args[2].trim().equalsIgnoreCase("~")) {
				newVal = NCERConfig.dimSpecific.atmospheric_absorption_thickness.get(dimKey);
			} else {
				try {
					newVal = Integer.parseInt(args[2]);
				} catch (NumberFormatException e) {
					CommandUtils.sendError(sender,"Provide a valid number for the atmospheric absorption thickness.");
					return;
				}
			}
			NCERConfig.dimSpecific.atmospheric_absorption_thickness.put(dimKey, (int) newVal);
			infoString += "Atmospheric absorption thickness: "+
					newVal+"\n";
			
			
			//-----Sky Radiation Settings
			//Sky Radiation Enabled-ness
			if (!args[3].trim().equalsIgnoreCase("~")) {
				NCERConfig.dimSpecific.sky_radiation.put(dimKey,
						CommandBase.parseBoolean(args[3]));
			}
			infoString += "Sky radiation enabled: "+
					NCERConfig.dimSpecific.sky_radiation.get(dimKey)+"\n";
			
			//Sky Radiation Amount
			if (args[4].trim().equalsIgnoreCase("~")) {
				newVal = NCERConfig.dimSpecific.sky_max_rads.get(dimKey);
			} else {
				try {
					newVal = Double.parseDouble(args[4]);
				} catch (NumberFormatException e) {
					CommandUtils.sendError(sender,"Provide a valid number for the sky rad amount.");
					return;
				}
			}
			NCERConfig.dimSpecific.sky_max_rads.put(dimKey, newVal);
			infoString += "Sky radiation: "+newVal+"\n";
			
			//Sky Radiation Source Y-Level
			if (args[5].trim().equalsIgnoreCase("~")) {
				newVal = NCERConfig.dimSpecific.sky_origin_height.get(dimKey);
			} else {
				try {
					newVal = Integer.parseInt(args[5]);
				} catch (NumberFormatException e) {
					CommandUtils.sendError(sender,"Provide a valid number for the sky rad source y-level.");
					return;
				}
			}
			
			NCERConfig.dimSpecific.sky_origin_height.put(dimKey, (int) newVal);
			infoString += "Sky radiation source y-level: "+newVal+"\n";
			
			if (!args[6].trim().equalsIgnoreCase("~")) {
				NCERConfig.dimSpecific.sky_respect_daynight.put(dimKey,
						CommandBase.parseBoolean(args[6]));
			}
			infoString += "Sky radiation respects day/night cycle: "+
					NCERConfig.dimSpecific.sky_respect_daynight.get(dimKey)+"\n";
			//-----
			
			
			//-----Bedrock Radiation Settings
			//Bedrock Radiation Enabled-ness
			if (!args[7].trim().equalsIgnoreCase("~")) {
				NCERConfig.dimSpecific.bedrock_radiation.put(dimKey,
						CommandBase.parseBoolean(args[7]));
			}
			infoString += "Bedrock radiation enabled: "+
					NCERConfig.dimSpecific.bedrock_radiation.get(dimKey)+"\n";
			
			//Bedrock Radiation Amount
			if (args[8].trim().equalsIgnoreCase("~")) {
				newVal = NCERConfig.dimSpecific.bedrock_max_rads.get(dimKey);
			} else {
				try {
					newVal = Double.parseDouble(args[8]);
				} catch (NumberFormatException e) {
					CommandUtils.sendError(sender,"Provide a valid number for the bedrock rad amount.");
					return;
				}
			}
			
			NCERConfig.dimSpecific.bedrock_max_rads.put(dimKey, newVal);
			infoString += "Bedrock radiation: "+newVal+"\n";
			
			//Bedrock Radiation Source Y-Level
			if (args[9].trim().equalsIgnoreCase("~")) {
				newVal = NCERConfig.dimSpecific.bedrock_origin_height.get(dimKey);
			} else {
				try {
					newVal = Integer.parseInt(args[9]);
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
		case "scan"://Scan all dims
			String scanResult = "";
			
			for (String scannedKey : CommonProxy.helper.getDimensionKeys()) {
				scanResult += "Dimension '"+scannedKey+"' registered...\n";
				
				CommonProxy.helper.tryAddNewDimension(scannedKey);
			}
			
			CommandUtils.sendInfo(sender,scanResult);
			break;
		default://Invalid action
			CommandUtils.sendError(sender, "'"+args[0].trim()+"' is not a valid action.\n"
					+ "Valid actions are 'Get', 'Set', 'Remove', and 'Scan'.");
			return;
		}

	}

}
