package com.templars_server.database;

import com.templars_server.Application;
import com.templars_server.database.store.AccountStore;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;

public class Database {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    private final DataSource dataSource;
    private final AccountStore accountStore;

    public Database(String address, String database, String user, String password) {
        HikariConfig config = new HikariConfig();
        String uri = String.format("jdbc:mariadb://%s/%s", address, database);
        config.setDriverClassName("org.mariadb.jdbc.Driver");
        config.setJdbcUrl(uri);
        config.setUsername(user);
        config.setPassword(password);
        this.dataSource = new HikariDataSource(config);
        this.accountStore = new AccountStore(dataSource);
    }


    public void setup() throws SQLException {
        try (Connection db = dataSource.getConnection()) {
            try (Statement stmt = db.createStatement()) {
                stmt.execute(
                        "CREATE TABLE IF NOT EXISTS `account` (" +
                        "  `id` varchar(36) NOT NULL DEFAULT uuid()," +
                        "  `balance` int(11) NOT NULL DEFAULT 0," +
                        "  `created_at` timestamp NOT NULL DEFAULT current_timestamp()," +
                        "  PRIMARY KEY (`id`)" +
                        ");"
                );

                stmt.execute(
                        "CREATE TABLE IF NOT EXISTS `account_alias` (" +
                        "  `alias` varchar(100) NOT NULL," +
                        "  `stripped_alias` varchar(100) NOT NULL," +
                        "  `account` varchar(36) NOT NULL," +
                        "  `first_seen` timestamp NOT NULL DEFAULT current_timestamp()," +
                        "  `last_seen` timestamp NOT NULL DEFAULT current_timestamp()," +
                        "  PRIMARY KEY (`alias`,`account`)," +
                        "  KEY `FK_account_alias_account_id` (`account`)," +
                        "  CONSTRAINT `FK_account_alias_account_id` FOREIGN KEY (`account`) REFERENCES `account` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION" +
                        ");"
                );
                stmt.execute(
                        "CREATE TABLE IF NOT EXISTS `account_ip` (" +
                        "  `ip` varchar(15) NOT NULL," +
                        "  `account` varchar(36) NOT NULL," +
                        "  `last_accessed` timestamp NOT NULL DEFAULT current_timestamp()," +
                        "  PRIMARY KEY (`ip`,`account`)," +
                        "  KEY `FK_account_ip_account_id` (`account`)," +
                        "  CONSTRAINT `FK_account_ip_account_id` FOREIGN KEY (`account`) REFERENCES `account` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION\n" +
                        ");"
                );
                stmt.execute(
                        "CREATE TABLE IF NOT EXISTS `account_jaguid` (" +
                                "  `jaguid` varchar(128) NOT NULL," +
                                "  `account` varchar(36) NOT NULL," +
                                "  PRIMARY KEY (`jaguid`)," +
                                "  KEY `FK_account_jaguid_account_id` (`account`)," +
                                "  CONSTRAINT `FK_account_jaguid_account_id` FOREIGN KEY (`account`) REFERENCES `account` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION" +
                                ");"
                );
            }
        }
    }

    public AccountStore getAccountStore() {
        return accountStore;
    }

}
