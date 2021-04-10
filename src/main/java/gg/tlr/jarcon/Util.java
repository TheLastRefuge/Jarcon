package gg.tlr.jarcon;

import gg.tlr.jarcon.core.Jarcon;
import gg.tlr.jarcon.core.Var;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

//TODO Fetch snippets straight from StackOverflow
public final class Util {

    private Util() { }

    public static int unsignedToSigned(int unsigned) {
        return (unsigned & 0x3FFFFFFF);
    }

    public static byte[] hashToByteArray(String hexString) {
        final byte[] bytes = new byte[hexString.length() / 2];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseUnsignedInt(hexString.substring(i * 2, i * 2 + 2), 16);
        }

        return bytes;
    }

    public static String hashPassword(String password, String salt) throws NoSuchAlgorithmException {
        final byte[] passwordBytes = password.getBytes();
        final byte[] saltBytes = Util.hashToByteArray(salt);
        final byte[] combined = new byte[saltBytes.length + passwordBytes.length];
        System.arraycopy(saltBytes, 0, combined, 0, saltBytes.length);
        System.arraycopy(passwordBytes, 0, combined, saltBytes.length, passwordBytes.length);

        MessageDigest md = MessageDigest.getInstance("MD5");
        return toHex(md.digest(combined));
    }

    public static String toHex(byte[] bytes) {
        final BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "X", bi);
    }

    public static InetSocketAddress parseIpPort(String ipPortPair) {
        final String[] split = ipPortPair.split(":");
        return new InetSocketAddress(split[0], Integer.parseInt(split[1]));
    }

    public static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    public static String binaryInt(int i) {
        return String.format("%32s", Integer.toBinaryString(i)).replace(' ', '0');
    }

    public static String[] flattenStringArray(Object... words) {
        //Flatten nested arrays
        return Arrays.stream(words).flatMap(o -> {
            if (o instanceof Object[] array) return Arrays.stream(array).map(Object::toString);
            else return Stream.of(o.toString());
        }).toArray(String[]::new);
    }

    public static List<Var<?>> listVars(Class<?> clazz) {
        final List<Var<?>> list = new ArrayList<>();

        try {
            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) && field.getType().equals(Var.class)) {
                    try {
                        field.setAccessible(true);
                        list.add((Var<?>) field.get(null));
                        field.setAccessible(false);
                    } catch (Exception e) {
                        Jarcon.getLogger().error("Exception encountered while listing fields", e);
                    }
                }
            }

        } catch (SecurityException e) {
            Jarcon.getLogger().error("Failed to list Vars reflectively", e);
        }

        return Collections.unmodifiableList(list);
    }
}
