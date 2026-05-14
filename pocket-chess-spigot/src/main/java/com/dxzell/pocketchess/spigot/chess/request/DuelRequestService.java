package com.dxzell.pocketchess.spigot.chess.request;

import com.dxzell.pocketchess.api.game.GameCreationResult;
import com.dxzell.pocketchess.api.game.GameCreationResultType;
import com.dxzell.pocketchess.api.game.TimeMode;
import com.dxzell.pocketchess.spigot.PocketChess;
import com.dxzell.pocketchess.spigot.chess.game.SpigotChessGameService;
import com.dxzell.pocketchess.spigot.chess.request.creation.DuelRequestCreationResult;
import com.dxzell.pocketchess.spigot.chess.request.creation.DuelRequestCreationResultType;
import com.dxzell.pocketchess.spigot.config.MessageConfig;
import com.dxzell.pocketchess.spigot.config.SettingsConfig;
import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Manages all sent out chess duel requests. */
public final class DuelRequestService {

  private final PocketChess plugin;
  private final SpigotChessGameService spigotChessGameService;
  private final MessageConfig messageConfig;
  private final List<DuelRequest> ongoingRequests = new ArrayList<>();
  private final long expireTimeMillis;
  private BukkitTask requestTask;

  @Inject
  public DuelRequestService(
      PocketChess plugin,
      SpigotChessGameService spigotChessGameService,
      MessageConfig messageConfig,
      SettingsConfig settingsConfig) {
    this.plugin = plugin;
    this.spigotChessGameService = spigotChessGameService;
    this.messageConfig = messageConfig;
    expireTimeMillis = settingsConfig.getDuelRequestExpiresInMillis();
  }

  /**
   * Tries sending out a duel request.
   *
   * @param senderId the id of the duel request sender
   * @param receiverId the id of the duel request receiver
   * @param senderName the name of the duel request sender
   * @param receiverName the name of the duel request receiver
   * @param timeMode the requested time mode to play
   */
  public void sendRequest(
      UUID senderId, UUID receiverId, String senderName, String receiverName, TimeMode timeMode) {
    expireOldRequests();
    if (ongoingRequests.isEmpty()) {
      stopRequestTask();
    }
    DuelRequestCreationResult creationResult = checkPlayerStatus(senderId, receiverId);
    if (creationResult.type() == DuelRequestCreationResultType.SUCCESS) {
      ongoingRequests.add(
          new DuelRequest(
              senderId,
              receiverId,
              senderName,
              receiverName,
              timeMode,
              System.currentTimeMillis()));
      startRequestTask();
      sendMessage(
          senderId,
          messageConfig.getSuccessfullySentDuelRequest(receiverName, timeMode.getDisplayName()));
      sendMessage(
          receiverId, messageConfig.getDuelRequestReceived(senderName, timeMode.getDisplayName()));
    } else {
      sendMessage(senderId, creationResult.message());
    }
  }

  /**
   * Tries to accept the duel request and to create a chess game.
   *
   * @param receiverId the id of the duel request receiver
   * @param senderId the id of the duel request sender
   */
  public void acceptRequest(UUID receiverId, UUID senderId) {
    DuelRequest duelRequest = getDuelRequest(senderId, receiverId);
    if (duelRequest != null) {
      GameCreationResult creationResult =
          spigotChessGameService.createGame(
              Bukkit.getPlayer(senderId), Bukkit.getPlayer(receiverId), duelRequest.timeMode());
      if (creationResult.type() == GameCreationResultType.SUCCESS) {
        ongoingRequests.remove(duelRequest);
      } else {
        String message =
            switch (creationResult.type()) {
              case SUCCESS -> "";
              case FIRST_PLAYER_IN_GAME -> messageConfig.getRequestSenderPlaying();
              case SECOND_PLAYER_IN_GAME -> messageConfig.getYouAlreadyInGame();
              case BOTH_PLAYERS_IN_GAME -> messageConfig.getRequestSenderReceiverPlaying();
            };
        sendMessage(receiverId, message);
      }
    } else {
      sendMessage(receiverId, messageConfig.getNoOngoingRequest());
    }
  }

  /**
   * Tries to decline the duel request.
   *
   * @param receiverId the id of the duel request receiver
   * @param senderId the id of the duel request sender
   */
  public void declineRequest(UUID receiverId, UUID senderId) {
    DuelRequest duelRequest = getDuelRequest(senderId, receiverId);
    if (duelRequest != null) {
      sendMessage(senderId, messageConfig.getOpponentDeclinedRequest(duelRequest.receiverName()));
      sendMessage(receiverId, messageConfig.getYouDeclinedRequest(duelRequest.senderName()));
      ongoingRequests.remove(duelRequest);
    } else {
      sendMessage(receiverId, messageConfig.getNoOngoingRequest());
    }
  }

  /**
   * Checks whether a duel request can be sent out, or not.
   *
   * @param senderId the id of the duel request sender
   * @param receiverId the id of the duel request receiver
   * @return a duel creation result
   */
  private DuelRequestCreationResult checkPlayerStatus(UUID senderId, UUID receiverId) {
    if (senderId.equals(receiverId)) {
      return new DuelRequestCreationResult(
          DuelRequestCreationResultType.SAME_PLAYER, messageConfig.getCannotDuelYourself());
    }

    if (alreadyRequested(senderId, receiverId)) {
      return new DuelRequestCreationResult(
          DuelRequestCreationResultType.ALREADY_REQUESTED_DUEL,
          messageConfig.getAlreadySentDuelRequest());
    }

    if (spigotChessGameService.isPlaying(senderId)
        && spigotChessGameService.isPlaying(receiverId)) {
      return new DuelRequestCreationResult(
          DuelRequestCreationResultType.BOTH_PLAYERS_IN_GAME, messageConfig.getBothAlreadyInGame());
    }

    if (spigotChessGameService.isPlaying(senderId)) {
      return new DuelRequestCreationResult(
          DuelRequestCreationResultType.FIRST_PLAYER_IN_GAME, messageConfig.getYouAlreadyInGame());
    }

    if (spigotChessGameService.isPlaying(receiverId)) {
      return new DuelRequestCreationResult(
          DuelRequestCreationResultType.SECOND_PLAYER_IN_GAME,
          messageConfig.getOpponentAlreadyInGame());
    }

    return new DuelRequestCreationResult(DuelRequestCreationResultType.SUCCESS, "");
  }

  /** Starts the duel request task. */
  private void startRequestTask() {
    if (requestTask == null) {
      requestTask =
          Bukkit.getScheduler()
              .runTaskTimer(
                  plugin,
                  () -> {
                    expireOldRequests();

                    if (ongoingRequests.isEmpty()) {
                      stopRequestTask();
                    }
                  },
                  20L,
                  20L);
    }
  }

  /** Stops the duel request task. */
  private void stopRequestTask() {
    if (requestTask == null) {
      return;
    }

    requestTask.cancel();
    requestTask = null;
  }

  /**
   * Removes expired duel requests from the list of ongoing duel requests and sends messages to both
   * players.
   */
  private void expireOldRequests() {
    long now = System.currentTimeMillis();

    List<DuelRequest> expiredRequests =
        ongoingRequests.stream().filter(request -> isExpired(request, now)).toList();

    ongoingRequests.removeAll(expiredRequests);

    for (DuelRequest request : expiredRequests) {
      sendMessage(
          request.senderId(), messageConfig.getDuelRequestToExpired(request.receiverName()));

      sendMessage(
          request.receiverId(), messageConfig.getDuelRequestFromExpired(request.senderName()));
    }
  }

  /**
   * @param duelRequest the duel request
   * @param now the current time in millis
   * @return whether the given duel request has expired, or not
   */
  private boolean isExpired(DuelRequest duelRequest, long now) {
    long expirationTimestamp = duelRequest.sentTimestamp() + expireTimeMillis;
    return now >= expirationTimestamp;
  }

  /**
   * @param senderId the id of the duel request sender
   * @param receiverId the id of the duel request receiver
   * @return whether the sender already has an ongoing duel request to the given receiver
   */
  private boolean alreadyRequested(UUID senderId, UUID receiverId) {
    for (DuelRequest request : ongoingRequests) {
      if (request.senderId().equals(senderId) && request.receiverId().equals(receiverId)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param senderId the id of the duel request sender
   * @param receiverId the id of the duel request receiver
   * @return the duel request of the given sender and receiver
   */
  private DuelRequest getDuelRequest(UUID senderId, UUID receiverId) {
    for (DuelRequest duelRequest : ongoingRequests) {
      if (duelRequest.senderId().equals(senderId) && duelRequest.receiverId().equals(receiverId)) {
        return duelRequest;
      }
    }
    return null;
  }

  /**
   * Sends the given message to the given player if online.
   *
   * @param playerId the id of the player
   * @param message the message to send
   */
  private void sendMessage(UUID playerId, String message) {
    Player player = Bukkit.getPlayer(playerId);
    if (player != null) {
      player.sendMessage(message);
    }
  }
}
