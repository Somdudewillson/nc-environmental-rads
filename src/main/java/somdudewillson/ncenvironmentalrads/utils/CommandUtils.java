package somdudewillson.ncenvironmentalrads.utils;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CommandUtils {
	//-----ICommandSender Convenience Methods
	public static void sendInfo(ICommandSender target, String message) {
		sendInfo(target.getCommandSenderEntity(), message);
	}
	
	public static void sendWarning(ICommandSender target, String message) {
		sendWarning(target.getCommandSenderEntity(), message);
	}
	
	public static void sendError(ICommandSender target, String message) {
		sendError(target.getCommandSenderEntity(), message);
	}
	//-----
	
	public static void sendInfo(Entity target, String message) {
		sendColoredMessage(target, message, TextFormatting.AQUA);
	}
	
	public static void sendWarning(Entity target, String message) {
		sendColoredMessage(target, message, TextFormatting.YELLOW);
	}
	
	public static void sendError(Entity target, String message) {
		sendColoredMessage(target, message, TextFormatting.RED);
	}
	
	public static void sendColoredMessage(Entity target, String message, TextFormatting color) {
		TextComponentString formedMessage = new TextComponentString(message);
		formedMessage.getStyle().setColor(color);
		target.sendMessage(formedMessage);
	}
}