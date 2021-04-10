package gg.tlr.jarcon.bf3;

import gg.tlr.jarcon.core.Action;
import gg.tlr.jarcon.core.Version;
import gg.tlr.jarcon.frostbite.FrostbiteClient;
import gg.tlr.jarcon.frostbite.FrostbiteVersion;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.net.SocketAddress;
import java.util.List;

public class BF3Client extends FrostbiteClient {

    private final BF3EventHandler eventHandler = new BF3EventHandler();

    public BF3Client(SocketAddress address, String password) {
        super(address, password);
        getMetaHandler().registerListener(eventHandler);
    }

    @Override
    public BF3EventHandler getEventHandler() {
        return eventHandler;
    }

    @Override
    protected boolean compatibleWith(@Nonnull Version version) {
        return switch (FrostbiteVersion.getById(version.version())) {
            case BF3 -> true;
            default -> false;
        };
    }

    @CheckReturnValue
    public Action<BF3ServerInfo> serverInfo() {
        return action.new Packet(false, "serverInfo").map(packet -> BF3ServerInfo.parse(packet.data()));
    }

    @CheckReturnValue
    public Action<List<BF3MaplistEntry>> listMaps(int offset) {
        return listMapsTemplate(offset, BF3MaplistEntry::read);
    }

//    "Currently broken!" - R38 spec page 20
//    @CheckReturnValue
//    public Action<List<GameMap>> availableMaps() {
//
//    }
}
