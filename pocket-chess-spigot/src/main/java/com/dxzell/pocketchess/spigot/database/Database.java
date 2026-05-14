package com.dxzell.pocketchess.spigot.database;

import com.dxzell.pocketchess.spigot.config.DatabaseConfig;
import com.google.inject.Inject;
import lombok.Getter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

/** Manages the database connection. */
public final class Database {

  private final DatabaseConfig databaseConfig;
  private final Logger pluginLogger;
  @Getter private Connection connection;

  @Inject
  public Database(DatabaseConfig databaseConfig, Logger pluginLogger) {
    this.databaseConfig = databaseConfig;
    this.pluginLogger = pluginLogger;
  }

  /** Connects to the database. */
  public void connect() {
    try {
      if (connection != null && !connection.isClosed()) {
        return;
      }

      String jdbcUrl =
          "jdbc:mysql://"
              + databaseConfig.getHost()
              + ":"
              + databaseConfig.getPort()
              + "/"
              + databaseConfig.getDatabase()
              + "?useSSL=false&autoReconnect=true";

      pluginLogger.info(
          "Connecting to MySQL database: "
              + jdbcUrl
              + " with user "
              + databaseConfig.getUsername());

      connection =
          DriverManager.getConnection(
              jdbcUrl, databaseConfig.getUsername(), databaseConfig.getPassword());

      pluginLogger.info("Successfully connected to MySQL database");
    } catch (SQLException ex) {
      pluginLogger.severe("Failed to connect to database: " + ex.getMessage());
    }
  }

  /** Closes the database connection. */
  public void close() {
    try {
      if (connection != null && !connection.isClosed()) {
        connection.close();
        connection = null;

        pluginLogger.info("Database connection closed");
      }
    } catch (SQLException ex) {
      pluginLogger.severe("Failed to close database connection: " + ex.getMessage());
      ex.printStackTrace();
    }
  }
}
