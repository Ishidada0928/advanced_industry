package com.aki.advanced_industry.mods.industry.tileentities.machines;

import com.aki.advanced_industry.recipe.CrushingRecipeUtils;
import com.aki.advanced_industry.tile.TileEntityBase;
import com.aki.mcutils.APICore.DataManage.DataListManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;

import java.util.Map;
import java.util.function.Function;

public class TileCompressionCrusher extends TileEntityBase {
    public int Step = 0;
    public ItemStack InStack = ItemStack.EMPTY;

    public ItemStack Out = ItemStack.EMPTY;

    public TileCompressionCrusher() {

    }

    @Override
    public void invalidate() {
        super.invalidate();
        if(!world.isRemote) {
            world.spawnEntity(new EntityItem(this.world, (double)this.getPos().getX() + 0.5d, (double)this.getPos().getY() - 0.2d, (double)this.getPos().getZ() + 0.5d, this.InStack));
        }
    }

    public void OnClick() {
        if(!world.isRemote) {
            if(!this.IsItemStackEmpty(this.InStack)) {
                this.Step++;

                if (this.Step >= 10) {
                    this.Step = 0;
                    for(Function<ItemStack, ItemStack> stackFunction : CrushingRecipeUtils.CrushRecipeRules) {
                        if(this.IsItemStackEmpty(this.Out))
                            this.Out = stackFunction.apply(this.InStack);
                        else break;
                    }
                    if(this.IsItemStackEmpty(this.Out)) {
                        for(Map.Entry<ItemStack, Function<ItemStack, ItemStack>> entry : CrushingRecipeUtils.CrushRecipes.entrySet()) {
                            if(entry.getKey().getItem().equals(this.InStack.getItem())) {
                                this.Out = entry.getValue().apply(this.InStack);
                                break;
                            }
                        }
                    }
                    this.InStack = ItemStack.EMPTY;

                    world.spawnEntity(new EntityItem(this.world, (double)this.getPos().getX() + 0.5d, (double)this.getPos().getY() - 0.2d, (double)this.getPos().getZ() + 0.5d, this.Out));
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
                this.InStack = stack1;
                this.Step = 0;
                stack.shrink(1);
                return;
            }
        }

        for(Map.Entry<ItemStack, Function<ItemStack, ItemStack>> entry : CrushingRecipeUtils.CrushRecipes.entrySet()) {
            if(entry.getKey().getItem().equals(stack1.getItem())) {
                System.out.println("OK");
                this.InStack = stack1;
                this.Step = 0;
                stack.shrink(1);
                break;
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("Step", this.Step);
        compound.setTag("InStack", this.InStack.writeToNBT(new NBTTagCompound()));
        compound.setTag("OutStack", this.Out.writeToNBT(new NBTTagCompound()));
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.Step = compound.getInteger("Step");
        this.InStack = new ItemStack(compound.getCompoundTag("InStack"));
        this.Out = new ItemStack(compound.getCompoundTag("OutStack"));
    }

    @Override
    public DataListManager getNetWorkData() {
        DataListManager dataListManager = super.getNetWorkData();
        dataListManager.addData(this.Step);
        dataListManager.addData(this.InStack.writeToNBT(new NBTTagCompound()));
        dataListManager.addData(this.Out.writeToNBT(new NBTTagCompound()));
        return dataListManager;
    }

    @Override
    public void ReceivePacketData(DataListManager dataListManager) {
        super.ReceivePacketData(dataListManager);
        this.Step = dataListManager.getDataInt();
        this.InStack = new ItemStack(dataListManager.getDataTag());
        this.Out = new ItemStack(dataListManager.getDataTag());
    }

    public boolean IsItemStackEmpty(ItemStack stack) {
        return stack.isEmpty() || stack.getItem() == Items.AIR;
    }
}
