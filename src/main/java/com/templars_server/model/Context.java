package com.templars_server.model;

import com.templars_server.database.Database;
import com.templars_server.util.rcon.RconClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Context {

    private final Database database;
    private final RconClient rcon;
    private final Map<Integer, Player> players;

    public Context(Database database, RconClient rcon) {
        this.database = database;
        this.rcon = rcon;
        this.players = new HashMap<>();
    }

    public Database getDatabase() {
        return database;
    }

    public RconClient getRcon() {
        return rcon;
    }

    public Map<Integer, Player> getPlayers() {
        return players;
    }

    public List<Player> findPlayersByArg(String arg) {
        try {
            List<Player> targets = new ArrayList<>();
            targets.add(players.get(Integer.parseInt(arg)));
            return targets;
        } catch (NumberFormatException ignored) {
            return players.values().stream()
                    .filter(player -> player.getAliasStripped().toLowerCase().contains(arg.toLowerCase()))
                    .collect(Collectors.toList());
        }
    }

}
