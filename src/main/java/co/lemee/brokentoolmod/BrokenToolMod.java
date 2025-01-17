package co.lemee.brokentoolmod;

import com.mojang.logging.LogUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.List;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(BrokenToolMod.MODID)
public class BrokenToolMod
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "brokentoolmod";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public BrokenToolMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("Broken Tool SETUP");
    }

    @SubscribeEvent
    public void onItemBreak(PlayerDestroyItemEvent itemEvent) {
        ItemStack brokenItemStack = itemEvent.getOriginal();
        Inventory playerInventory = itemEvent.getEntity().getInventory();
        if(playerInventory.items.stream().anyMatch(x -> x.equals(brokenItemStack))) {
            return;
        }
        Item brokenItem = brokenItemStack.getItem();
        List<ItemStack> sameTools = playerInventory.items.stream().filter(x -> x.getItem().getClass() == brokenItem.getClass()).toList();
        if (!sameTools.isEmpty()) {
            List<ItemStack> sameMaterialTool = sameTools.stream()
                    .filter(x -> x.getDescriptionId().equals(brokenItem.getDescriptionId())).toList();
            ItemStack newTool = !sameMaterialTool.isEmpty() ? sameMaterialTool.get(0) : sameTools.get(0);
            if (newTool != null) {
                playerInventory.add(playerInventory.selected, newTool);
                playerInventory.removeItem(newTool);
            }
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("Broken Tool ACTIVE");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            LOGGER.info("Broken Tool SETUP");
        }
    }
}
