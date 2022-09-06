package com.templars_server;

import com.templars_server.database.Database;
import com.templars_server.model.Context;
import com.templars_server.model.Player;
import com.templars_server.util.rcon.RconClient;
import generated.ClientUserinfoChangedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;


class StatsTest {

    private static Context context;
    private static Stats stats;

    @BeforeEach
    void setup() {
        context = new Context(
                mock(Database.class),
                mock(RconClient.class)
        );
        stats = new Stats(context);
    }

    @Test
    void testOnClientUserinfoChangedEvent_SparseData_NoExceptions() {
        context.getPlayers().put(21, new Player(0, "", null, null));
        ClientUserinfoChangedEvent testMessage = new ClientUserinfoChangedEvent();
        testMessage.setSlot(21);
        testMessage.setColor1(0);
        testMessage.setColor2(0);
        testMessage.setModelVariant(0);
        testMessage.setSaberVariant(0);

        stats.onClientUserinfoChangedEvent(testMessage);
    }

}
