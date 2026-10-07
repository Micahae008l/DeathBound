package com.deathbound.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.RisingParticle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.SingleQuadParticle.Layer;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class SoulParticle extends RisingParticle {
   private final SpriteSet sprites;
   private final float startSize;

   SoulParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprites, float size, int life) {
      super(level, x, y, z, xd, yd, zd, sprites.first());
      this.sprites = sprites;
      this.quadSize *= size;
      this.startSize = this.quadSize;
      this.lifetime = life;
      this.hasPhysics = false;
      this.setSpriteFromAge(sprites);
   }

   @Override
   public void tick() {
      super.tick();
      this.setSpriteFromAge(this.sprites);
      float t = (float)this.age / this.lifetime;
      float alpha = t < 0.15F ? t / 0.15F : 1.0F - Mth.clamp((t - 0.55F) / 0.45F, 0.0F, 1.0F);
      double d = Minecraft.getInstance().gameRenderer.mainCamera().position().distanceTo(new Vec3(this.x, this.y, this.z));
      this.setAlpha(alpha * (float)Mth.clamp((d - 0.4) / 1.6, 0.0, 1.0));
      this.quadSize = this.startSize * (1.0F - t * 0.35F);
   }

   @Override
   public int getLightCoords(float a) {
      return 15728880;
   }

   @Override
   protected Layer getLayer() {
      return Layer.TRANSLUCENT;
   }

   public record FlameProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
      public Particle createParticle(
         SimpleParticleType o, ClientLevel level, double x, double y, double z, double xd, double yd, double zd, RandomSource random
      ) {
         return new SoulParticle(level, x, y, z, xd, yd, zd, this.sprites, 1.1F + random.nextFloat() * 0.6F, 14 + random.nextInt(12));
      }
   }

   public record MoteProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
      public Particle createParticle(
         SimpleParticleType o, ClientLevel level, double x, double y, double z, double xd, double yd, double zd, RandomSource random
      ) {
         return new SoulParticle(level, x, y, z, xd, yd, zd, this.sprites, 0.55F + random.nextFloat() * 0.4F, 30 + random.nextInt(30));
      }
   }

   public static class Sweep extends SingleQuadParticle {
      private final SpriteSet sprites;

      Sweep(ClientLevel level, double x, double y, double z, double size, SpriteSet sprites) {
         super(level, x, y, z, 0.0, 0.0, 0.0, sprites.first());
         this.sprites = sprites;
         this.lifetime = 7;
         this.quadSize = (float)(size <= 0.0 ? 1.4 : size);
         this.setSpriteFromAge(sprites);
      }

      @Override
      public int getLightCoords(float a) {
         return 15728880;
      }

      @Override
      public void tick() {
         this.xo = this.x;
         this.yo = this.y;
         this.zo = this.z;
         if (this.age++ >= this.lifetime) {
            this.remove();
         } else {
            this.setSpriteFromAge(this.sprites);
         }
      }

      @Override
      protected Layer getLayer() {
         return Layer.TRANSLUCENT;
      }
   }

   public record SweepProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
      public Particle createParticle(
         SimpleParticleType o, ClientLevel level, double x, double y, double z, double xd, double yd, double zd, RandomSource random
      ) {
         return new SoulParticle.Sweep(level, x, y, z, xd, this.sprites);
      }
   }
}
