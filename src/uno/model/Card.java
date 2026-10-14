package uno.model;
import uno.util.Color;
import uno.util.Value;

public class Card {
    private Color color;
    private int value;

    public Card(Color color, int value) {
        this.color = color;
        this.value = value;
    }

    public Color getColor() {
        return color;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        String valueName = Value.fromValue(value).name();
        if (color == Color.SPECIAL) {
            return valueName;
        }
        return color.name() + " " + valueName;
    }
}