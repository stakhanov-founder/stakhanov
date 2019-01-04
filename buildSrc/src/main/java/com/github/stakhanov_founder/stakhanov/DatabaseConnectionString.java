package com.github.stakhanov_founder.stakhanov;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;

public class DatabaseConnectionString {

    public static String getDatabaseJdbcConnectionString() throws URISyntaxException, SQLException, ClassNotFoundException {
        URI databaseFullUri = new URI(System.getenv("DATABASE_URL"));

        return "jdbc:postgresql://" + databaseFullUri.getHost() + ':' + databaseFullUri.getPort() + databaseFullUri.getPath()
            + "?user=" + databaseFullUri.getUserInfo().split(":")[0]
            + "&password=" + databaseFullUri.getUserInfo().split(":")[1];
    }
}
