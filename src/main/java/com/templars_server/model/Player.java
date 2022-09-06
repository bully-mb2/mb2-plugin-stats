package com.templars_server.model;

import com.templars_server.database.model.Account;
import generated.Team;

public class Player {

    private final int slot;
    private final String ip;
    private Account account;
    private String alias;
    private String aliasStripped;
    private String jaguid;
    private Team team;

    public Player(int slot, String ip, Account account, String alias) {
        this.slot = slot;
        this.ip = ip;
        this.account = account;
        this.team = Team.SPECTATOR;
        this.setAlias(alias);
    }

    public int getSlot() {
        return slot;
    }

    public String getIp() {
        return ip;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getAlias() {
        return alias;
    }

    public String getAliasStripped() {
        return aliasStripped;
    }

    public void setAlias(String alias) {
        this.alias = alias;
        if (alias == null) {
            this.aliasStripped = null;
        } else {
            this.aliasStripped = alias.replaceAll("\\^[0-9]", "");
        }
    }

    public String getJaguid() {
        return jaguid;
    }

    public void setJaguid(String jaguid) {
        this.jaguid = jaguid;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    @Override
    public String toString() {
        return "Player{" +
                "slot=" + slot +
                ", ip='" + ip + '\'' +
                ", account=" + account +
                ", aliasStripped='" + aliasStripped + '\'' +
                ", jaguid='" + jaguid + '\'' +
                '}';
    }
}
