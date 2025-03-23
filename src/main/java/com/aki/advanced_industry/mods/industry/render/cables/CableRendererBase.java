package com.aki.advanced_industry.mods.industry.render.cables;

import com.aki.advanced_industry.AdvancedIndustryCore;
import com.aki.akisutils.apis.renderer.mqo_obj.AdvancedModelLoader;
import com.aki.akisutils.apis.renderer.mqo_obj.IModelCustom;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public abstract class CableRendererBase<T extends TileEntity> extends TileEntitySpecialRenderer<T> {
    public IModelCustom Bridge;
    public IModelCustom Pull;
    public IModelCustom Push;


    public CableRendererBase() {
        Bridge = AdvancedModelLoader.loadModel(new ResourceLocation(AdvancedIndustryCore.ModID, "models/block/obj/cable_bridge.obj"));
        Push = AdvancedModelLoader.loadModel(new ResourceLocation(AdvancedIndustryCore.ModID, "models/block/obj/cable_push.obj"));
        Pull = AdvancedModelLoader.loadModel(new ResourceLocation(AdvancedIndustryCore.ModID, "models/block/obj/cable_pull.obj"));
    }

    @Override
    public boolean isGlobalRenderer(T te) {
        return true;
    }
}
