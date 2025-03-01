package com.aki.advanced_industry.api.gui.container;

import com.aki.advanced_industry.api.tile.TileEntityBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ModContainerBase extends Container {
    public TileEntityBase inventoryTile;
    public EntityPlayer player;
    public InventoryPlayer player_inv;

    public ModContainerBase(TileEntityBase inventoryTile, EntityPlayer player) {
        this.inventoryTile = inventoryTile;
        this.player = player;
        this.player_inv = this.player.inventory;

        if(inventoryTile.HasPlayerInventory()) {
            for (int y = 0; y < 3; y++) {
                for (int i = 0; i < 9; i++)
                    //27 = 9
                    //26 + 27 = 53
                    addSlotToContainer(new Slot(this.player_inv, i + y * 9 + 9, 12 + i * 18, 102 + y * 18));
            }
            for (int x = 0; x < 9; x++)
                //
                addSlotToContainer(new Slot(this.player_inv, x, 12 + x * 18, 160));
        }
    }

    //merge
    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        Slot slot = this.inventorySlots.get(index);//ShiftクリックされたSlot
        ItemStack stack1 = ItemStack.EMPTY;
        if(slot != null && slot.getHasStack()) {
            ItemStack stack2 = slot.getStack();
            stack1 = stack2.copy();
            if(index <= 53) {//インベントリ内から。
                if(!mergeItemStack(stack2, 53, this.inventorySlots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else {//Tile側
                if(!mergeItemStack(stack2, 0, 53, true)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack2.getCount() == 0) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (stack1.getCount() == stack2.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stack2);
        }
        return stack1;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }
}