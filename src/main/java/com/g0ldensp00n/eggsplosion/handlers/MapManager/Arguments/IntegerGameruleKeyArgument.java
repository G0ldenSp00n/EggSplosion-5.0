package com.g0ldensp00n.eggsplosion.handlers.MapManager.Arguments;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import org.jspecify.annotations.NullMarked;

import com.g0ldensp00n.eggsplosion.handlers.MapManager.GameMap.IntegerGameRules;
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
public class IntegerGameruleKeyArgument implements CustomArgumentType.Converted<IntegerGameRules, String> {

  public static final DynamicCommandExceptionType ERROR_INVALID_GAMERULE_KEY = new DynamicCommandExceptionType(
      gameruleKey -> {
        return MessageComponentSerializer.message()
            .serialize(Component.text("Unknown gamerule: " + gameruleKey));
      });

  @Override
  public IntegerGameRules convert(String nativeType) throws CommandSyntaxException {
    return IntegerGameRules.fromString(nativeType);
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder) {
    Arrays.stream(IntegerGameRules.values()).map(entry -> entry.asString())
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
