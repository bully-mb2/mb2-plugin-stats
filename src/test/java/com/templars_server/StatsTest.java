package com.templars_server;

import com.templars_server.database.Database;
import com.templars_server.mb2_log_reader.schema.*;
import com.templars_server.model.Context;
import com.templars_server.model.Player;
import com.templars_server.util.rcon.RconClient;
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
        int testSlot = 21;
        context.getPlayers().put(testSlot, new Player(testSlot, "", null, null));
        ClientUserinfoChangedEvent testMessage = new ClientUserinfoChangedEvent();
        testMessage.setSlot(testSlot);
        testMessage.setColor1(0);
        testMessage.setColor2(0);
        testMessage.setModelVariant(0);
        testMessage.setSaberVariant(0);

        stats.onClientUserinfoChangedEvent(testMessage);
    }

}
