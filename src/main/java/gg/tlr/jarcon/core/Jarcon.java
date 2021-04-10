package gg.tlr.jarcon.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Jarcon {

    private static final Logger LOGGER = LoggerFactory.getLogger(Jarcon.class);

    private Jarcon() { }

    public static Logger getLogger() {
        return LOGGER;
    }
}
