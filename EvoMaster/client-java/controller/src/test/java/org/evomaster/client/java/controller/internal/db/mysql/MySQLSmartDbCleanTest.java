package org.evomaster.client.java.controller.internal.db.mysql;

import org.evomaster.client.java.controller.internal.SutController;
import org.evomaster.client.java.controller.internal.db.SmartDbCleanTest;

import java.sql.Connection;

public class MySQLSmartDbCleanTest extends DatabaseMySQLTestInit implements SmartDbCleanTest {

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public SutController getSutController() {
        return new DatabaseFakeMySQLSutController(connection);
    }
}
