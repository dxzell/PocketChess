package com.dxzell.pocketchess.spigot.database.dao;

import com.dxzell.pocketchess.spigot.database.Database;

import java.util.logging.Logger;

/**
 * Provides shared functionality for DAO classes.
 */
public abstract class DAO {

  protected final Database database;
  protected final Logger logger;

  public DAO(Database database, Logger logger) {
    this.database = database;
    this.logger = logger;
  }

  /** Creates database table if not existing. */
  public abstract void createTable();
}
