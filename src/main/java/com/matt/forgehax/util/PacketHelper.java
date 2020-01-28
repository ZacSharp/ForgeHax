package com.matt.forgehax.util;

import static com.matt.forgehax.Helper.getNetworkManager;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import net.minecraft.network.IPacket;

/**
 * Created on 8/4/2017 by fr1kin
 */
public class PacketHelper {
  
  private static final LoadingCache<IPacket, Boolean> CACHE =
      CacheBuilder.newBuilder()
          .expireAfterWrite(15L, TimeUnit.SECONDS)
          .build(new CacheLoader<IPacket, Boolean>() {
            @Override
            public Boolean load(IPacket key) throws Exception {
              return false;
            }
          });
  
  public static void ignore(IPacket packet) {
    CACHE.put(packet, true);
  }
  
  public static void ignoreAndSend(IPacket packet) {
    ignore(packet);
    getNetworkManager().sendPacket(packet);
  }
  
  public static boolean isIgnored(IPacket packet) {
    try {
      return CACHE.get(packet);
    } catch (ExecutionException e) {
      return false;
    }
  }
  
  public static void remove(IPacket packet) {
    CACHE.invalidate(packet);
  }
}
