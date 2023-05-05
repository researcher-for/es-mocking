package org.evomaster.client.java.controller.internal.db.h2;

import org.evomaster.client.java.controller.internal.SutController;
import org.evomaster.client.java.controller.internal.db.SmartDbCleanTest;

import java.sql.Connection;

public class H2SmartDbCleanTest extends DatabaseH2TestInit implements SmartDbCleanTest {

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public SutController getSutController() {
        return new DatabaseFakeH2SutController(connection);
    }
}