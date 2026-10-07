package com.deathbound.registry;

import com.deathbound.DeathBound;
import com.deathbound.charm.RelicMenu;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;

public final class ModMenus {
   public static final ExtendedMenuType<RelicMenu, Integer> RELIC = Registry.register(
      BuiltInRegistries.MENU, DeathBound.id("relic"), new ExtendedMenuType<>(RelicMenu::new, ByteBufCodecs.VAR_INT)
   );

   public static void init() {
   }

   private ModMenus() {
   }
}
