package gg.tlr.jarcon;

import gg.tlr.jarcon.bf3.BF3Client;
import org.junit.jupiter.api.Assertions;
import org.slf4j.impl.SimpleLogger;

import java.lang.reflect.Field;
import java.net.Inet4Address;
import java.net.InetSocketAddress;

public final class TestEnv {

    public static final String  IP          = null;
    public static final Integer PORT        = null;
    public static final String  PASSWORD    = null;
    public static final String  TEST_PLAYER = null;

    static {
        Assertions.assertNotNull(IP);
        Assertions.assertNotNull(PORT);
        Assertions.assertNotNull(PASSWORD);
        Assertions.assertNotNull(TEST_PLAYER);
    }

    public static BF3Client newClient() throws Exception {
        final InetSocketAddress address = new InetSocketAddress(Inet4Address.getByName(IP), PORT);
        BF3Client client = new BF3Client(address, PASSWORD);
        client.getSettings().eventsEnabled(true);

        if(client.getLogger() instanceof SimpleLogger logger) {
            final Field field = logger.getClass().getDeclaredField("currentLogLevel");
            field.setAccessible(true);
            field.set(logger, 0);
        }

        client.connect().get();

        return client;
    }

}
