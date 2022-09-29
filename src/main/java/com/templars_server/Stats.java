package com.templars_server;


import com.templars_server.command.BalanceCommand;
import com.templars_server.database.Database;
import com.templars_server.database.model.Account;
import com.templars_server.database.store.AccountStore;
import com.templars_server.model.Context;
import com.templars_server.model.Display;
import com.templars_server.model.Player;
import com.templars_server.util.command.Command;
import com.templars_server.util.command.InvalidArgumentException;
import com.templars_server.util.rcon.RconClient;
import generated.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.stream.Collectors;

public class Stats {

    private static final Logger LOG = LoggerFactory.getLogger(Stats.class);
    private static final DecimalFormat KILL_DECIMAL_FORMAT = new DecimalFormat(
            "#.##",
            DecimalFormatSymbols.getInstance(Locale.GERMAN)
    );
    private static final int KILL_REWARD = 6;
    private static final int TEAMKILL_PENALTY = -12;
    private static final String PREFIX = "^6MVP >> ^7";


    private final Context context;
    private final RconClient rcon;
    private final Database database;
    private final List<Command<Context>> commands;

    public Stats(Context context) {
        this.context = context;
        this.rcon = context.getRcon();
        this.database = context.getDatabase();
        this.commands = new ArrayList<>();
    }

    public void setup() {
        LOG.info("Setting up commands:");
        commands.clear();
        commands.add(new BalanceCommand());
        for (Command<Context> command : commands) {
            LOG.info("    - " + command.getClass().getSimpleName());
        }
    }

    void onClientSpawnedEvent(ClientSpawnedEvent event) {
        Player player = createPlayerIfNotExists(event.getSlot(), event.getIp(), event.getName());
        AccountStore accountStore = database.getAccountStore();
        String jaguid = event.getJaGuid();
        if (player.getJaguid() == null && jaguid != null) {
            // Player didn't have a jaguid before, so we should migrate their account
            Account account = accountStore.getByJaguid(jaguid);
            if (account == null) {
                accountStore.registerJaguid(player.getAccount(), jaguid);
            } else if (!player.getAccount().getId().equals(account.getId())) {
                accountStore.registerIp(account, player.getIp());
                player.setAccount(findDuplicateAccount(account));
                rcon.print(player.getSlot(), Display.MERGE + "Account restored from previous IP");
            }

            player.setJaguid(jaguid);
        }

        player.setTeam(event.getTeam());
        player.setMbClass(event.getForcePowers().getMbClass());
    }

    void onClientConnectEvent(ClientConnectEvent event) {
        Player player = createPlayerIfNotExists(event.getSlot(), event.getIp(), event.getName());
        player.setAlias(event.getName());
        database.getAccountStore().registerAlias(player.getAccount(), player.getAlias(), player.getAliasStripped());
    }

    void onClientDisconnectEvent(ClientDisconnectEvent event) {
        Player player = context.getPlayers().remove(event.getSlot());
        if (player == null || player.getAccount() == null) {
            return;
        }

        LOG.info("Saving " + player.getAliasStripped() + " account " + player.getAccount());
        database.getAccountStore().save(player.getAccount());
    }


    void onShutdownGameEvent(ShutdownGameEvent event) {
        AccountStore accountStore = database.getAccountStore();
        List<Player> savedPlayers = context.getPlayers().values().stream()
                .filter(player -> player.getAccount() != null)
                .filter(player -> player.getAccount().shouldSave())
                .collect(Collectors.toUnmodifiableList());

        LOG.info("Saving " + savedPlayers.size() + " out of " + context.getPlayers().size() + " player accounts");
        savedPlayers.forEach(player -> accountStore.save(player.getAccount()));
    }


    public void onKillEvent(KillEvent killEvent) {
        Player killer = context.getPlayers().get(killEvent.getKiller());
        Player victim = context.getPlayers().get(killEvent.getVictim());
        if (killer == null || killer.getAccount() == null || victim == null) {
            return;
        }

        if (killer.getTeam() == Team.SPECTATOR || victim.getTeam() == Team.SPECTATOR) {
            return;
        }

        if (killer.getSlot() == victim.getSlot()) {
            return;
        }

        if (killer.getTeam().equals(victim.getTeam())) {
            killer.getAccount().addBalance(TEAMKILL_PENALTY);
            LOG.info(killer.getAliasStripped() + " team killed " + victim.getAliasStripped());
            rcon.printCon(killer.getSlot(),
                    Display.BALANCE
                            + "You lost "
                            + Display.renderBalance(TEAMKILL_PENALTY)
                            + " for teamkilling "
                            + victim.getAlias() + "^7"
                            + " and now have "
                            + Display.renderBalance(killer.getAccount().getBalance())
            );
            return;
        }

        killer.getAccount().addBalance(KILL_REWARD);
        LOG.info(killer.getAliasStripped() + " killed " + victim.getAliasStripped());
        rcon.printCon(killer.getSlot(),
                Display.BALANCE
                        + "You got "
                        + Display.renderBalance(KILL_REWARD)
                        + " for killing "
                        + victim.getAlias() + "^7"
                        + " and now have "
                        + Display.renderBalance(killer.getAccount().getBalance())
        );

        killer.addRoundKill(calculateKillWeight(victim.getMbClass()));
    }

    public void onClientUserinfoChangedEvent(ClientUserinfoChangedEvent event) {
        if (event.getName() == null) {
            return;
        }

        Player player = context.getPlayers().get(event.getSlot());
        if (player == null) {
            return;
        }

        if (player.getAlias() != null && player.getAlias().equals(event.getName())) {
            return;
        }

        player.setAlias(event.getName());
        Account account = player.getAccount();
        if (account == null) {
            return;
        }

        database.getAccountStore().registerAlias(account, player.getAlias(), player.getAliasStripped());
    }

    public void onSayEvent(SayEvent event) {
        String message = event.getMessage();
        for (Command<Context> command : commands) {
            try {
                if (command.execute(event.getSlot(), message, context)) {
                    LOG.info("Executed user command " + command.getClass().getSimpleName() + " for player " + event.getSlot() + " " + event.getName());
                    break;
                }
            } catch (InvalidArgumentException e) {
                rcon.print(event.getSlot(), Display.PREFIX + command.getUsage());
            } catch (Exception e) {
                LOG.error("Uncaught exception during command execution", e);
            }
        }
    }

    public void onSendingGameReportEvent(SendingGameReportEvent event) {
        double mostKills = 0d;
        Player mostKillsPlayer = null;
        for (Player player : context.getPlayers().values()) {
            if (player.getRoundKills() > mostKills) {
                mostKills = player.getRoundKills();
                mostKillsPlayer = player;
            }

            player.setRoundKills(0d);
        }


        if (mostKillsPlayer != null && mostKills > 0.01d) {
            String killString = "kill";
            if (mostKills > 1d) {
                killString += "s";
            }

            LOG.info("Round ended " + mostKillsPlayer.getAliasStripped() + " is MVP with " + mostKills + " " + killString);
            rcon.printAll(String.format(
                    PREFIX + "%s^7 was this rounds ^6MVP^7 with %s %s",
                    mostKillsPlayer.getAlias(),
                    KILL_DECIMAL_FORMAT.format(mostKills),
                    killString)
            );
            return;
        }

        LOG.info("Round ended with no MVP = " + mostKills);
    }

    private double calculateKillWeight(MBClass mbClass) {
        if (mbClass == null) {
            return 1d;
        }

        switch (mbClass) {
            case COMMANDER:
            case ELITE_TROOPER:
            case CLONE_TROOPER:
                return 0.5d;
            case IMPERIAL_SOLDIER:
            case REBEL_SOLDIER:
                return 0.33333333d;
            default:
                return 1d;
        }
    }

    private Player createPlayerIfNotExists(int slot, String ip, String name) {
        Map<Integer, Player> players = context.getPlayers();
        Player player = players.get(slot);
        if (player != null && !player.getIp().equals(ip)) {
            LOG.warn("Did we recover from a server crash? Deleting mismatched player " + player);
            players.remove(slot);
            player = null;
        }

        if (player == null) {
            player = new Player(
                    slot,
                    ip,
                    null,
                    name
            );
            players.put(slot, player);
        }

        if (player.getAccount() == null) {
            AccountStore accountStore = database.getAccountStore();
            Account account = accountStore.getByIp(ip);
            if (account == null) {
                account = accountStore.create();
                accountStore.registerIp(account, ip);
            }

            player.setAccount(findDuplicateAccount(account));
        }

        return player;
    }

    private Account findDuplicateAccount(Account account) {
        Optional<Player> player = context.getPlayers().values().stream()
                .filter(p -> p.getAccount() != null)
                .filter(p -> p.getAccount().getId().equals(account.getId()))
                .findFirst();
        return player.isEmpty() ? account : player.get().getAccount();
    }

}
