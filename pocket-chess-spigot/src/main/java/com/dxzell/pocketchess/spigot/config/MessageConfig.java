package com.dxzell.pocketchess.spigot.config;

import com.dxzell.pocketchess.spigot.PocketChess;
import com.google.inject.Inject;

/** Provides access to all plugin sent messages. */
public final class MessageConfig extends Config {

  @Inject
  public MessageConfig(PocketChess plugin) {
    super(plugin, "messages.yml");
  }

  public String getNotYourTurn() {
    return getColoredString(getInfoPath() + "not-your-turn");
  }

  public String getNoPieceSelected() {
    return getColoredString(getInfoPath() + "no-piece-selected");
  }

  public String getInvalidSquare() {
    return getColoredString(getInfoPath() + "invalid-square");
  }

  public String getPickPromotion() {
    return getColoredString(getInfoPath() + "pick-promotion-piece");
  }

  public String getInfoItemName() {
    return getColoredString(getChessGameMessagePath() + "chess-menu-item-names.info-item");
  }

  public String getSurrenderItemName() {
    return getColoredString(getChessGameMessagePath() + "chess-menu-item-names.surrender-item");
  }

  public String getDrawItemName() {
    return getColoredString(getChessGameMessagePath() + "chess-menu-item-names.draw-item");
  }

  public String getDrawOfferCooldown(String cooldownString) {
    return getColoredString(getDrawPath() + "cooldown").replace("[cooldown]", cooldownString);
  }

  public String getDrawOfferReceived() {
    return getColoredString(getDrawPath() + "offer-received");
  }

  public String getDrawOfferSent() {
    return getColoredString(getDrawPath() + "offer-sent");
  }

  public String getConfirmDraw() {
    return getColoredString(getDrawPath() + "confirm-offer");
  }

  public String getConfirmSurrender() {
    return getColoredString(getSurrenderPath() + "confirm");
  }

  public String getOpponentMoved() {
    return getColoredString(getChessChatPath() + "opponent-moved");
  }

  public String getYouAlreadyInGame() {
    return getColoredString(getChessCommandMessagePath() + "you-already-in-game");
  }

  public String getOpponentAlreadyInGame() {
    return getColoredString(getChessCommandMessagePath() + "opponent-already-in-game");
  }

  public String getBothAlreadyInGame() {
    return getColoredString(getChessCommandMessagePath() + "both-in-game");
  }

  public String getCannotDuelYourself() {
    return getColoredString(getChessCommandMessagePath() + "cannot-duel-yourself");
  }

  public String getNotPlaying() {
    return getColoredString(getChessCommandMessagePath() + "not-playing");
  }

  public String getOpponentNotOnline() {
    return getColoredString(getChessCommandMessagePath() + "opponent-not-online");
  }

  public String getInvalidTimeMode() {
    return getColoredString(getChessCommandMessagePath() + "invalid-time-mode");
  }

  public String getAlreadySentDuelRequest() {
    return getColoredString(getChessCommandMessagePath() + "already-requested-duel");
  }

  public String getDuelRequestToExpired(String opponentName) {
    return getColoredString(getChessCommandMessagePath() + "duel-request-to-expired")
        .replace("[opponent]", opponentName);
  }

  public String getDuelRequestFromExpired(String opponentName) {
    return getColoredString(getChessCommandMessagePath() + "duel-request-from-expired")
        .replace("[opponent]", opponentName);
  }

  public String getDuelRequestReceived(String opponentName, String timeMode) {
    return getColoredString(getChessCommandMessagePath() + "duel-request-received")
        .replace("[opponent]", opponentName)
        .replace("[mode]", timeMode);
  }

  public String getSuccessfullySentDuelRequest(String opponentName, String timeMode) {
    return getColoredString(getChessCommandMessagePath() + "successfully-sent-duel-request")
        .replace("[opponent]", opponentName)
        .replace("[mode]", timeMode);
  }

  public String getDuelRequestSenderOffline() {
    return getColoredString(getChessCommandMessagePath() + "request-sender-offline");
  }

  public String getRequestSenderPlaying() {
    return getColoredString(getChessCommandMessagePath() + "request-sender-playing");
  }

  public String getRequestSenderReceiverPlaying() {
    return getColoredString(getChessCommandMessagePath() + "request-sender-receiver-playing");
  }

  public String getNoOngoingRequest() {
    return getColoredString(getChessCommandMessagePath() + "no-ongoing-request");
  }

  public String getGameStarted() {
    return getColoredString(getChessChatPath() + "started");
  }

  public String getYouDeclinedRequest(String senderName) {
    return getColoredString(getChessCommandMessagePath() + "you-declined-request")
        .replace("[opponent]", senderName);
  }

  public String getOpponentDeclinedRequest(String receiverName) {
    return getColoredString(getChessCommandMessagePath() + "opponent-declined-request")
        .replace("[opponent]", receiverName);
  }

  public String getInvalidArgsCmd() {
    return getColoredString(getChessCommandMessagePath() + "invalid-args-command");
  }

  private String getChessCommandMessagePath() {
    return "messages.chess-command.";
  }

  private String getChessGameMessagePath() {
    return "messages.chess-game.";
  }

  private String getInfoPath() {
    return getChessGameMessagePath() + "info.";
  }

  private String getDrawPath() {
    return getChessGameMessagePath() + "draw.";
  }

  private String getSurrenderPath() {
    return getChessGameMessagePath() + "surrender.";
  }

  private String getChessChatPath() {
    return getChessGameMessagePath() + "chat.";
  }
}
