package vazkii.patchouli.common.handler;

import net.minecraft.block.BlockState;
import net.minecraft.block.LecternBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.LecternTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import vazkii.patchouli.api.PatchouliAPI;
import vazkii.patchouli.common.book.Book;
import vazkii.patchouli.common.util.ItemStackUtil;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LecternEventHandler {

	@SubscribeEvent
	static void rightClick(PlayerInteractEvent.RightClickBlock event) {
		World world = event.getWorld();
		BlockPos pos = event.getPos();
		BlockState state = world.getBlockState(pos);
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity instanceof LecternTileEntity) {
			PlayerEntity player = event.getPlayer();
			if (state.get(LecternBlock.HAS_BOOK)) {
				if (player.isSneaking()) {
					takeBook(player, (LecternTileEntity) tileEntity);
				} else {
					Book book = ItemStackUtil.getBookFromStack(((LecternTileEntity) tileEntity).getBook());
					if (book != null) {
						if (!world.isRemote) {
							PatchouliAPI.get().openBookGUI((ServerPlayerEntity) player, book.id);
						}
						event.setUseBlock(Event.Result.DENY);
					}
				}
			} else {
				ItemStack stack = event.getItemStack();
				if (ItemStackUtil.getBookFromStack(stack) != null) {
					if (LecternBlock.tryPlaceBook(world, pos, state, stack)) {
						event.setUseItem(Event.Result.ALLOW);
					}
				}
			}
		}
	}

	private static void takeBook(PlayerEntity player, LecternTileEntity tileEntity) {
		ItemStack itemstack = tileEntity.getBook();
		tileEntity.setBook(ItemStack.EMPTY);
		LecternBlock.setHasBook(tileEntity.getWorld(), tileEntity.getPos(), tileEntity.getBlockState(), false);
		if (!player.inventory.addItemStackToInventory(itemstack)) {
			player.dropItem(itemstack, false);
		}
	}
}
