package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLog;
import static com.matt.forgehax.asm.reflection.FastReflection.Fields.GuiConnecting_networkManager;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.gui.GuiConnecting;
import net.minecraft.network.login.server.SPacketLoginSuccess;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class InstantMessage extends ToggleMod {
  private final Setting<String> message =
      getCommandStub()
          .builders()
          .<String>newSettingBuilder()
          .name("message")
          .description("Message to send")
          .defaultTo("test")
          .build();

  public InstantMessage() {
    super(Category.MISC, "InstantMessage", false, "Send message as soon as you join");
  }

  @SubscribeEvent
  public void onPacketIn(PacketEvent.Incoming.Pre event) {
    if (event.getPacket() instanceof SPacketLoginSuccess) {
      if (MC.currentScreen instanceof GuiConnecting) {
        GuiConnecting_networkManager.get(MC.currentScreen)
            .sendPacket(new CPacketChatMessage(message.get()));
      } else getLog().warn("Did not send message as current screen is not GuiConnecting");
    }
  }
}
