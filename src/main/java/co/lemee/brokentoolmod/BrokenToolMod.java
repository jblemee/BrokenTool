package co.lemee.brokentoolmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class BrokenToolMod implements ModInitializer {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "brokentoolmod";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Broken Tool Mod Initialized");
        
        // Register a listener for after block break events
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            ItemStack mainHandStack = player.getMainHandStack();
            
            // If the main hand is empty, it might be because a tool just broke
            if (mainHandStack.isEmpty()) {
                // Try to find a tool to replace it with
                PlayerInventory playerInventory = player.getInventory();
                
                // The item that broke is the one that was just used to break the block
                // We need to guess the type of tool that was used based on the broken block
                Item brokenItem = findProbableToolTypeFromBrokenBlock(state, world, pos);
                if (brokenItem == null) return;
                
                replaceWithSimilarTool(playerInventory, brokenItem);
            } else if (mainHandStack.isDamageable() && mainHandStack.getDamage() == mainHandStack.getMaxDamage() - 1) {
                // Tool is about to break (one use left)
                Item aboutToBreakItem = mainHandStack.getItem();
                PlayerInventory playerInventory = player.getInventory();
                
                // Find a replacement tool of the same type
                prepareReplacementTool(playerInventory, aboutToBreakItem);
            }
        });
        
        // We also need to catch tools that break during item use (not block breaking)
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getPlayerManager().getPlayerList().forEach(player -> {
                ItemStack mainHandStack = player.getMainHandStack();
                
                // If main hand is empty, check if a tool just broke
                if (mainHandStack.isEmpty()) {
                    // We'll try to detect if a tool broke during use
                    // This is a fallback method for tools that break during use not mining
                    PlayerInventory playerInventory = player.getInventory();
                    
                    // Since we don't know what broke, try to find any tool in inventory 
                    // (not ideal but better than nothing)
                    for (ItemStack stack : playerInventory.main) {
                        if (!stack.isEmpty() && stack.isDamageable()) {
                            replaceWithSimilarTool(playerInventory, stack.getItem());
                            break;
                        }
                    }
                }
            });
        });
    }
    
    // This is a helper method to find a replacement tool
    private void replaceWithSimilarTool(PlayerInventory playerInventory, Item brokenItem) {
        List<ItemStack> sameTools = playerInventory.main.stream()
                .filter(stack -> !stack.isEmpty())
                .filter(stack -> stack.getItem().getClass() == brokenItem.getClass())
                .collect(Collectors.toList());
        
        if (!sameTools.isEmpty()) {
            // Try to find a tool of the same material first
            List<ItemStack> sameMaterialTool = sameTools.stream()
                    .filter(stack -> stack.getItem().getTranslationKey().equals(brokenItem.getTranslationKey()))
                    .collect(Collectors.toList());
            
            ItemStack newTool = !sameMaterialTool.isEmpty() ? sameMaterialTool.get(0) : sameTools.get(0);
            if (newTool != null) {
                // Find the slot with the replacement tool
                int slotWithTool = -1;
                for (int i = 0; i < playerInventory.main.size(); i++) {
                    if (playerInventory.main.get(i) == newTool) {
                        slotWithTool = i;
                        break;
                    }
                }
                
                if (slotWithTool != -1) {
                    // Replace the broken tool with the new one
                    playerInventory.main.set(playerInventory.selectedSlot, newTool);
                    playerInventory.main.set(slotWithTool, ItemStack.EMPTY);
                    LOGGER.info("Replaced broken tool with a similar one");
                }
            }
        }
    }
    
    // Helper to prepare a replacement tool before the current one breaks
    private void prepareReplacementTool(PlayerInventory playerInventory, Item aboutToBreakItem) {
        // We don't actually swap the tool yet, just log that we found one about to break
        LOGGER.info("Tool is about to break: {}", aboutToBreakItem.getTranslationKey());
    }
    
    // This method tries to determine what kind of tool was likely used to break a block
    private Item findProbableToolTypeFromBrokenBlock(net.minecraft.block.BlockState state, World world, BlockPos pos) {
        // This is a placeholder implementation - in a real mod you would 
        // use more sophisticated logic to determine the tool type
        // For now, we just return null to indicate we couldn't determine the tool
        return null;
    }
}