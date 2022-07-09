package com.templars_server;

import com.templars_server.database.Database;
import com.templars_server.model.Context;
import com.templars_server.util.mqtt.MBMqttClient;
import com.templars_server.util.rcon.RconClient;
import com.templars_server.util.settings.Settings;
import generated.*;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;


public class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws IOException, MqttException, SQLException {
        LOG.info("======== Starting mb2-plugin-stats ========");
        LOG.info("Loading settings");
        Settings settings = new Settings();
        settings.load("application.properties");

        LOG.info("Setting up database connection");
        Database database = new Database(
                settings.get("database.address"),
                settings.get("database.name"),
                settings.get("database.user"),
                settings.get("database.password")
        );
        LOG.info("Creating tables if they don't already exist");
        database.setup();

        LOG.info("Setting up rcon client");
        RconClient rcon = new RconClient();
        rcon.connect(
                settings.getAddress("rcon.host"),
                settings.get("rcon.password")
        );

        LOG.info("Setting up Stats");
        Context context = new Context(database, rcon);
        Stats stats = new Stats(context);
        stats.setup();

        LOG.info("Registering event callbacks");
        MBMqttClient client = new MBMqttClient();
        client.putEventListener(stats::onClientSpawnedEvent, ClientSpawnedEvent.class);
        client.putEventListener(stats::onClientConnectEvent, ClientConnectEvent.class);
        client.putEventListener(stats::onClientDisconnectEvent, ClientDisconnectEvent.class);
        client.putEventListener(stats::onShutdownGameEvent, ShutdownGameEvent.class);
        client.putEventListener(stats::onKillEvent, KillEvent.class);
        client.putEventListener(stats::onSayEvent, SayEvent.class);

        LOG.info("Connecting to MQTT broker");
        client.connect(
                "tcp://localhost:" + settings.getInt("mqtt.port"),
                settings.get("mqtt.topic")
        );
    }

}
