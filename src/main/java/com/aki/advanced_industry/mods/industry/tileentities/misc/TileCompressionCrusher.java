package com.aki.advanced_industry.mods.industry.tileentities.misc;

import com.aki.advanced_industry.api.tile.TileEntityInventoryBase;
import com.aki.advanced_industry.mods.industry.recipe.CrushingRecipeUtils;
import com.aki.advanced_industry.api.tile.TileEntityBase;
import com.aki.mcutils.APICore.DataManage.DataListManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Function;

public class TileCompressionCrusher extends TileEntityInventoryBase {
    public int Step = 0;

    public ItemStack Out = ItemStack.EMPTY;

    public TileCompressionCrusher() {
        super(0, new ItemStackHandler() {
            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Nonnull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return ItemStack.EMPTY;
            }

            @Override
            @Nonnull
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                boolean flag = false;
                for (Function<ItemStack, ItemStack> stackFunction : CrushingRecipeUtils.CrushRecipeRules) {
                    ItemStack stack2 = stackFunction.apply(stack);
                    if (!this.IsItemStackEmpty(stack2)) {
                        flag = true;
                        break;
                    }
                }

                if (!flag) {
                    for (Map.Entry<ItemStack, Function<ItemStack, ItemStack>> entry : CrushingRecipeUtils.CrushRecipes.entrySet()) {
                        if (entry.getKey().getItem().equals(stack.getItem())) {
                            flag = true;
                        }
                    }
                }

                if (flag) {
                    return super.insertItem(slot, stack, simulate);
                } else {
                    return stack;
                }
            }

            public boolean IsItemStackEmpty(ItemStack stack) {
                return stack.isEmpty() || stack.getItem() == Items.AIR;
            }
        });
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if(!world.isRemote) {
            world.spawnEntity(new EntityItem(this.world, (double)this.getPos().getX() + 0.5d, (double)this.getPos().getY() - 0.2d, (double)this.getPos().getZ() + 0.5d, this.Inventory.getStackInSlot(0)));
        }
    }

    public void OnClick() {
        if(!world.isRemote) {
            ItemStack InStack1 = this.Inventory.getStackInSlot(0);
            if(!this.IsItemStackEmpty(InStack1)) {
                this.Step++;

                if (this.Step >= 10) {
                    this.Step = 0;
                    for(Function<ItemStack, ItemStack> stackFunction : CrushingRecipeUtils.CrushRecipeRules) {
                        if(this.IsItemStackEmpty(this.Out))
                            this.Out = stackFunction.apply(InStack1);
                        else break;
                    }

                    if(this.IsItemStackEmpty(this.Out)) {
                        for(Map.Entry<ItemStack, Function<ItemStack, ItemStack>> entry : CrushingRecipeUtils.CrushRecipes.entrySet()) {
                            if(entry.getKey().getItem().equals(InStack1.getItem())) {
                                this.Out = entry.getValue().apply(InStack1);
                                break;
                            }
                        }
                    }

                    this.Inventory.setStackInSlot(0, ItemStack.EMPTY);

                    world.spawnEntity(new EntityItem(this.world, (double)this.getPos().getX() + 0.5d, (double)this.getPos().getY() - 0.3d, (double)this.getPos().getZ() + 0.5d, this.Out));
                    this.Out = ItemStack.EMPTY;
                    this.sendUpdates();
                }
            }
        }
    }

    public void InputStack(ItemStack stack) {
        ItemStack stack1 = stack.copy();
        stack1.setCount(1);
        for(Function<ItemStack, ItemStack> stackFunction : CrushingRecipeUtils.CrushRecipeRules) {
            ItemStack stack2 = stackFunction.apply(stack1);
            if(!this.IsItemStackEmpty(stack2)) {
                this.Inventory.insertItem(0, stack1, false);
                this.Step = 0;
                stack.shrink(1);
                return;
            }
        }

        for(Map.Entry<ItemStack, Function<ItemStack, ItemStack>> entry : CrushingRecipeUtils.CrushRecipes.entrySet()) {
            if(entry.getKey().getItem().equals(stack1.getItem())) {
                this.Inventory.insertItem(0, stack1, false);
                this.Step = 0;
                stack.shrink(1);
                break;
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("Step", this.Step);
        compound.setTag("OutStack", this.Out.writeToNBT(new NBTTagCompound()));
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.Step = compound.getInteger("Step");
        this.Out = new ItemStack(compound.getCompoundTag("OutStack"));
    }

    @Override
    public DataListManager getNetWorkData() {
        DataListManager dataListManager = super.getNetWorkData();
        dataListManager.addData(this.Step);
        dataListManager.addData(this.Out.writeToNBT(new NBTTagCompound()));
        return dataListManager;
    }

    @Override
    public void ReceivePacketData(DataListManager dataListManager) {
        super.ReceivePacketData(dataListManager);
        this.Step = dataListManager.getDataInt();
        this.Out = new ItemStack(dataListManager.getDataTag());
    }

    public boolean IsItemStackEmpty(ItemStack stack) {
        return stack.isEmpty() || stack.getItem() == Items.AIR;
    }
}
