package com.g0ldensp00n.eggsplosion.handlers.MapManager;

import java.util.concurrent.CompletableFuture;

import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NullMarked;

import com.g0ldensp00n.eggsplosion.EggSplosion;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;

@NullMarked
public class CapturePointNameArgument implements CustomArgumentType.Converted<String, NamespacedKey> {

  private static final DynamicCommandExceptionType ERROR_INVALID_MAP_NAME = new DynamicCommandExceptionType(
      capturePointName -> {
        return MessageComponentSerializer.message()
            .serialize(Component.text("Unknown capture point: " + capturePointName));
      });

  @Override
  public String convert(NamespacedKey nativeType) throws CommandSyntaxException {
    MapManager mapManager = EggSplosion.getInstance().getMapManager();

    GameMap map = mapManager.getMapByName(nativeType.getNamespace());
    if (map == null || map.getCapturePoint(nativeType.getKey()) == null) {
      throw ERROR_INVALID_MAP_NAME.create(nativeType.getKey());
    }
    return nativeType.getKey();
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder) {
    MapManager mapManager = EggSplosion.getInstance().getMapManager();
    final String mapName = ctx.getArgument("map_name", String.class);

    GameMap map = mapManager.getMapByName(mapName);
    map.getAllCapturePointName()
        .stream()
        .filter(entry -> new NamespacedKey(mapName.toLowerCase(), entry.toLowerCase()).asString()
            .toLowerCase()
            .contains(builder
                .getRemainingLowerCase()))
        .forEach(entry -> {
          builder.suggest(new NamespacedKey(mapName.toLowerCase(), entry.toLowerCase()).asString());
        });

    return builder.buildFuture();
  }

  @Override
  public ArgumentType<NamespacedKey> getNativeType() {
    return ArgumentTypes.namespacedKey();
  }
}
