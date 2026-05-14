package com.dxzell.pocketchess.spigot.database;

import com.dxzell.pocketchess.spigot.database.dao.StatsDAO;
import com.google.inject.Inject;

/**
 * Manages the creation of database tables.
 */
public final class DatabaseSchemaCreator {

    private final StatsDAO statsDAO;

    @Inject
    public DatabaseSchemaCreator(StatsDAO statsDAO) {
        this.statsDAO = statsDAO;
    }

    /**
     * Creates database tables if they don't exist.
     */
    public void createTables() {
        statsDAO.createTable();
    }
}
