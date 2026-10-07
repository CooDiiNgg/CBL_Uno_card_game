package uno.util;

public enum Value {
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    NINE(9),
    ZERO(0),
    SKIP(10),
    REVERSE(11),
    DRAW_TWO(12),
    WILD(13),
    WILD_DRAW_FOUR(14);

    private final int val;

    Value(int value){
        this.val = value;
    }

    public int getValue() {
        return val;
    }

    public static Value[] getAllValues() {
        return new Value[]{ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, ZERO, SKIP, REVERSE, DRAW_TWO};
    }

    public static Value[] getSpecialValues() {
        return new Value[]{WILD, WILD_DRAW_FOUR};
    }
}
