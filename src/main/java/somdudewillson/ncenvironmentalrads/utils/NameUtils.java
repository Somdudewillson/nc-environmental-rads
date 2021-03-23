package somdudewillson.ncenvironmentalrads.utils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import somdudewillson.ncenvironmentalrads.EnvironmentalRads;

public class NameUtils {
	private static FixedQueueSet<Item> errdItems = new FixedQueueSet<Item>(5);
	
	public static String getBlockKey(ItemStack itemBlockStack) {
		if (!(itemBlockStack.getItem() instanceof ItemBlock)) {
			throw new IllegalArgumentException("ItemStack is not a stack of ItemBlocks!");
		}
		
		ItemBlock itemBlock = (ItemBlock) itemBlockStack.getItem();
		
		return getBlockKey(itemBlockStack, itemBlock);
	}
	
	public static String getBlockKey(ItemStack itemStack, Item item) {
		String name = item.getRegistryName().toString();
		name += getEscapedUnlocalizedName(itemStack,item);
		
		return name;
	}
	
	public static String getBlockKey(World world, IBlockState state) {
		Item stateItem = ItemBlock.getItemFromBlock(state.getBlock());
		if (stateItem == Items.AIR) {
			return state.getBlock().getTranslationKey();
		}
		ItemStack itemStack = new ItemStack(stateItem);
		int meta = state.getBlock().getMetaFromState(state);
		try {
			itemStack.setItemDamage(meta);
		} catch (NoSuchMethodError e) {
			if (!errdItems.contains(stateItem)) {
				errdItems.add(stateItem);
				EnvironmentalRads.logger.error(
						"The setItemDamage() method doesn't exist for '"+stateItem.getTranslationKey()+"'.");
			}
		}
		
		return getBlockKey(itemStack, stateItem);
	}
	
	public static String getEscapedUnlocalizedName(ItemStack stack, Item item) {
		return item.getTranslationKey(stack)
				.replaceAll("^"+(item.getTranslationKey().replaceAll(".", "\\.")), "");
	}
}
