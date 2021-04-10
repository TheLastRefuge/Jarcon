package gg.tlr.jarcon.core;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Arrays;
import java.util.function.Function;

@NotThreadSafe
public class WordBuffer {

    private final String[] words;

    private int position;

    public WordBuffer(String[] words) {
        this.words = words;
    }

    public int size() {
        return words.length;
    }

    public int position() {
        return position;
    }

    public void position(int position) {
        this.position = position;
    }

    public void jump(int relative) {
        position += relative;
    }

    public <T> T read(Function<String, T> parser) {
        return parser.apply(read());
    }

    public <T> T read(int index, Function<String, T> parser) {
        return parser.apply(read(index));
    }

    public String read(int index) {
        return words[index];
    }

    public String read() {
        return read(position++);
    }

    public boolean readBool(int index) {
        return read(index, Boolean::parseBoolean);
    }

    public boolean readBool() {
        return readBool(position++);
    }

    public int readInt(int index) {
        return read(index, Integer::parseInt);
    }

    public int readInt() {
        return readInt(position++);
    }

    public int readUnsignedInt(int index) {
        return read(index, Integer::parseUnsignedInt);
    }

    public int readUnsignedInt() {
        return readUnsignedInt(position++);
    }

    public float readFloat(int index) {
        return read(index, Float::parseFloat);
    }

    public float readFloat() {
        return readFloat(position++);
    }

    public <T extends Enum<T>> T readEnum(Class<T> clazz) {
        return Enum.valueOf(clazz, read().toUpperCase());
    }

    public <T> T readComplex(Function<WordBuffer, T> parser) {
        return parser.apply(this);
    }

    public <T> T readBlock(Function<String[], T> parser, int length) {
        String[] block = Arrays.copyOfRange(words, position, position + length);
        position += length;
        return parser.apply(block);
    }

    public String[] remainder() {
        final String[] result = Arrays.copyOfRange(words, position, words.length);
        position = words.length;
        return result;
    }
}
