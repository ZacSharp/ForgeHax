package com.matt.forgehax.mods;

import com.matt.forgehax.Globals;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.entity.LocalPlayerInventory;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.util.OptionalInt;
import java.util.stream.IntStream;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.matt.forgehax.Globals.*;

@RegisterMod
public class AutoTotemMod extends ToggleMod {
  
  private final int OFFHAND_SLOT = 45;
  
  public AutoTotemMod() {
    super(Category.COMBAT, "AutoTotem", false, "Automatically move totems to off-hand");
  }

  private final Setting<Boolean> allowGui =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("allow-gui")
          .description(
              "Lets AutoTotem work in menus.")
          .defaultTo(false)
          .build();

  @Override
  public String getDisplayText() {
    final long totemCount = IntStream.rangeClosed(9, 45) // include offhand slot
        .mapToObj(i -> LocalPlayerInventory.getContainer().getSlot(i).getStack().getItem())
        .filter(Items.TOTEM_OF_UNDYING::equals)
        .count();
    return String.format(super.getDisplayText() + "[%d]", totemCount);
  }
  
  @SubscribeEvent
  public void onPlayerUpdate(LocalPlayerUpdateEvent event) {
    if (!getOffhand().isEmpty() // if there's an item in offhand slot
        // if in inventory
        || (getDisplayScreen() != null && !allowGui.getAsBoolean())) {
      return; // if there's an item in offhand slot
    }
    
    findItem(Items.TOTEM_OF_UNDYING).ifPresent(slot -> {
      invPickup(slot);
      invPickup(OFFHAND_SLOT);
    });
  }
  
  private void invPickup(final int slot) {
    getPlayerController().windowClick(0, slot, 0, ClickType.PICKUP, getLocalPlayer());
  }
  
  private OptionalInt findItem(final Item item) {
    for (int i = 9; i <= 44; i++) {
      if (LocalPlayerInventory.getContainer().getSlot(i).getStack().getItem() == item) {
        return OptionalInt.of(i);
      }
    }
    return OptionalInt.empty();
  }
  
  private ItemStack getOffhand() {
    return getLocalPlayer().getItemStackFromSlot(EquipmentSlotType.OFFHAND);
  }
}
