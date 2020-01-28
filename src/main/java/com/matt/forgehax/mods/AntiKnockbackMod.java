package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.ApplyCollisionMotionEvent;
import com.matt.forgehax.asm.events.EntityBlockSlipApplyEvent;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.asm.events.PushOutOfBlocksEvent;
import com.matt.forgehax.asm.events.WaterMovementEvent;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.math.VectorUtils;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SEntityStatusPacket;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.network.play.server.SExplosionPacket;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.matt.forgehax.Globals.*;

@RegisterMod
public class AntiKnockbackMod extends ToggleMod {
  
  private final Setting<Double> multiplier_x =
      getCommandStub()
          .builders()
          .<Double>newSettingBuilder()
          .name("x-multiplier")
          .description("Multiplier for X axis")
          .defaultTo(0.D)
          .build();
  
  private final Setting<Double> multiplier_y =
      getCommandStub()
          .builders()
          .<Double>newSettingBuilder()
          .name("y-multiplier")
          .description("Multiplier for Y axis")
          .defaultTo(0.D)
          .build();
  
  private final Setting<Double> multiplier_z =
      getCommandStub()
          .builders()
          .<Double>newSettingBuilder()
          .name("z-multiplier")
          .description("Multiplier for Z axis")
          .defaultTo(0.D)
          .build();
  
  private final Setting<Boolean> explosions =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("explosions")
          .description("Disable velocity from SPacketExplosion")
          .defaultTo(true)
          .build();
  
  private final Setting<Boolean> velocity =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("velocity")
          .description("Disable velocity from SPacketEntityVelocity")
          .defaultTo(true)
          .build();
  
  private final Setting<Boolean> fishhook =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("fishhook")
          .description("Disable velocity from a fishhook")
          .defaultTo(true)
          .build();
  
  private final Setting<Boolean> water =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("water")
          .description("Disable velocity from flowing water")
          .defaultTo(true)
          .build();
  
  private final Setting<Boolean> push =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("push")
          .description("Disable velocity from entity pushing")
          .defaultTo(true)
          .build();
  
  private final Setting<Boolean> blocks =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("blocks")
          .description("Disable velocity from block pushing")
          .defaultTo(true)
          .build();
  
  private final Setting<Boolean> slipping =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("slipping")
          .description("Disable velocity from ice slipping")
          .defaultTo(true)
          .build();
  
  public AntiKnockbackMod() {
    super(Category.COMBAT, "AntiKnockback", false, "Removes knockback movement");
  }
  
  private Vec3d getMultiplier() {
    return new Vec3d(multiplier_x.get(), multiplier_y.get(), multiplier_z.get());
  }
  
  private Vec3d getPacketMotion(IPacket<?> packet) {
    if (packet instanceof SExplosionPacket) {
      return new Vec3d(
          FastReflection.Fields.SPacketExplosion_motionX.get(packet),
          FastReflection.Fields.SPacketExplosion_motionY.get(packet),
          FastReflection.Fields.SPacketExplosion_motionZ.get(packet));
    } else if (packet instanceof SEntityVelocityPacket) {
      return new Vec3d(
          FastReflection.Fields.SPacketEntityVelocity_motionX.get(packet),
          FastReflection.Fields.SPacketEntityVelocity_motionY.get(packet),
          FastReflection.Fields.SPacketEntityVelocity_motionZ.get(packet));
    } else {
      throw new IllegalArgumentException();
    }
  }
  
  private void setPacketMotion(IPacket<?> packet, Vec3d in) {
    if (packet instanceof SExplosionPacket) {
      FastReflection.Fields.SPacketExplosion_motionX.set(packet, (float) in.x);
      FastReflection.Fields.SPacketExplosion_motionY.set(packet, (float) in.y);
      FastReflection.Fields.SPacketExplosion_motionZ.set(packet, (float) in.z);
    } else if (packet instanceof SEntityVelocityPacket) {
      FastReflection.Fields.SPacketEntityVelocity_motionX.set(packet, (int) Math.round(in.x));
      FastReflection.Fields.SPacketEntityVelocity_motionY.set(packet, (int) Math.round(in.y));
      FastReflection.Fields.SPacketEntityVelocity_motionZ.set(packet, (int) Math.round(in.z));
    } else {
      throw new IllegalArgumentException();
    }
  }
  
  private void addEntityVelocity(Entity in, Vec3d velocity) {
    in.setMotion(in.getMotion().add(velocity));
  }
  
  /**
   * Stops TNT and knockback velocity
   */
  @SubscribeEvent
  public void onPacketReceived(PacketEvent.Incoming.Pre event) {
    if (!isInWorld()) {
      return;
    } else if (explosions.get() && event.getPacket() instanceof SExplosionPacket) {
      Vec3d multiplier = getMultiplier();
      Vec3d motion = getPacketMotion(event.getPacket());
      setPacketMotion(event.getPacket(), VectorUtils.multiplyBy(motion, multiplier));
    } else if (velocity.get() && event.getPacket() instanceof SEntityVelocityPacket) {
      if (((SEntityVelocityPacket) event.getPacket()).getEntityID() == getLocalPlayer().getEntityId()) {
        Vec3d multiplier = getMultiplier();
        if (multiplier.lengthSquared() > 0.D) {
          setPacketMotion(event.getPacket(),
              VectorUtils.multiplyBy(getPacketMotion(event.getPacket()), multiplier));
        } else {
          event.setCanceled(true);
        }
      }
    } else if (fishhook.get() && event.getPacket() instanceof SEntityStatusPacket) {
      // CREDITS TO 0x22
      // fuck you popbob for making me need this
      SEntityStatusPacket packet = event.getPacket();
      if (packet.getOpCode() == 31) {
        Entity offender = packet.getEntity(getWorld()); // TODO: this is not thread safe
        if (offender instanceof FishingBobberEntity) {
          FishingBobberEntity hook = (FishingBobberEntity) offender;
          if (getLocalPlayer().equals(hook.caughtEntity)) {
            event.setCanceled(true);
          }
        }
      }
    }
  }
  
  /**
   * Stops velocity from water
   */
  @SubscribeEvent
  public void onWaterMovementEvent(WaterMovementEvent event) {
    if (water.get() && getLocalPlayer() != null && getLocalPlayer().equals(event.getEntity())) {
      addEntityVelocity(
          event.getEntity(),
          VectorUtils.multiplyBy(event.getMoveDir().normalize().scale(0.014D), getMultiplier()));
      event.setCanceled(true);
    }
  }
  
  /**
   * Stops velocity from collision
   */
  @SubscribeEvent
  public void onApplyCollisionMotion(ApplyCollisionMotionEvent event) {
    if (push.get() && getLocalPlayer() != null && getLocalPlayer().equals(event.getEntity())) {
      addEntityVelocity(
          event.getEntity(),
          VectorUtils.multiplyBy(
              new Vec3d(event.getMotionX(), event.getMotionY(), event.getMotionZ()),
              getMultiplier()));
      event.setCanceled(true);
    }
  }
  
  @SubscribeEvent
  public void onPushOutOfBlocks(PushOutOfBlocksEvent event) {
    if (blocks.get()) {
      event.setCanceled(true);
    }
  }
  
  @SubscribeEvent
  public void onBlockSlip(EntityBlockSlipApplyEvent event) {
    if (slipping.get()
        && getLocalPlayer() != null
        && getLocalPlayer().equals(event.getEntityLiving())) {
      event.setSlipperiness(0.6f);
    }
  }
}
