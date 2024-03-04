package com.aki.advanced_industry;

import buildcraft.core.item.ItemWrench_Neptune;
import cofh.api.item.IToolHammer;
import com.aki.advanced_industry.mods.industry.items.tools.ItemWrench;
import com.aki.advanced_industry.mods.industry.render.cables.energy.TileRenderEnergyCable;
import com.aki.advanced_industry.mods.industry.render.cables.fluid.TileRenderFluidCable;
import com.aki.advanced_industry.mods.industry.render.machines.TileRenderCompressionCrusher;
import com.aki.advanced_industry.mods.industry.tileentities.TileLagChecker;
import com.aki.advanced_industry.mods.industry.tileentities.cables.energy.*;
import com.aki.advanced_industry.mods.industry.tileentities.cables.fluid.*;
import com.aki.advanced_industry.mods.industry.tileentities.machines.TileCompressionCrusher;
import com.aki.advanced_industry.mods.industry.util.IBlockFacingBound;
import com.aki.advanced_industry.mods.industry.util.IMachineConfiguration;
import com.aki.advanced_industry.mods.industry.util.WrenchUtil;
import com.aki.advanced_industry.packet.PacketTileData;
import com.aki.advanced_industry.packet.PacketTileDataRequest;
import com.aki.advanced_industry.packet.PacketTileGuiUpdate;
import com.aki.advanced_industry.recipe.CrushingRecipeUtils;
import com.aki.advanced_industry.registry.BlockRegistryHelper;
import com.aki.advanced_industry.registry.ItemRegistryHelper;
import com.aki.advanced_industry.tile.handler.TileEntityChunkLoadingHandler;
import com.aki.advanced_industry.util.RaytraceUtil;
import com.aki.mcutils.APICore.Utils.list.Pair;
import com.mojang.authlib.GameProfile;
import crazypants.enderio.base.item.yetawrench.ItemYetaWrench;
import mekanism.api.IMekWrench;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod(modid = AdvancedIndustryCore.ModID, name = AdvancedIndustryCore.ModName, version = AdvancedIndustryCore.ModVersion)
public class AdvancedIndustryCore {
    public static final String ModID = "advanced_industry";
    public static final String ModName = "AdvancedIndustry";
    public static final String ModVersion = "1.0.0-Snapshot";

    public static final int ModPriority = 1007;

    @Mod.Instance(ModID)
    public static AdvancedIndustryCore INSTANCE;

    public static Logger logger;

    public static GameProfile modgameprofile = new GameProfile(UUID.nameUUIDFromBytes("AdvancedIndustry".getBytes(StandardCharsets.UTF_8)),"[AdvancedIndustry]");
    public static SimpleNetworkWrapper wrapper = NetworkRegistry.INSTANCE.newSimpleChannel(ModID);

    public static BlockRegistryHelper blockRegistryHelper = new BlockRegistryHelper();
    public static ItemRegistryHelper itemRegistryHelper = new ItemRegistryHelper();

    @Mod.EventHandler
    public void construct(FMLConstructionEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * This is the first initialization event. Register tile entities here.
     * The registry events below will have fired prior to entry to this method.
     */
    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);

        /**
         * renderの初めに必ず読み込む
         * */
        OBJLoader.INSTANCE.addDomain(ModID);

        logger = event.getModLog();
        wrapper.registerMessage(new PacketTileData.HandlerClient(), PacketTileData.class, 0, Side.CLIENT);
        wrapper.registerMessage(new PacketTileDataRequest.HandlerServer(), PacketTileDataRequest.class, 1, Side.SERVER);
        wrapper.registerMessage(new PacketTileGuiUpdate(), PacketTileGuiUpdate.TileEntityGUIMessage.class, 2, Side.SERVER);

        ClientRegistry.bindTileEntitySpecialRenderer(TileCompressionCrusher.class, new TileRenderCompressionCrusher());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEnergyCableBase.class, new TileRenderEnergyCable());
        ClientRegistry.bindTileEntitySpecialRenderer(TileFluidCableBase.class, new TileRenderFluidCable());

        ModMaterials.RegisterBlock();
        ModMaterials.RegisterItem();
    }

    /**
     * This is the second initialization event. Register custom recipes
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);

        GameRegistry.registerTileEntity(TileCompressionCrusher.class, new ResourceLocation(ModID, "TileCompressionCrusher"));
        //cable
        //energy
        GameRegistry.registerTileEntity(TileBasicEnergyCable.class, new ResourceLocation(ModID, "TileBasicEnergyCable"));
        GameRegistry.registerTileEntity(TileAdvancedEnergyCable.class, new ResourceLocation(ModID, "TileAdvancedEnergyCable"));
        GameRegistry.registerTileEntity(TileExtremeEnergyCable.class, new ResourceLocation(ModID, "TileExtremeEnergyCable"));
        GameRegistry.registerTileEntity(TileUltimateEnergyCable.class, new ResourceLocation(ModID, "TileUltimateEnergyCable"));
        //fluid
        GameRegistry.registerTileEntity(TileBasicFluidCable.class, new ResourceLocation(ModID, "TileBasicFluidCable"));
        GameRegistry.registerTileEntity(TileAdvancedFluidCable.class, new ResourceLocation(ModID, "TileAdvancedFluidCable"));
        GameRegistry.registerTileEntity(TileExtremeFluidCable.class, new ResourceLocation(ModID, "TileExtremeFluidCable"));
        GameRegistry.registerTileEntity(TileUltimateFluidCable.class, new ResourceLocation(ModID, "TileUltimateFluidCable"));

        GameRegistry.registerTileEntity(TileLagChecker.class, new ResourceLocation(ModID, "TileLagChecker"));

        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, new ModGuiHandler());


        ForgeChunkManager.setForcedChunkLoadingCallback(INSTANCE, new TileEntityChunkLoadingHandler());

        OreDictionary.registerOre("oreCopper", ModMaterials.copperOre);
        OreDictionary.registerOre("oreLead", ModMaterials.leadOre);
        OreDictionary.registerOre("oreNickel", ModMaterials.nickelOre);
        OreDictionary.registerOre("oreOsmium", ModMaterials.osmiumOre);
        OreDictionary.registerOre("oreSilver", ModMaterials.silverOre);
        OreDictionary.registerOre("oreTin", ModMaterials.tinOre);

        OreDictionary.registerOre("dustCopper", ModMaterials.copperDust);
        OreDictionary.registerOre("dustLead", ModMaterials.leadDust);
        OreDictionary.registerOre("dustNickel", ModMaterials.nickelDust);
        OreDictionary.registerOre("dustOsmium", ModMaterials.osmiumDust);
        OreDictionary.registerOre("dustSilver", ModMaterials.silverDust);
        OreDictionary.registerOre("dustTin", ModMaterials.tinDust);
        OreDictionary.registerOre("dustSulfur", ModMaterials.sulfurDust);
        OreDictionary.registerOre("dustIron", ModMaterials.ironDust);
        OreDictionary.registerOre("dustGold", ModMaterials.goldDust);
        OreDictionary.registerOre("dustDiamond", ModMaterials.diamondDust);
        OreDictionary.registerOre("dustObsidian", ModMaterials.obsidianDust);

        OreDictionary.registerOre("ingotCopper", new ItemStack(ModMaterials.copperIngot));
        OreDictionary.registerOre("ingotLead", new ItemStack(ModMaterials.leadIngot));
        OreDictionary.registerOre("ingotNickel", ModMaterials.nickelIngot);
        OreDictionary.registerOre("ingotOsmium", ModMaterials.osmiumIngot);
        OreDictionary.registerOre("ingotSilver", ModMaterials.silverIngot);
        OreDictionary.registerOre("ingotTin", ModMaterials.tinIngot);

        CrushingRecipeUtils.Init();
    }

    /**
     * This is the final initialization event. Register actions from other mods here
     */
    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent event) {
        //My Mod
        WrenchUtil.AddWrench((item) -> item instanceof ItemWrench);

        if(Loader.isModLoaded("enderio"))
            WrenchUtil.AddWrench((item) -> item instanceof ItemYetaWrench);
        if(Loader.isModLoaded("mekanism"))
            WrenchUtil.AddWrench((item) -> item instanceof IMekWrench);
        if(Loader.isModLoaded("buildcraft"))
            WrenchUtil.AddWrench((item) -> item instanceof ItemWrench_Neptune);
        if(Loader.isModLoaded("cofhcore"))
            WrenchUtil.AddWrench((item) -> item instanceof IToolHammer);
    }

    @SubscribeEvent
    public void OnRightClickEvent(PlayerInteractEvent.RightClickBlock rightClickBlock) {
        EntityPlayer playerIn = rightClickBlock.getEntityPlayer();
        World worldIn = rightClickBlock.getWorld();
        EnumHand handIn = rightClickBlock.getHand();
        BlockPos pos = rightClickBlock.getPos();
        if(!playerIn.isSpectator() && WrenchUtil.PlayerHasWrench(playerIn)) {
            ItemStack stack = playerIn.getHeldItem(handIn);
            if(!stack.isEmpty() && !worldIn.isRemote) {
                TileEntity tile = worldIn.getTileEntity(pos);
                Pair<Vec3d, Vec3d> pair = RaytraceUtil.getRayTraceVectors(playerIn);
                RayTraceResult rayTraceResult = worldIn.rayTraceBlocks(pair.getKey(), pair.getValue());
                EnumFacing facing = rightClickBlock.getFace();
                Block block = worldIn.getBlockState(pos).getBlock();
                if(block instanceof IBlockFacingBound) {
                    if(rayTraceResult != null) {
                        double dist = Double.POSITIVE_INFINITY;
                        Pair<Vec3d, Vec3d> vecs = RaytraceUtil.getRayTraceVectors(playerIn);
                        for (Map.Entry<EnumFacing, AxisAlignedBB> entry : ((IBlockFacingBound) block).getFacingBoundingBox(worldIn, pos, playerIn, WrenchUtil.PlayerHasWrench(playerIn)).entrySet()) {
                            RayTraceResult result = entry.getValue().offset(pos).calculateIntercept(vecs.getKey(), vecs.getValue());
                            if (result != null && dist > result.hitVec.distanceTo(vecs.getKey())) {
                                dist = result.hitVec.distanceTo(vecs.getKey());
                                facing = entry.getKey();
                                rayTraceResult = result;
                            }
                        }
                    }
                }
                if(tile instanceof IMachineConfiguration && rayTraceResult != null) {
                    if(playerIn.isSneaking()) {
                        rightClickBlock.setCancellationResult(((IMachineConfiguration) tile).onSneakRightClick(playerIn, facing, rayTraceResult));
                    } else {
                        rightClickBlock.setCancellationResult(((IMachineConfiguration) tile).onRightClick(playerIn, facing, rayTraceResult));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        //GameRegistry.addShapedRecipe(airCane.getRegistryName(), new ResourceLocation("AAB,CDA,DCA"), new ItemStack(airCane, 1));
        /*OreDictionary.registerOre("dustCorrupted", Materials.CORRUPTED_ESSENCE.getStack());
        OreDictionary.registerOre("ingotCorrupted", Materials.CORRUPTED_INGOT.getStack());
        OreDictionary.registerOre("dustVile", Materials.VILE_DUST.getStack());

        GameRegistry.addSmelting(new ItemStack(Blocks.SOUL_SAND), Materials.VILE_DUST.getStack(), 0.75F);*/
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        itemRegistryHelper.RegisterItem(event);
        blockRegistryHelper.RegisterItem(event);
    }

    public List<Item> getRegisterItems(Block... blocks) {
        List<Item> itemList = new ArrayList<>();
        for(Block block : blocks) {
            itemList.add(BlockToItem(block));
        }
        return itemList;
    }

    public Item BlockToItem(Block block) {
        return new ItemBlock(block).setRegistryName(block.getRegistryName());
    }

    //ブロックを登録するイベント。 旧preinitのタイミングで発火する。
    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        blockRegistryHelper.RegisterBlock(event);
    }

    //モデルを登録するイベント。SideOnlyによってクライアント側のみ呼ばれる。旧preinitのタイミングで発火する。
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerModels(ModelRegistryEvent event) {
        itemRegistryHelper.RegisterModel();
        blockRegistryHelper.RegisterModel();
                /**
                 *
                 * 注意
                 * {
                 *     "Model": "~~~"
                 * }
                 *
                 * のようなものを作るとき、"~~~"の中にそのディレクトリの名前が入っていないか確認。
                 *
                 * 例:
                 * ///models/block/block_laser.json <-ディレクトリ
                 * {
                 *     "Model": "block/block_laser"
                 * }
                 * これは、block/が入っているため
                 * [models/block/]の中の[block/]を探そうとしているためうまくいかない。
                 *
                 * 正しくは、
                 * ///models/block/block_laser.json <-ディレクトリ
                 * {
                 *     "Model": "block_laser"
                 * }
                 * */
    }
}
