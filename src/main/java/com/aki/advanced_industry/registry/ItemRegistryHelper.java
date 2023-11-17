package com.aki.advanced_industry.registry;

import com.aki.mcutils.APICore.Utils.list.Pair;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ItemRegistryHelper {
    private final Consumer<Pair<RegistryEvent.Register<Item>, Item>> DefaultItemRegister = pair -> pair.getKey().getRegistry().register(pair.getValue());
    private final Consumer<Item> DefaultModelRegister = item -> ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    public ItemRegistryHelper() {}

    private final List<ItemRegister> registerList = new ArrayList<>();

    public ItemRegistryHelper Init() {
        this.registerList.clear();
        return this;
    }

    public ItemRegistryHelper add(Item item) {
        registerList.add(new ItemRegister(item, DefaultItemRegister, DefaultModelRegister));
        return this;
    }

    public ItemRegistryHelper addItemCustom(Item item, Consumer<Pair<RegistryEvent.Register<Item>, Item>> consumer) {
        registerList.add(new ItemRegister(item, consumer, DefaultModelRegister));
        return this;
    }

    public ItemRegistryHelper addModelCustom(Item item, Consumer<Item> consumer) {
        registerList.add(new ItemRegister(item, DefaultItemRegister, consumer));
        return this;
    }

    public void RegisterItem(RegistryEvent.Register<Item> event) {
        registerList.forEach(itemRegister -> itemRegister.RunItemRegister(event));
    }

    public void RegisterModel() {
        registerList.forEach(ItemRegister::RunModelRegister);
    }
}
