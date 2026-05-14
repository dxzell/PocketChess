package com.dxzell.pocketchess.spigot.chess.game;

import com.dxzell.pocketchess.spigot.chess.inventory.item.ChessMenuItemBuilder;
import com.dxzell.pocketchess.spigot.chess.inventory.item.ChessMenuItemType;
import com.dxzell.pocketchess.spigot.config.MessageConfig;
import com.dxzell.pocketchess.spigot.config.SettingsConfig;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/** Handles surrender requests. */
public final class SurrenderHandler {

  private final SpigotChessGame spigotChessGame;
  private final ChessInventoryManager chessInventoryManager;
  private final ChessMenuItemBuilder menuItemBuilder;
  private final SettingsConfig settingsConfig;
  private final MessageConfig messageConfig;
  private final SurrenderData whiteSurrenderData;
  private final SurrenderData blackSurrenderData;

  public SurrenderHandler(SpigotChessGame spigotChessGame) {
    this.spigotChessGame = spigotChessGame;
    chessInventoryManager = spigotChessGame.getInventoryManager();
    menuItemBuilder = spigotChessGame.getChessMenuItemBuilder();
    settingsConfig = spigotChessGame.getSettingsConfig();
    messageConfig = spigotChessGame.getMessageConfig();
    whiteSurrenderData = new SurrenderData(spigotChessGame.getChessGame().getWhitePlayerId());
    blackSurrenderData = new SurrenderData(spigotChessGame.getChessGame().getBlackPlayerId());
  }

  /**
   * Handles the surrender click by checking the current surrender state and validating which action
   * should be performed.
   *
   * @param playerId the id of the player who clicked the surrender item
   */
  public void handleSurrenderClick(UUID playerId) {
    SurrenderData surrenderData = getPlayersSurrenderData(playerId);

    if (surrenderData.isWaitingForConfirmation()) {
      spigotChessGame.endGame(spigotChessGame.getOtherPlayerId(playerId));
    } else {
      surrenderData.setSurrenderRequestTimestamp(System.currentTimeMillis());
      setConfirmationState(surrenderData);
    }
  }

  /**
   * Sets the players surrender data into a confirmation state. The player now has to click again to
   * surrender.
   *
   * @param playersSurrenderData the players surrender data
   */
  private void setConfirmationState(SurrenderData playersSurrenderData) {
    UUID playerId = playersSurrenderData.getPlayerId();
    playersSurrenderData.setWaitingForConfirmation(true);
    chessInventoryManager.highlightSurrenderItem(playerId, true);
    playersSurrenderData.addLittleTimeToRequest();
    setSurrenderItemMessage(messageConfig.getConfirmSurrender(), playerId);
  }

  /** Updates the surrender request timestamps for both players. */
  public void updateTimestamps() {
    whiteSurrenderData.updateSurrenderRequestTimestamp();
    blackSurrenderData.updateSurrenderRequestTimestamp();
  }

  /**
   * Displays the given message in the surrender item lore.
   *
   * @param message the message to display
   * @param playerId the id of the player to display the message to
   */
  private void setSurrenderItemMessage(String message, UUID playerId) {
    menuItemBuilder.setChessMenuItemMessage(
        message,
        ChessMenuItemType.SURRENDER,
        chessInventoryManager.getPlayersChessInventory(playerId));
  }

  /**
   * @param playerId the id of the player
   * @return the given players surrender data
   */
  private SurrenderData getPlayersSurrenderData(UUID playerId) {
    return playerId.equals(whiteSurrenderData.getPlayerId())
        ? whiteSurrenderData
        : blackSurrenderData;
  }

  /** Holds all needed data for the surrender validation. */
  @Getter
  @Setter
  private final class SurrenderData {

    private final UUID playerId;
    private Long surrenderRequestTimestamp;
    private boolean waitingForConfirmation = false;

    public SurrenderData(UUID playerId) {
      this.playerId = playerId;
    }

    /** Updates the surrender request timestamp and resets it if the timestamp has been reached. */
    public void updateSurrenderRequestTimestamp() {
      if (surrenderRequestTimestamp == null) {
        return;
      }

      long expiresInTimestamp =
          surrenderRequestTimestamp + settingsConfig.getSurrenderConfirmationExpiresInMillis();
      if (System.currentTimeMillis() >= expiresInTimestamp) {
        resetSurrenderRequestTimestamp();
      }
    }

    /** Resets the surrender request timestamp and removes the highlight & item message. */
    private void resetSurrenderRequestTimestamp() {
      waitingForConfirmation = false;
      surrenderRequestTimestamp = null;
      spigotChessGame.getInventoryManager().highlightSurrenderItem(playerId, false);
      setSurrenderItemMessage(" ", playerId);
    }

    /**
     * Adds a little hardcoded amount of time. When the player clicks the item the first time and
     * needs to click again to confirm this method will allow the player to get a little bit more
     * time so the item doesn't reset instantly.
     */
    private void addLittleTimeToRequest() {
      surrenderRequestTimestamp += 3000L;
    }
  }
}
