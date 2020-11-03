package somdudewillson.ncenvironmentalrads.utils;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class NameUtils {
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
			return state.getBlock().getUnlocalizedName();
		}
		ItemStack itemStack = stateItem.getDefaultInstance();
		itemStack.setItemDamage(state.getBlock().getMetaFromState(state));
		
		return getBlockKey(itemStack, stateItem);
	}
	
	public static String getEscapedUnlocalizedName(ItemStack stack, Item item) {
		return item.getUnlocalizedName(stack)
				.replaceAll("^"+(item.getUnlocalizedName().replaceAll(".", "\\.")), "");
	}
}
