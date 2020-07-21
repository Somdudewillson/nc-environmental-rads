package somdudewillson.ncenvironmentalrads.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import somdudewillson.ncenvironmentalrads.utils.CommandUtils;

public class DebugCommand extends CommandBase {

	@Override
	public String getName() {
		return "envrads-debug";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/envrads-debug";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (!(sender.getCommandSenderEntity() instanceof EntityPlayer)) {return;}
		
		for (Biome biome : ForgeRegistries.BIOMES.getValuesCollection()) {
			CommandUtils.sendInfo(sender, biome.getBiomeName());
		}
	}

}
