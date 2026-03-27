package com.sample.PersonApp.config;

import java.sql.DriverManager;
import java.sql.Connection;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

public class JooqConfig {

    private static final String URL = "jdbc:mysql://localhost:3306/personapp";
    private static final String USER = "root";
    private static final String PASS = "root";

    public static DSLContext getDSLContext() {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASS);
            return DSL.using(conn, SQLDialect.MYSQL);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
