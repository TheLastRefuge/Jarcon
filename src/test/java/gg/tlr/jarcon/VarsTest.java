package gg.tlr.jarcon;

import gg.tlr.jarcon.bf3.BF3Vars;
import gg.tlr.jarcon.core.JarconClient;
import gg.tlr.jarcon.core.Var;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class VarsTest {

    private static JarconClient client;

    @BeforeAll
    public static void beforeAll() throws Exception {
        client = TestEnv.newClient();
    }

    @Test
    public void getAllVars() {
        boolean failed = false;

        for (Var<?> var : BF3Vars.vars()) {
            try {
                System.out.println(var.getName() + ": " + client.getVar(var).queue().get());
            } catch (Exception e) {
                failed = true;
                e.printStackTrace();
            }
        }

        assertFalse(failed);
    }

}