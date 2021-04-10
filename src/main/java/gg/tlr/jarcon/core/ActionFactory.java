package gg.tlr.jarcon.core;

public final class ActionFactory {

    protected final JarconClient client;

    ActionFactory(JarconClient client) {
        this.client = client;
    }

    public class Packet extends Action<gg.tlr.jarcon.core.Packet> {

        public Packet(boolean secure, Object... words) {
            super(client, secure, words);
        }

        @Override
        protected gg.tlr.jarcon.core.Packet interpret(gg.tlr.jarcon.core.Packet packet) {
            return packet;
        }
    }

    public class Void extends Action<java.lang.Void> {

        public Void(boolean secure, Object... words) {
            super(client, secure, words);
        }

        @Override
        protected java.lang.Void interpret(gg.tlr.jarcon.core.Packet packet) {
            return null;
        }
    }

    public class String extends Action<java.lang.String> {

        public String(boolean secure, Object... words) {
            super(client, secure, words);
        }

        @Override
        protected java.lang.String interpret(gg.tlr.jarcon.core.Packet packet) {
            return packet.word();
        }
    }

    public class Integer extends Action<java.lang.Integer> {

        public Integer(boolean secure, Object... words) {
            super(client, secure, words);
        }

        @Override
        protected java.lang.Integer interpret(gg.tlr.jarcon.core.Packet packet) {
            return java.lang.Integer.parseInt(packet.word());
        }
    }

    public class Boolean extends Action<java.lang.Boolean> {

        public Boolean(boolean secure, Object... words) {
            super(client, secure, words);
        }

        @Override
        protected java.lang.Boolean interpret(gg.tlr.jarcon.core.Packet packet) {
            return java.lang.Boolean.parseBoolean(packet.word());
        }
    }

    public class Float extends Action<java.lang.Float> {

        public Float(boolean secure, Object... words) {
            super(client, secure, words);
        }

        @Override
        protected java.lang.Float interpret(gg.tlr.jarcon.core.Packet packet) {
            return java.lang.Float.parseFloat(packet.word());
        }
    }
}
