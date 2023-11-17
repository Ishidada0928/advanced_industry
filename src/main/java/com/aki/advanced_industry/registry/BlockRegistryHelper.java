package com.aki.advanced_industry.registry;

import com.aki.mcutils.APICore.Utils.list.Pair;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BlockRegistryHelper {
    private final Consumer<Pair<RegistryEvent.Register<Block>, Block>> DefaultBlockRegister = pair -> pair.getKey().getRegistry().register(pair.getValue());
    private final Consumer<Pair<RegistryEvent.Register<Item>, Item>> DefaultItemRegister = pair -> pair.getKey().getRegistry().register(pair.getValue());
    private final Consumer<Block> DefaultModelRegister = block -> ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, new ModelResourceLocation(block.getRegistryName(), "inventory"));
    public BlockRegistryHelper() {}

    private final List<BlockRegister> registerList = new ArrayList<>();

    public BlockRegistryHelper Init() {
        this.registerList.clear();
        return this;
    }

    public BlockRegistryHelper add(Block block) {
        registerList.add(new BlockRegister(block, DefaultBlockRegister, DefaultItemRegister, DefaultModelRegister));
        return this;
    }

    public BlockRegistryHelper addBlockCustom(Block block, Consumer<Pair<RegistryEvent.Register<Block>, Block>> consumer) {
        registerList.add(new BlockRegister(block, consumer, DefaultItemRegister, DefaultModelRegister));
        return this;
    }

    public BlockRegistryHelper addItemCustom(Block block, Consumer<Pair<RegistryEvent.Register<Item>, Item>> consumer) {
        registerList.add(new BlockRegister(block, DefaultBlockRegister, consumer, DefaultModelRegister));
        return this;
    }

    public BlockRegistryHelper addModelCustom(Block block, Consumer<Block> consumer) {
        registerList.add(new BlockRegister(block, DefaultBlockRegister, DefaultItemRegister, consumer));
        return this;
    }

    public void RegisterBlock(RegistryEvent.Register<Block> event) {
        registerList.forEach(blockRegister -> blockRegister.RunBlockRegister(event));
    }

    public void RegisterItem(RegistryEvent.Register<Item> event) {
        registerList.forEach(blockRegister -> blockRegister.RunItemRegister(event));
    }

    public void RegisterModel() {
        registerList.forEach(BlockRegister::RunModelRegister);
    }
}
