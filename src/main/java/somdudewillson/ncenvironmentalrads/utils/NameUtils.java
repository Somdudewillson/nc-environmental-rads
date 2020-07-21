package somdudewillson.ncenvironmentalrads.utils;

import net.minecraft.block.state.IBlockState;
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
	
	public static String getBlockKey(ItemStack itemBlockStack, ItemBlock itemBlock) {
		String name = itemBlock.getRegistryName().toString();
		name += itemBlock.getUnlocalizedName(itemBlockStack)
				.replaceAll("^"+(itemBlock.getUnlocalizedName().replaceAll(".", "\\.")), "");
		
		return name;
	}
	
	public static String getBlockKey(World world, IBlockState state) {
		
		ItemBlock itemBlock = (ItemBlock) ItemBlock.getItemFromBlock(state.getBlock());
		ItemStack itemBlockStack = itemBlock.getDefaultInstance();
		itemBlockStack.setItemDamage(state.getBlock().getMetaFromState(state));
		
		return getBlockKey(itemBlockStack, itemBlock);
	}
}
