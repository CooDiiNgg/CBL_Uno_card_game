package uno.view.dialogs;

import java.awt.Component;
import javax.swing.JOptionPane;
import uno.util.Color;

public class ColorPickerDialog {
    public static Color choose(Component parent) {
        Color[] colors = Color.getAllColors();
        String[] options = new String[colors.length];
        for (int i = 0; i < colors.length; i++) {
            options[i] = colors[i].name();
        }
        int choice = JOptionPane.showOptionDialog(parent, "Choose the next color:", "Wild card",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (choice < 0) {
            return null;
        }
        return colors[choice];
    }
}
