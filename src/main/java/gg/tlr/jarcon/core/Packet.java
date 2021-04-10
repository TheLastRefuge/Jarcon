package gg.tlr.jarcon.core;

import gg.tlr.jarcon.Util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

public record Packet(
        boolean serverPacket,
        boolean response,
        int sequence,
        int size,
        String[] words,
        Instant timestamp) {

    public static final int HEADER_SIZE = 12;

    public static Packet create(int sequence, String... words) {
        final int size = HEADER_SIZE + Arrays.stream(words).mapToInt(s -> s.getBytes().length + 4 + 1).sum();

        return new Packet(false, false, sequence, size, words, Instant.now());
    }

    public static Packet decode(byte[] raw) {
        final ByteBuffer buffer = ByteBuffer.wrap(raw);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        final int header = buffer.getInt(0);
        final int size = buffer.getInt(4);
        final int numWords = buffer.getInt(8);

        boolean serverPacket = (header & 0x80000000) != 0;  //First bit flag
        boolean response = (header & 0x40000000) != 0;      //Second bit flag
        int sequence = Util.unsignedToSigned(header);       //Ignore flags

        if (size > 16384) throw new RuntimeException("Invalid packet size");
        String[] words = new String[numWords];

        buffer.position(HEADER_SIZE);
        for (int n = 0; n < numWords; n++) {
            final int wordSize = buffer.getInt();

            words[n] = new String(raw, buffer.position(), wordSize);

            //Skip word & trailing null byte
            buffer.position(buffer.position() + wordSize + 1);
        }

        return new Packet(serverPacket, response, sequence, size, words, Instant.now());
    }

    public byte[] encode() {
        final ByteBuffer buffer = ByteBuffer.allocate(this.size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        int header = this.sequence;
        if (serverPacket) header |= 0x80000000;
        if (response) header |= 0x40000000;

        int size = (int) Integer.toUnsignedLong(this.size);
        int numWords = (int) Integer.toUnsignedLong(this.words.length);

        buffer.putInt(0, header);
        buffer.putInt(4, size);
        buffer.putInt(8, numWords);

        buffer.position(Packet.HEADER_SIZE);
        for (String s : this.words) {
            final byte[] word = s.getBytes();
            buffer.putInt(word.length);
            buffer.put(word);
            buffer.put((byte) 0);
        }

        return buffer.array();
    }

    public String message() {
        return String.join(" ", words);
    }

    public String status() {
        return words[0];
    }

    public String word() {
        return words[1];
    }

    public String[] data() {
        return Arrays.copyOfRange(words, 1, words.length);
    }

    @Override
    public String toString() {
        return "Packet{" +
                "serverPacket=" + serverPacket +
                ", response=" + response +
                ", sequence=" + sequence +
                ", size=" + size +
                ", words=" + Arrays.toString(words) +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Packet packet = (Packet) o;
        return serverPacket == packet.serverPacket &&
                response == packet.response &&
                sequence == packet.sequence &&
                size == packet.size &&
                Arrays.equals(words, packet.words);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(serverPacket, response, sequence, size);
        result = 31 * result + Arrays.hashCode(words);
        return result;
    }
}
