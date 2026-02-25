package com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects;

import java.util.Random;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.destroystokyo.paper.ParticleBuilder;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.WeaponEffect;

public class GeometricVisualEffect extends WeaponEffect {
  private Shape shape;
  private int particleCount = 500;
  private ParticleBuilder particleBuilder;
  private boolean isPlayerSource;

  public GeometricVisualEffect(Shape shape, int particleCount, ParticleBuilder particleBuilder,
      boolean isPlayerSource) {
    this.shape = shape;
    this.particleCount = particleCount;
    this.particleBuilder = particleBuilder;
    this.isPlayerSource = isPlayerSource;
  }

  @Override
  public void activateEffect(Location location, Player shooter) {
    if (!isPlayerSource) {
      shape.drawShape(particleBuilder, particleCount, location, shooter);
    } else {
      shape.drawShape(particleBuilder, particleCount, shooter.getEyeLocation(), shooter);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static abstract class Offset {
    float speed = 0.f;

    public abstract ParticleBuilder applyOffsetAndSpeed(Vector particleOffset,
        Player shooter, ParticleBuilder builder);

    public Offset withSpeed(float speed) {
      this.speed = speed;
      return this;
    }

    public static class FromPoint extends Offset {
      boolean awayFromPoint;

      public FromPoint(boolean awayFromPoint) {
        this.awayFromPoint = awayFromPoint;
      }

      public FromPoint() {
        this(true);
      }

      @Override
      public ParticleBuilder applyOffsetAndSpeed(Vector particleOffset, Player shooter,
          ParticleBuilder builder) {
        if (awayFromPoint) {
          return builder.offset(particleOffset.getX(), particleOffset.getY(), particleOffset.getZ()).extra(speed);
        } else {
          return builder.offset(particleOffset.getX(), particleOffset.getY(), particleOffset.getZ()).extra(-speed);
        }
      }
    }

    public static class InDirection extends Offset {
      Vector offset;

      public InDirection(Vector offset) {
        this.offset = offset;
      }

      @Override
      public ParticleBuilder applyOffsetAndSpeed(Vector particleOffset, Player shooter,
          ParticleBuilder builder) {
        return builder.offset(offset.getX(), offset.getY(), offset.getZ()).extra(speed);
      }
    }

    public static class InDirectionRelative extends Offset {
      Vector offset;

      public InDirectionRelative(Vector relativeOffset) {
        this.offset = relativeOffset.clone();
      }

      @Override
      public ParticleBuilder applyOffsetAndSpeed(Vector particleOffset, Player shooter,
          ParticleBuilder builder) {
        Vector forward = shooter.getLocation().getDirection();

        Vector right;
        if (Math.abs(forward.getY()) > 0.99) {
          double yaw = Math.toRadians(shooter.getLocation().getYaw());
          right = new Vector(-Math.cos(yaw), 0, -Math.sin(yaw));
        } else {
          Vector globalUp = new Vector(0, 1, 0);
          right = forward.clone().crossProduct(globalUp).normalize();
        }

        Vector up = right.clone().crossProduct(forward).normalize();

        Vector worldOffset = new Vector(0, 0, 0)
            .add(right.multiply(offset.getX()))
            .add(up.multiply(offset.getY()))
            .add(forward.multiply(offset.getZ()));

        return builder.offset(worldOffset.getX(), worldOffset.getY(), worldOffset.getZ()).extra(speed);
      }
    }

  }

  public static abstract class Shape {
    Offset offset;

    public Shape(Offset offset) {
      this.offset = offset;
    }

    abstract void drawShape(ParticleBuilder builder, int particleCount, Location location, Player shooter);

    public static class Sphere extends Shape {
      float radius = 2.f;

      public Sphere(float radius, Offset offset) {
        super(offset);
        this.radius = radius;
      }

      @Override
      void drawShape(ParticleBuilder builder, int particleCount, Location location, Player shooter) {
        for (float i = 0; i < particleCount; i += 1) {
          float k = i + .5f;
          float phi = (float) Math.acos(1f - 2f * k / particleCount);
          float theta = (float) (Math.PI * (1 + Math.sqrt(5)) * k);

          float x = (float) (Math.cos(theta) * Math.sin(phi));
          float y = (float) (Math.sin(theta) * Math.sin(phi));
          float z = (float) (Math.cos(phi));

          offset.applyOffsetAndSpeed(new Vector(x, y, z).multiply(radius), shooter, builder.count(0))
              .count(0)
              .location(location.clone().add(new Vector(x, y, z).multiply(radius)))
              .receivers(50, true).spawn();
        }
      }

      public static Builder builder() {
        return new Builder();
      }

      public static class Builder {
        float radius = 2.f;
        Offset offset;

        public Builder() {
        }

        public Builder withRadius(float radius) {
          this.radius = radius;
          return this;
        }

        public Builder withOffset(Offset offset) {
          this.offset = offset;
          return this;
        }

        public Sphere build() {
          return new Sphere(radius, offset);
        }
      }
    }

    public static class Ring extends Shape {
      float radius = 2.f;

      public Ring(float radius, Offset offset) {
        super(offset);
        this.radius = radius;
      }

      @Override
      void drawShape(ParticleBuilder builder, int particleCount, Location location, Player shooter) {
        Vector shooterDirection = shooter.getEyeLocation().getDirection().normalize();
        Vector right = new Vector(-shooterDirection.getY(), shooterDirection.getX(), 0).normalize().multiply(radius);
        for (int i = 0; i < particleCount; i++) {
          Vector particleOffset = right.clone().rotateAroundAxis(shooterDirection,
              ((2.f * Math.PI) / particleCount) * i);
          offset.applyOffsetAndSpeed(particleOffset, shooter, builder.count(0))
              .location(location.clone().add(particleOffset.multiply(radius)))
              .receivers(50, true).spawn();

        }
      }

      public static Builder builder() {
        return new Builder();
      }

      public static class Builder {
        float radius = 2.f;
        Offset offset;

        public Builder() {
        }

        public Builder withRadius(float radius) {
          this.radius = radius;
          return this;
        }

        public Builder withOffset(Offset offset) {
          this.offset = offset;
          return this;
        }

        public Ring build() {
          return new Ring(radius, offset);
        }
      }
    }
  }

  public static class Builder {
    Shape shape;
    int particleCount = 500;
    ParticleBuilder particleBuilder = Particle.FLAME.builder();
    boolean isPlayerSource = false;

    public Builder() {
    }

    public Builder withShape(Shape shape) {
      this.shape = shape;
      return this;
    }

    public Builder withParticleCount(int particleCount) {
      this.particleCount = particleCount;
      return this;
    }

    public Builder withParticleBuilder(ParticleBuilder builder) {
      this.particleBuilder = builder;
      return this;
    }

    public Builder withPlayerAsSource() {
      this.isPlayerSource = true;
      return this;
    }

    public GeometricVisualEffect build() {
      return new GeometricVisualEffect(shape, particleCount, particleBuilder, isPlayerSource);
    }
  }
}
