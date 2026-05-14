package com.dxzell.pocketchess.spigot.chess.game;

import com.dxzell.pocketchess.api.game.ChessGame;
import com.dxzell.pocketchess.spigot.PocketChess;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

/** Manages the time of the chess game. */
public final class ChessGameClock {

  private final PocketChess plugin;
  private final SpigotChessGame spigotChessGame;
  private final ChessGame chessGame;
  private BukkitTask gameRunnable;
  private long lastRunnableUpdateMillis;

  public ChessGameClock(PocketChess plugin, SpigotChessGame spigotChessGame) {
    this.plugin = plugin;
    this.spigotChessGame = spigotChessGame;

    chessGame = spigotChessGame.getChessGame();
  }

  /** Creates and starts the runnable that manages the time. */
  public void startTimeRunnable() {
    if (gameRunnable != null) {
      return;
    }

    lastRunnableUpdateMillis = System.currentTimeMillis();

    gameRunnable =
        Bukkit.getScheduler()
            .runTaskTimer(
                plugin,
                () -> {
                  UUID currentTurn = chessGame.getCurrentTurn();

                  long now = System.currentTimeMillis();
                  long timePassed = now - lastRunnableUpdateMillis;
                  lastRunnableUpdateMillis = now;

                  long newTime = chessGame.getTimeLeftMillis(currentTurn) - timePassed;

                  if (newTime <= 0) {
                    spigotChessGame.endGame(
                        spigotChessGame.getOtherPlayerId(
                            spigotChessGame.getChessGame().getCurrentTurn()));
                  }
                  chessGame.updateRemainingTime(currentTurn, newTime);
                  spigotChessGame.getInventoryManager().updateInventoryTime(newTime);

                  spigotChessGame.getDrawHandler().updateTimestamps();
                  spigotChessGame.getSurrenderHandler().updateTimestamps();
                },
                5L,
                5L);
  }

  /** Stops the runnable if running. */
  public void stopRunnable() {
    if (gameRunnable != null) {
      gameRunnable.cancel();
      gameRunnable = null;
    }
  }
}
