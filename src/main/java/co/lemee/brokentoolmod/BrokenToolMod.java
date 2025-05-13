package co.lemee.brokentoolmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class BrokenToolMod implements ModInitializer {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "brokentoolmod";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Broken Tool Mod initialized");

        // Register event to handle item breaks
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            // Check if the item is about to break (1 durability left)
            if (stack.isDamageable() && stack.getDamage() == stack.getMaxDamage() - 1) {
                Item brokenItem = stack.getItem();
                PlayerInventory playerInventory = player.getInventory();

                // Get all non-equipment items in inventory
                List<ItemStack> nonEquipmentItems = playerInventory.main.stream()
                        .filter(itemStack -> !itemStack.isEmpty() && !itemStack.equals(stack))
                        .toList();

                // Check if any of them match our broken item
                List<ItemStack> sameTools = nonEquipmentItems.stream()
                        .filter(x -> x.getItem().getClass() == brokenItem.getClass())
                        .toList();

                if (!sameTools.isEmpty()) {
                    // Try to find tools of the same material
                    List<ItemStack> sameMaterialTool = sameTools.stream()
                            .filter(x -> x.getItem().getTranslationKey().equals(brokenItem.getTranslationKey()))
                            .toList();

                    ItemStack newTool = !sameMaterialTool.isEmpty() ? sameMaterialTool.get(0) : sameTools.get(0);

                    // Flag to ensure we only replace the tool once
                    final AtomicBoolean hasReplaced = new AtomicBoolean(false);

                    // Register a tick callback to replace the tool after it breaks
                    ServerTickEvents.END_SERVER_TICK.register(server -> {
                        // Only execute once and only if the item is now empty (broken)
                        if (!hasReplaced.get() && stack.isEmpty()) {
                            // The original tool broke, replace it
                            int slot = playerInventory.getSlotWithStack(newTool);
                            if (slot >= 0) {
                                ItemStack replacementTool = newTool.copy();
                                playerInventory.removeStack(slot);
                                playerInventory.setStack(playerInventory.selectedSlot, replacementTool);
                                LOGGER.info("Replaced broken tool with " + replacementTool.getName().getString());
                            }
                            // Mark as replaced so this doesn't run again
                            hasReplaced.set(true);
                        }
                    });
                }
            }

            // Return unchanged result
            return ActionResult.PASS;
        });
    }
}