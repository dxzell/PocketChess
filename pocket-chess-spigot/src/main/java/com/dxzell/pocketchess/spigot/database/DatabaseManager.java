package com.dxzell.pocketchess.spigot.database;

import com.google.inject.Inject;

/** Manages database initialization and shutdown. */
public final class DatabaseManager {

    private final Database database;
    private final DatabaseSchemaCreator databaseSchemaCreator;

    @Inject
    public DatabaseManager(Database database, DatabaseSchemaCreator databaseSchemaCreator) {
        this.database = database;
        this.databaseSchemaCreator = databaseSchemaCreator;
    }

    /**
     * Connects to the database and creates tables that don't already exist.
     */
    public void initialize() {
        database.connect();
        databaseSchemaCreator.createTables();
    }

    /**
     * Closes the connection to the database.
     */
    public void shutdown() {
        database.close();
    }
}
