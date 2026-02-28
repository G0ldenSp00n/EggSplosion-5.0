package com.g0ldensp00n.eggsplosion.handlers.MapManager;

import java.util.concurrent.CompletableFuture;

import org.jspecify.annotations.NullMarked;

import com.g0ldensp00n.eggsplosion.EggSplosion;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;

@NullMarked
public class MapNameArgument implements CustomArgumentType.Converted<String, String> {

  private static final DynamicCommandExceptionType ERROR_INVALID_MAP_NAME = new DynamicCommandExceptionType(mapName -> {
    return MessageComponentSerializer.message().serialize(Component.text("Unknown map: " + mapName));
  });

  @Override
  public String convert(String nativeType) throws CommandSyntaxException {
    MapManager mapManager = EggSplosion.getInstance().getMapManager();
    if (mapManager.getMapByName(nativeType) == null) {
      throw ERROR_INVALID_MAP_NAME.create(nativeType);
    }
    return nativeType;
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
    MapManager mapManager = EggSplosion.getInstance().getMapManager();
    mapManager.getMaps().keySet()
        .stream()
        .filter(entry -> entry
            .toLowerCase()
            .startsWith(builder
                .getRemainingLowerCase()))
        .forEach(entry -> {
          builder.suggest(entry);
        });

    return builder.buildFuture();
  }

  @Override
  public ArgumentType<String> getNativeType() {
    return StringArgumentType.word();
  }
}
