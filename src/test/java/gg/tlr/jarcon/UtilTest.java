package gg.tlr.jarcon;

import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UtilTest {

    @Test
    public void hashPassword() throws NoSuchAlgorithmException {
        final String actual = Util.hashPassword("8FpRebLgRR42Y6Kw", "2EE5D80FB487A5029B2274E664997A5C");
        assertEquals("1CA751799D2E62BF4728323332485C58", actual);
    }
}