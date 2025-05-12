package co.lemee.brokentoolmod.mixin;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;


@Mixin(PlayerInventory.class)
public class MixinInventory {

    /**
     * @see PlayerInventory#main
     */
    @Shadow
    @Final
    public DefaultedList<ItemStack> main;

    @Unique
    private ReferenceArrayList<ItemStack> prevHotbar;

    /**
     * @see PlayerInventory#removeOne(ItemStack)
     */
    @Inject(at = @At("RETURN"), method = "removeOne(ILnet/minecraft/world/item/ItemStack;)V")
    private void autoswitch$setr(int slot, ItemStack stack, CallbackInfo ci) {
        handleHotbarUpdate(slot);
    }

    /**
     * @see PlayerInventory#removeItemNoUpdate(int)
     */
    @Inject(at = @At("RETURN"), method = "removeItemNoUpdate(I)Lnet/minecraft/world/item/ItemStack;")
    private void autoswitch$rmvs(int slot, CallbackInfoReturnable<ItemStack> cir) {
        handleHotbarUpdate(slot);
    }


    //todo these injects don't seem to cover everything anymore
    //  instead, redirct inventory creation to one that track the changes? that seems dangerous
    /**
     * If the sot changed is on the hotbar, pass to the HotbarWatcher and update the prevHotbar.
     *
     * @param slot slot changed
     */
    @Unique
    private void handleHotbarUpdate(int slot) {
        if (!Inventory.isHotbarSlot(slot)) return;

        List<ItemStack> hb = this.items.subList(0, getSelectionSize());
        HotbarWatcher.handleSlotChange(prevHotbar, hb);
        prevHotbar = new ReferenceArrayList<>(hb);
    }

}