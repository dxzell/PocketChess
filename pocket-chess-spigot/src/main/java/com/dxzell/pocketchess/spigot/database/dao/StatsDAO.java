package com.dxzell.pocketchess.spigot.database.dao;

import com.dxzell.pocketchess.spigot.database.Database;
import com.google.inject.Inject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

/** Manages database access for player statistics. */
public final class StatsDAO extends DAO {

  @Inject
  public StatsDAO(Database database, Logger logger) {
    super(database, logger);
  }

  @Override
  public synchronized void createTable() {
    Connection connection = getConnectionOrNull();

    if (connection == null) {
      return;
    }

    String sql =
        "CREATE TABLE IF NOT EXISTS stats ("
            + "player_uuid VARCHAR(36), "
            + "wins INT, "
            + "draws INT, "
            + "losses INT, "
            + "PRIMARY KEY(player_uuid)"
            + ");";

    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.executeUpdate();
    } catch (SQLException ex) {
      logger.severe("Error creating stats table: " + ex.getMessage());
      ex.printStackTrace();
    }
  }

  /**
   * Creates a stats entry for the player if not already existing.
   *
   * @param playerId the id of the player
   */
  public synchronized void createPlayerEntry(UUID playerId) {
    Connection connection = getConnectionOrNull();

    if (connection == null) {
      return;
    }

    if (containsPlayer(playerId)) {
      return;
    }

    String sql = "INSERT INTO stats (player_uuid, wins, draws, losses) VALUES (?, 0, 0, 0);";

    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, playerId.toString());
      statement.executeUpdate();
    } catch (SQLException ex) {
      logger.severe("Error creating stats entry: " + ex.getMessage());
      ex.printStackTrace();
    }
  }

  /**
   * Adds a win to the players stats.
   *
   * @param playerId the id of the player
   */
  public synchronized void incrementWins(UUID playerId) {
    incrementStat(playerId, "wins");
  }

  /**
   * Adds a draw to the players stats.
   *
   * @param playerId the id of the player
   */
  public synchronized void incrementDraws(UUID playerId) {
    incrementStat(playerId, "draws");
  }

  /**
   * Adds a loss to the players stats.
   *
   * @param playerId the id of the player
   */
  public synchronized void incrementLosses(UUID playerId) {
    incrementStat(playerId, "losses");
  }

  /**
   * Increments the given stat of the player.
   *
   * @param playerId the id of the player
   * @param column the stat
   */
  private void incrementStat(UUID playerId, String column) {
    Connection connection = getConnectionOrNull();

    if (connection == null) {
      return;
    }

    createPlayerEntry(playerId);

    String sql = "UPDATE stats SET " + column + " = " + column + " + 1 WHERE player_uuid = ?;";

    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, playerId.toString());
      statement.executeUpdate();
    } catch (SQLException ex) {
      logger.severe("Error incrementing stat '" + column + "': " + ex.getMessage());
      ex.printStackTrace();
    }
  }

  /**
   * @param playerId the id of the player
   * @return whether an entry for the given player stats exists in the database
   */
  public synchronized boolean containsPlayer(UUID playerId) {
    Connection connection = getConnectionOrNull();

    if (connection == null) {
      return false;
    }

    String sql = "SELECT 1 FROM stats WHERE player_uuid = ?;";

    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, playerId.toString());

      try (ResultSet resultSet = statement.executeQuery()) {
        return resultSet.next();
      }
    } catch (SQLException ex) {
      logger.severe("Error checking stats entry: " + ex.getMessage());
      ex.printStackTrace();
    }

    return false;
  }

  private Connection getConnectionOrNull() {
    Connection connection = database.getConnection();

    if (connection == null) {
      return null;
    }

    try {
      if (connection.isClosed()) {
        return null;
      }
    } catch (SQLException ignored) {
      return null;
    }

    return connection;
  }
}
