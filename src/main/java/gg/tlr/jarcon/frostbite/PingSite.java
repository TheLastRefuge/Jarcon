package gg.tlr.jarcon.frostbite;

import javax.annotation.Nullable;
import java.net.URI;

public enum PingSite {

    AMSTERDAM("ams", "qos-prod-bio-dub-common-common.gos.ea.com"),
    GUARULHOS("gru", "qos-prod-m3d-brz-common-common.gos.ea.com"),
    LOS_ANGELES("lax", "qos-prod-bio-sjc-common-common.gos.ea.com"),
    NARITA("nrt", "qos-prod-m3d-nrt-common-common.gos.ea.com"),
    SYDNEY("syd", "qos-prod-bio-syd-common-common.gos.ea.com"),
    WASHINGTON("iad", "qos-prod-bio-iad-common-common.gos.ea.com");

    private final String id;
    private final URI uri;

    PingSite(String id, String uri) {
        this.id = id;
        this.uri = URI.create(uri);
    }

    @Nullable
    public static PingSite getById(String id) {
        for (PingSite value : values()) if (value.getId().equalsIgnoreCase(id)) return value;
        return null;
    }

    public String getId() {
        return id;
    }

    public URI getUri() {
        return uri;
    }
}
