package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.main.util.cmd.settings.DoubleSetting;
import dev.fiki.forgehax.main.util.entity.EntityUtils;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static dev.fiki.forgehax.main.Common.getLocalPlayer;
import static dev.fiki.forgehax.main.Common.getMountedEntity;

@RegisterMod(
    name = "HorseStats",
    description = "Change the stats of your horse",
    category = Category.PLAYER
)
public class HorseStats extends ToggleMod {
  private final DoubleSetting jumpHeight = newDoubleSetting()
      .name("jump-height")
      .description("Modified horse jump height attribute. Default: 1")
      .defaultTo(1.0D)
      .build();

  private final DoubleSetting speed = newDoubleSetting()
      .name("speed")
      .description("Modified horse speed attribute. Default: 0.3375")
      .defaultTo(0.3375D)
      .build();

  private final DoubleSetting multiplier = newDoubleSetting()
      .name("multiplier")
      .description("multiplier while sprinting")
      .defaultTo(1.0D)
      .build();

  @Override
  public void onDisabled() {
    if (getMountedEntity() instanceof AbstractHorseEntity) {
      applyStats(jumpHeight.getDefaultValue(), speed.getDefaultValue());
    }
  }

  @SubscribeEvent
  public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
    if (EntityUtils.isDrivenByPlayer(event.getEntity())
        && getMountedEntity() instanceof AbstractHorseEntity) {

      double newSpeed = speed.getValue();
      if (getLocalPlayer().isSprinting()) {
        newSpeed *= multiplier.getValue();
      }
      applyStats(jumpHeight.getValue(), newSpeed);
    }
  }

  private void applyStats(double newJump, double newSpeed) {
    LivingEntity living = (LivingEntity) getMountedEntity();
    if (living != null) {
      living.getAttribute(Attributes.HORSE_JUMP_STRENGTH)
          .setBaseValue(newJump);

      living.getAttribute(Attributes.MOVEMENT_SPEED)
          .setBaseValue(newSpeed);
    }
  }
}
