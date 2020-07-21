package somdudewillson.ncenvironmentalrads.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import somdudewillson.ncenvironmentalrads.EnvironmentalRads;
import somdudewillson.ncenvironmentalrads.config.NCERConfig;
import somdudewillson.ncenvironmentalrads.utils.CommandUtils;
import somdudewillson.ncenvironmentalrads.utils.NameUtils;

public class BlockConfigCommand extends CommandBase {

	@Override
	public String getName() {
		return "envrads-blockconfig";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/envrads-blockconfig (rad absorption %/\"Default\"/\"Remove\")";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (!(sender.getCommandSenderEntity() instanceof EntityPlayer)) {return;}
		
		EntityPlayer player = (EntityPlayer) sender.getCommandSenderEntity();
		ItemStack heldItemStack = player.getHeldItemMainhand();
		
		if (!(heldItemStack.getItem() instanceof ItemBlock)) {
			CommandUtils.sendError(sender, "You're not holding a block!");
			return;
		}
		
		String blockKey = NameUtils.getBlockKey(heldItemStack);
		
		switch (args.length) {
		case 0://Get block rad absorption percentage
			double currentRadAbsorption = NCERConfig.blockSettings.rad_absorption.getOrDefault(blockKey, -1.0);
			CommandUtils.sendInfo(sender,"Current rad absorption override for "+blockKey+": "+
					(currentRadAbsorption!=-1 ? (currentRadAbsorption*100)+"%" : "No Override Set"));
			break;
		case 1://Set block rad absorption percentage
			double newRadAbsorption;
			if (args[0].trim().equalsIgnoreCase("Default") || args[0].trim().equalsIgnoreCase("Remove")) {
				NCERConfig.blockSettings.rad_absorption.remove(blockKey);
				
				ConfigManager.sync(EnvironmentalRads.MODID, Config.Type.INSTANCE);
				CommandUtils.sendInfo(sender, blockKey+" rad absorption override removed.");
				return;
			} else {
				try {
					newRadAbsorption = Double.parseDouble(args[0]);
				} catch (NumberFormatException e) {
					CommandUtils.sendError(sender,"Provide a valid number, "
							+ "or \"Default\" to mark it to be autocalculated.");
					return;
				}
			}
			
			NCERConfig.blockSettings.rad_absorption.put(blockKey, newRadAbsorption);

			ConfigManager.sync(EnvironmentalRads.MODID, Config.Type.INSTANCE);
			CommandUtils.sendInfo(sender, blockKey+" rad absorption override set to "+(newRadAbsorption*100)+"%");
			break;
		default://Invalid arg quantity
			CommandUtils.sendError(sender, 
					"Provide one argument to set the rad absorption percentage for the held block, "
					+ "or provide no arguments to get the rad absorption percentage for the held block.");
			return;
		}
	}

}
