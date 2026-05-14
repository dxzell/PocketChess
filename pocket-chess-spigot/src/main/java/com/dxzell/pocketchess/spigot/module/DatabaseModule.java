package com.dxzell.pocketchess.spigot.module;

import com.dxzell.pocketchess.spigot.database.Database;
import com.dxzell.pocketchess.spigot.database.DatabaseManager;
import com.dxzell.pocketchess.spigot.database.DatabaseSchemaCreator;
import com.dxzell.pocketchess.spigot.database.dao.StatsDAO;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class DatabaseModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Database.class).in(Singleton.class);
    bind(DatabaseManager.class).in(Singleton.class);
    bind(DatabaseSchemaCreator.class).in(Singleton.class);
    bind(StatsDAO.class).in(Singleton.class);
  }
}
