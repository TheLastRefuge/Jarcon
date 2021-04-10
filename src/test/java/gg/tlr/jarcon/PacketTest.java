package gg.tlr.jarcon;

import gg.tlr.jarcon.core.Packet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PacketTest {

    private static final Packet VALID_PACKET = new Packet(false, true, 11, 267, new String[]{"OK", "TLR #0 | Dogfighter's Dream | tlr.gg", "0", "10", "ConquestLarge0", "MP_007", "0", "1", "2", "300", "300", "0", "", "false", "false", "true", "2823", "2800", "136.243.119.204:25200", "", "true", "EU", "ams", "DE", "false"}, null);
    private static final byte[] VALID_BYTES  = {11, 0, 0, 64, 11, 1, 0, 0, 25, 0, 0, 0, 2, 0, 0, 0, 79, 75, 0, 36, 0, 0, 0, 84, 76, 82, 32, 35, 48, 32, 124, 32, 68, 111, 103, 102, 105, 103, 104, 116, 101, 114, 39, 115, 32, 68, 114, 101, 97, 109, 32, 124, 32, 116, 108, 114, 46, 103, 103, 0, 1, 0, 0, 0, 48, 0, 2, 0, 0, 0, 49, 48, 0, 14, 0, 0, 0, 67, 111, 110, 113, 117, 101, 115, 116, 76, 97, 114, 103, 101, 48, 0, 6, 0, 0, 0, 77, 80, 95, 48, 48, 55, 0, 1, 0, 0, 0, 48, 0, 1, 0, 0, 0, 49, 0, 1, 0, 0, 0, 50, 0, 3, 0, 0, 0, 51, 48, 48, 0, 3, 0, 0, 0, 51, 48, 48, 0, 1, 0, 0, 0, 48, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 102, 97, 108, 115, 101, 0, 5, 0, 0, 0, 102, 97, 108, 115, 101, 0, 4, 0, 0, 0, 116, 114, 117, 101, 0, 4, 0, 0, 0, 50, 56, 50, 51, 0, 4, 0, 0, 0, 50, 56, 48, 48, 0, 21, 0, 0, 0, 49, 51, 54, 46, 50, 52, 51, 46, 49, 49, 57, 46, 50, 48, 52, 58, 50, 53, 50, 48, 48, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 116, 114, 117, 101, 0, 2, 0, 0, 0, 69, 85, 0, 3, 0, 0, 0, 97, 109, 115, 0, 2, 0, 0, 0, 68, 69, 0, 5, 0, 0, 0, 102, 97, 108, 115, 101, 0};

    @Test
    public void testPacketDecode() {
        assertEquals(VALID_PACKET, Packet.decode(VALID_BYTES));
    }

    @Test
    public void testPacketEncode() {
        assertArrayEquals(VALID_BYTES, VALID_PACKET.encode());
    }

}