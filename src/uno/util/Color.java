package uno.util;

public enum Color {
    RED((byte) 1),
    BLUE((byte) 2),
    YELLOW((byte) 4),
    GREEN((byte) 8),
    SPECIAL((byte) 15);

    private final byte col;

    Color(byte color){
        this.col = color;
    }

    public byte getColor() {
        return col;
    }

    public boolean matches(Color other) {
        return other != null && (this.col & other.col) != 0;
    }

    public static Color[] getAllColors() {
        return new Color[]{RED, BLUE, YELLOW, GREEN};
    }

    public static Color[] getSpecialColors() {
        return new Color[]{SPECIAL};
    }
}
