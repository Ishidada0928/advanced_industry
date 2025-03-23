package com.aki.advanced_industry.registry;

import com.aki.akisutils.apis.util.list.Pair;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;

import java.util.function.Consumer;

public class ItemRegister {
    private Item item;
    private Consumer<Pair<RegistryEvent.Register<Item>, Item>> DefaultItemRegister;
    private Consumer<Item> DefaultModelRegister;

    public ItemRegister(Item item, Consumer<Pair<RegistryEvent.Register<Item>, Item>> ItemRegister, Consumer<Item> ModelRegister) {
        this.item = item;
        this.DefaultItemRegister = ItemRegister;
        this.DefaultModelRegister = ModelRegister;
    }

    public void RunItemRegister(RegistryEvent.Register<Item> itemRegister) {
        this.DefaultItemRegister.accept(new Pair<>(itemRegister, this.item));
    }

    public void RunModelRegister() {
        this.DefaultModelRegister.accept(this.item);
    }
}
