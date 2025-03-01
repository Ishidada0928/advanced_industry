package com.aki.advanced_industry.registry;

import com.aki.mcutils.APICore.Utils.list.Pair;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;

import java.util.function.Consumer;

public class BlockRegister {
    private Block block;
    private Consumer<Pair<RegistryEvent.Register<Block>, Block>> DefaultBlockRegister;
    private Consumer<Pair<RegistryEvent.Register<Item>, Item>> DefaultItemRegister;
    private Consumer<Block> DefaultModelRegister;

    public BlockRegister(Block block, Consumer<Pair<RegistryEvent.Register<Block>, Block>> BlockRegister, Consumer<Pair<RegistryEvent.Register<Item>, Item>> ItemRegister, Consumer<Block> ModelRegister) {
        this.block = block;
        this.DefaultBlockRegister = BlockRegister;
        this.DefaultItemRegister = ItemRegister;
        this.DefaultModelRegister = ModelRegister;
    }

    public void RunBlockRegister(RegistryEvent.Register<Block> blockRegister) {
        this.DefaultBlockRegister.accept(new Pair<>(blockRegister, this.block));
    }

    public void RunItemRegister(RegistryEvent.Register<Item> itemRegister) {
        this.DefaultItemRegister.accept(new Pair<>(itemRegister, new ItemBlock(this.block).setRegistryName(this.block.getRegistryName())));
    }

    public void RunModelRegister() {
        this.DefaultModelRegister.accept(this.block);
    }
}
