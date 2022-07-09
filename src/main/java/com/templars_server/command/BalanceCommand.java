package com.templars_server.command;

import com.templars_server.model.Context;
import com.templars_server.model.Display;
import com.templars_server.model.Player;
import com.templars_server.util.command.Command;
import com.templars_server.util.command.InvalidArgumentException;
import com.templars_server.util.rcon.RconClient;

import java.util.ArrayList;
import java.util.List;

public class BalanceCommand extends Command<Context> {

    public BalanceCommand() {
        super(
                "balance",
                false,
                "!balance <target>"
        );
    }

    @Override
    protected void onExecute(int slot, Context context) throws InvalidArgumentException {
        List<Player> targets = new ArrayList<>();
        targets.add(context.getPlayers().get(slot));
        try {
            targets = context.findPlayersByArg(getArg(0));
        } catch (InvalidArgumentException ignored) {
        }

        RconClient rcon = context.getRcon();
        if (targets.size() == 0) {
            rcon.print(slot, Display.BALANCE + "Target player not found");
            return;
        }

        if (targets.size() > 1 && targets.size() < 4) {
            rcon.print(slot, Display.BALANCE + "Search too broad, try using player slot directly:");
            for (Player target : targets) {
                rcon.print(slot, Display.BALANCE + target.getSlot() + " - " + target.getAlias());
            }
            return;
        }

        if (targets.size() > 1) {
            rcon.print(slot, Display.BALANCE + "Search too broad, try being more specific");
            return;
        }

        Player target = targets.get(0);
        if (target == null || target.getAccount() == null) {
            rcon.print(slot, Display.BALANCE + "Player account hasn't loaded yet, try again next round");
            return;
        }

        if (target.getSlot() == slot) {
            rcon.print(slot, Display.BALANCE + "Your wallet contains " + Display.renderBalance(target.getAccount().getBalance()));
        } else {
            rcon.print(slot, Display.BALANCE + target.getAlias() + "^7's wallet contains " + Display.renderBalance(target.getAccount().getBalance()));
        }
    }

}
