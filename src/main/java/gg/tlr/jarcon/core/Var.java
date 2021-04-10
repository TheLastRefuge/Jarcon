package gg.tlr.jarcon.core;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.EnumSet;

public abstract class Var<T> {

    public static final java.lang.String PREFIX = "vars.";

    private final java.lang.String name;
    private final EnumSet<Flag>    flags;

    protected Var(@Nonnull java.lang.String name, @Nonnull Flag... flags) {
        this.flags = flags.length == 0 ? EnumSet.noneOf(Flag.class) : EnumSet.copyOf(Arrays.asList(flags));
        this.name = this.flags.contains(Flag.NON_VAR) ? name : PREFIX + name;
    }

    public java.lang.String getName() {
        return name;
    }

    public abstract T parse(@Nonnull java.lang.String s);

    public boolean isReadOnly() {
        return flags.contains(Flag.READ_ONLY);
    }

    public boolean isClamped() {
        return flags.contains(Flag.CLAMPED);
    }

    protected void validate(T value) {

    }

    protected void iae(java.lang.String message) {
        throw new IllegalArgumentException("%s: %s".formatted(name, message));
    }

    public static class Boolean extends Var<java.lang.Boolean> {
        public Boolean(@Nonnull java.lang.String name, @Nonnull Flag... flags) {
            super(name, flags);
        }

        @Override
        public java.lang.Boolean parse(@Nonnull java.lang.String s) {
            return java.lang.Boolean.parseBoolean(s);
        }
    }

    public static class Integer extends Var<java.lang.Integer> {
        public Integer(@Nonnull java.lang.String name, @Nonnull Flag... flags) {
            super(name, flags);
        }

        @Override
        public java.lang.Integer parse(@Nonnull java.lang.String s) {
            return java.lang.Integer.parseInt(s);
        }
    }

    public static class String extends Var<java.lang.String> {
        public String(@Nonnull java.lang.String name, @Nonnull Flag... flags) {
            super(name, flags);
        }

        @Override
        public java.lang.String parse(@Nonnull java.lang.String s) {
            return s;
        }
    }

    public static class Enum<T extends java.lang.Enum<T>> extends Var<T> {
        private final Class<T> clazz;

        public Enum(@Nonnull java.lang.String name, @Nonnull Class<T> clazz, @Nonnull Flag... flags) {
            super(name, flags);
            this.clazz = clazz;
        }

        @Override
        public T parse(@Nonnull java.lang.String s) {
            return java.lang.Enum.valueOf(clazz, s.toUpperCase());
        }
    }

    public enum Flag {
        READ_ONLY,
        CLAMPED,
        NON_VAR
    }
}
