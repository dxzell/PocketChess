package com.dxzell.pocketchess.spigot.chess.inventory;

import com.dxzell.pocketchess.api.move.Move;
import com.dxzell.pocketchess.api.piece.Piece;
import com.dxzell.pocketchess.api.piece.PieceColor;
import com.dxzell.pocketchess.api.piece.PieceType;
import com.dxzell.pocketchess.spigot.chess.game.SpigotChessGame;
import com.dxzell.pocketchess.spigot.chess.inventory.item.*;
import com.dxzell.pocketchess.spigot.chess.inventory.time.TimeUnit;
import com.dxzell.pocketchess.spigot.chess.inventory.time.TimeUnitTextureAmount;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

/** Handles updates in the chess inventory. */
public final class ChessInventoryUpdater {

  private final SpigotChessGame spigotChessGame;
  private final ChessInventory chessInventory;
  private final Inventory upperInv;
  private final ItemStack[] lowerInv;
  private final UUID playerId;
  private final TimeUnitItemBuilder timeUnitItemBuilder;
  private final ChessMenuItemBuilder chessMenuItemBuilder;
  private final PromotionItemBuilder promotionItemBuilder;
  private BukkitTask infoTask;

  public ChessInventoryUpdater(
      SpigotChessGame spigotChessGame, ChessInventory chessInventory, UUID playerId) {
    this.spigotChessGame = spigotChessGame;
    this.chessInventory = chessInventory;
    this.playerId = playerId;

    timeUnitItemBuilder = spigotChessGame.getTimeUnitItemBuilder();
    chessMenuItemBuilder = spigotChessGame.getChessMenuItemBuilder();
    promotionItemBuilder = spigotChessGame.getPromotionItemBuilder();
    upperInv = chessInventory.getUpperInv();
    lowerInv = chessInventory.getLowerInv();
  }

  /**
   * Updates the chess inventory by making the move on the board.
   *
   * @param move the played move
   */
  public void updateChessBoard(Move move) {
    PieceColor color = chessInventory.getColor();
    ChessInventorySlot fromInvSlot = ChessInventoryUtil.toInventorySlot(move.from(), color);
    ChessInventorySlot toInvSlot = ChessInventoryUtil.toInventorySlot(move.to(), color);
    ItemStack pieceItem =
        ChessInventoryUtil.getPieceItemFromSquare(chessInventory, move.from(), color);

    removePieceFromStarterPosition(fromInvSlot);

    setPiece(toInvSlot, pieceItem);

    resetHighlights(move);
  }

  /** Adds the promotion items into the players chess inventory. */
  public void addPromotionPieces() {
    PieceColor color = spigotChessGame.getColor(playerId);
    lowerInv[29] =
        promotionItemBuilder.createPromotionItem(
            new Piece(
                PieceType.QUEEN, color == PieceColor.WHITE ? PieceColor.WHITE : PieceColor.BLACK));
    lowerInv[30] =
        promotionItemBuilder.createPromotionItem(
            new Piece(
                PieceType.KNIGHT, color == PieceColor.WHITE ? PieceColor.WHITE : PieceColor.BLACK));
    lowerInv[31] =
        promotionItemBuilder.createPromotionItem(
            new Piece(
                PieceType.BISHOP, color == PieceColor.WHITE ? PieceColor.WHITE : PieceColor.BLACK));
    lowerInv[32] =
        promotionItemBuilder.createPromotionItem(
            new Piece(
                PieceType.ROOK, color == PieceColor.WHITE ? PieceColor.WHITE : PieceColor.BLACK));
    updateLowerPart();
  }

  /** Removes the promotion items from the players chess inventory. */
  public void removePromotionPieces() {
    lowerInv[29] = null;
    lowerInv[30] = null;
    lowerInv[31] = null;
    lowerInv[32] = null;
    updateLowerPart();
  }

  /**
   * Removes the piece from starter position.
   *
   * @param fromInvSlot the start inventory slot
   */
  private void removePieceFromStarterPosition(ChessInventorySlot fromInvSlot) {
    if (fromInvSlot.part() == ChessInventoryPart.UPPER) {
      upperInv.setItem(fromInvSlot.slot(), null);
    } else {
      lowerInv[fromInvSlot.slot()] = null;
      updateLowerPart();
    }
  }

  /**
   * Places the piece on the new square slot.
   *
   * @param toInvSlot the destination inventory slot
   * @param pieceItem the piece item to move to that destination slot
   */
  private void setPiece(ChessInventorySlot toInvSlot, ItemStack pieceItem) {
    if (toInvSlot.part() == ChessInventoryPart.UPPER) {
      upperInv.setItem(toInvSlot.slot(), pieceItem);
    } else {
      lowerInv[toInvSlot.slot()] = pieceItem;
      updateLowerPart();
    }
  }

  /**
   * Resets the current highlights in the chess inventory.
   *
   * @param move the played move.
   */
  private void resetHighlights(Move move) {
    chessInventory.getChessInventoryHighlighter().unhighlightAvailableMoves();
    chessInventory.getChessInventoryHighlighter().unhighlightSelectedPiece(move.from());
  }

  /** Updates the players inventory with the lower part. */
  public void updateLowerPart() {
    Player player = Bukkit.getPlayer(playerId);
    if (player != null && player.getOpenInventory().getTopInventory().equals(upperInv)) {
      player.getInventory().setStorageContents(lowerInv);
    }
  }

  /**
   * Updates the time unit items in the chess inventory. At the right moments it even changes the
   * time unit textures for the amount of 1 and 0 to be displayed.
   *
   * @param milliSeconds the remaining amount of time in milliseconds
   */
  public void updateTime(long milliSeconds) {
    long remainingMilliseconds = updateHours(milliSeconds);

    remainingMilliseconds = updateMinutes(remainingMilliseconds);

    updateSeconds(remainingMilliseconds);
  }

  /**
   * Updates the amount of the hour item.
   *
   * @param milliSeconds the current time in milliseconds of the player to move
   * @return the remaining time with the calculated hours deducted
   */
  private long updateHours(long milliSeconds) {
    long rest = milliSeconds;
    int hours = (int) (rest / 3600000);
    rest %= 3600000;
    ItemStack hoursItem =
        timeUnitItemBuilder.getTimeUnitItem(
            chessInventory,
            TimeUnit.HOUR,
            spigotChessGame.getChessGame().getCurrentTurn().equals(playerId));
    setRightTexture(TimeUnit.HOUR, hoursItem, hours);

    return rest;
  }

  /**
   * Updates the amount of the minute item.
   *
   * @param remainingMilliseconds the remaining time with the calculated hours deducted
   * @return the remaining time with the calculated minutes deducted
   */
  private long updateMinutes(long remainingMilliseconds) {
    int minutes = (int) (remainingMilliseconds / 60000);
    remainingMilliseconds %= 60000;
    ItemStack minutesItem =
        timeUnitItemBuilder.getTimeUnitItem(
            chessInventory,
            TimeUnit.MINUTE,
            spigotChessGame.getChessGame().getCurrentTurn().equals(playerId));
    setRightTexture(TimeUnit.MINUTE, minutesItem, minutes);

    return remainingMilliseconds;
  }

  /**
   * Updates the amount of the second item.
   *
   * @param remainingMilliseconds the remaining time with the calculated minutes deducted
   */
  private void updateSeconds(long remainingMilliseconds) {
    ItemStack secondsItem =
        timeUnitItemBuilder.getTimeUnitItem(
            chessInventory,
            TimeUnit.SECOND,
            spigotChessGame.getChessGame().getCurrentTurn().equals(playerId));
    int seconds = (int) (remainingMilliseconds / 1000);
    setRightTexture(TimeUnit.SECOND, secondsItem, seconds);
  }

  /**
   * Updates the texture and amount of the given time unit item. This allows the time unit items to
   * have a visible amount of 0, 1 and above while without this feature the 0 and 1 amount would not
   * be visible.
   *
   * @param timeUnit the time unit of the item
   * @param timeUnitItem the time unit item
   * @param amount the amount of time for the specified time unit
   */
  private void setRightTexture(TimeUnit timeUnit, ItemStack timeUnitItem, int amount) {
    if (amount == 0) {
      timeUnitItemBuilder.changeTextureAmount(
          timeUnitItem, TimeUnitTextureAmount.ZERO, timeUnit, true);
      timeUnitItem.setAmount(1);
    } else if (amount == 1) {
      timeUnitItemBuilder.changeTextureAmount(
          timeUnitItem, TimeUnitTextureAmount.ONE, timeUnit, true);
      timeUnitItem.setAmount(1);
    } else {
      timeUnitItemBuilder.changeTextureAmount(
          timeUnitItem, TimeUnitTextureAmount.STANDARD, timeUnit, true);
      timeUnitItem.setAmount(amount);
    }
  }

  /**
   * Updates the info item with a new message.
   *
   * @param message the message to display
   */
  public void updateInfo(String message) {
    runSync(
        () -> {
          chessMenuItemBuilder.setChessMenuItemMessage(
              message, ChessMenuItemType.INFO, chessInventory);

          startInfoAnimation();
        });
  }

  /** Runs a small animation for the info item. */
  private void startInfoAnimation() {
    if (infoTask != null && !infoTask.isCancelled()) {
      return;
    }

    infoTask =
        new BukkitRunnable() {
          private int step;

          @Override
          public void run() {
            boolean highlight = step % 2 == 0;

            chessMenuItemBuilder.highlightChessMenuItem(
                ChessInventoryUpdater.this, chessInventory, ChessMenuItemType.INFO, highlight);

            step++;

            if (step >= 8) {
              chessMenuItemBuilder.highlightChessMenuItem(
                  ChessInventoryUpdater.this, chessInventory, ChessMenuItemType.INFO, false);

              infoTask = null;
              cancel();
            }
          }
        }.runTaskTimer(spigotChessGame.getPlugin(), 0L, 4L);
  }

  /** Resets the info message in the given chess inventory. */
  public void resetInfo() {
    runSync(
        () ->
            chessMenuItemBuilder.setChessMenuItemMessage(
                " ", ChessMenuItemType.INFO, chessInventory));
  }

  /** Cancels the info task if running. */
  public void cancelInfoTask() {
    runSync(
        () -> {
          if (infoTask != null && !infoTask.isCancelled()) {
            infoTask.cancel();
            infoTask = null;
          }

          chessMenuItemBuilder.highlightChessMenuItem(
              this, chessInventory, ChessMenuItemType.INFO, false);
        });
  }

  /**
   * Runs the task on the main server thread.
   *
   * @param task the task to run.
   */
  private void runSync(Runnable task) {
    if (Bukkit.isPrimaryThread()) {
      task.run();
      return;
    }

    Bukkit.getScheduler().runTask(spigotChessGame.getPlugin(), task);
  }
}
