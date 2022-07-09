package com.templars_server.database.store;

import com.templars_server.database.model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;

public class AccountStore {

    private static final Logger LOG = LoggerFactory.getLogger(AccountStore.class);

    private final DataSource dataSource;

    public AccountStore(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Account getByIp(String ip) {
        return fetchAccount(
                "SELECT a.* FROM account a JOIN account_ip ai ON a.id = ai.account WHERE ai.ip = ? ORDER BY last_accessed DESC",
                ip
        );
    }

    public Account getByJaguid(String jaguid) {
        return fetchAccount(
                "SELECT a.* FROM account a JOIN account_jaguid ag ON a.id = ag.account WHERE ag.jaguid = ?",
                jaguid
        );
    }

    private Account fetchAccount(String sql, String param) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, param);
                ResultSet result = stmt.executeQuery();
                if (result.next()) {
                    return new Account(result.getString(1), result.getInt(2));
                }
            }
        } catch (SQLException e) {
            LOG.error("Fetching account failed " + sql + ", " + param, e);
        }

        return null;
    }

    public Account create() {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "INSERT INTO account (balance) VALUES (0) RETURNING id";
            try (Statement stmt = connection.createStatement()) {
                ResultSet result = stmt.executeQuery(sql);
                if (result.next()) {
                    return new Account(result.getString(1), 0);
                }
            }
        } catch (SQLException e) {
            LOG.error("Creating account failed", e);
        }

        return null;
    }

    public void save(Account account) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "UPDATE account SET balance=? WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, account.getBalance());
                stmt.setString(2, account.getId());
                stmt.executeQuery();
                account.setSave(false);
            }
        } catch (SQLException e) {
            LOG.error("Saving account failed " + account, e);
        }
    }

    public void registerIp(Account account, String ip) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "INSERT IGNORE INTO account_ip (ip, account) VALUES (?, ?) ON DUPLICATE KEY UPDATE last_accessed=current_timestamp()";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, ip);
                stmt.setString(2, account.getId());
                stmt.executeQuery();
            }
        } catch (SQLException e) {
            LOG.error("Registering ip failed " + account + ", " + ip, e);
        }
    }

    public void registerJaguid(Account account, String jaguid) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "INSERT IGNORE INTO account_jaguid (jaguid, account) VALUES (?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, jaguid);
                stmt.setString(2, account.getId());
                stmt.executeQuery();
            }
        } catch (SQLException e) {
            LOG.error("Registering jaguid failed " + account + ", " + jaguid, e);
        }
    }

    public void registerAlias(Account account, String alias, String stripped_alias) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "INSERT INTO account_alias (alias, stripped_alias, account) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE last_seen=current_timestamp()";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, alias);
                stmt.setString(2, stripped_alias);
                stmt.setString(3, account.getId());
                stmt.executeQuery();
            }
        } catch (SQLException e) {
            LOG.error("Registering alias failed " + account + ", " + alias + ", " + stripped_alias, e);
        }
    }

}
