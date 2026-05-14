package com.dxzell.pocketchess.spigot.module;

import com.dxzell.pocketchess.spigot.PocketChess;
import com.dxzell.pocketchess.spigot.chess.game.SoundPlayer;
import com.dxzell.pocketchess.spigot.chess.game.SpigotChessGameService;
import com.dxzell.pocketchess.spigot.chess.inventory.item.ChessMenuItemBuilder;
import com.dxzell.pocketchess.spigot.chess.inventory.item.PieceItemBuilder;
import com.dxzell.pocketchess.spigot.chess.inventory.item.TimeUnitItemBuilder;
import com.dxzell.pocketchess.spigot.chess.request.DuelRequestService;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class SpigotModule extends AbstractModule {

  private final PocketChess plugin;

  public SpigotModule(PocketChess plugin) {
    this.plugin = plugin;
  }

  @Override
  protected void configure() {
    bind(SpigotChessGameService.class).in(Singleton.class);
    bind(PieceItemBuilder.class).in(Singleton.class);
    bind(TimeUnitItemBuilder.class).in(Singleton.class);
    bind(ChessMenuItemBuilder.class).in(Singleton.class);
    bind(SoundPlayer.class).in(Singleton.class);
    bind(DuelRequestService.class).in(Singleton.class);
  }

  @Provides
  @Singleton
  PocketChess getPlugin() {
    return plugin;
  }
}
