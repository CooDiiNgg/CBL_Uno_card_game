package uno.view.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import uno.util.Color;
import uno.view.CardComponent;

public class ColorPickerDialog {
    public static Color choose(Component parent) {
        JDialog dialog = new JDialog(parent instanceof java.awt.Window ? (java.awt.Window) parent
                : parent == null ? null : SwingUtilities.getWindowAncestor(parent),
                "Choose a colour", Dialog.ModalityType.APPLICATION_MODAL);
        Color[] selected = {null};
        JPanel content = createContent(color -> {
            selected[0] = color;
            dialog.dispose();
        });
        dialog.setContentPane(content);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return selected[0];
    }

    private static JPanel createContent(Consumer<Color> choose) {
        java.awt.Color background = new java.awt.Color(0x192332);
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(background);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 26, 18, 26));
        JPanel heading = new JPanel(new GridLayout(2, 1, 0, 8));
        heading.setOpaque(false);
        JLabel title = new JLabel("Choose the next colour", SwingConstants.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        title.setForeground(java.awt.Color.WHITE);
        JLabel subtitle = new JLabel("Your wild card sets the colour of play", SwingConstants.CENTER);
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        subtitle.setForeground(new java.awt.Color(0xC0CAD8));
        heading.add(title);
        heading.add(subtitle);
        panel.add(heading, BorderLayout.NORTH);
        JPanel choices = new JPanel(new GridLayout(2, 2, 12, 12));
        choices.setOpaque(false);
        for (Color color : Color.getAllColors()) {
            JButton button = new ColourButton(color);
            button.addActionListener(event -> choose.accept(color));
            choices.add(button);
            String key = color.name().substring(0, 1);
            panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), key);
            panel.getActionMap().put(key, new AbstractAction() {
                @Override public void actionPerformed(ActionEvent event) { choose.accept(color); }
            });
        }
        panel.add(choices, BorderLayout.CENTER);
        JButton cancel = new JButton("Cancel · Esc");
        cancel.setForeground(new java.awt.Color(0xD7DFEA));
        cancel.setContentAreaFilled(false);
        cancel.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));
        cancel.addActionListener(event -> choose.accept(null));
        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.add(cancel);
        panel.add(footer, BorderLayout.SOUTH);
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
        panel.getActionMap().put("cancel", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent event) { choose.accept(null); }
        });
        return panel;
    }

    private static class ColourButton extends JButton {
        private final Color color;

        ColourButton(Color color) {
            super(color.name());
            this.color = color;
            setPreferredSize(new Dimension(170, 96));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            setToolTipText("Choose " + color.name().toLowerCase() + " (" + color.name().charAt(0) + ")");
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            java.awt.Color base = CardComponent.toAwtColor(color);
            if (getModel().isRollover() || hasFocus()) { base = base.brighter(); }
            if (getModel().isPressed()) { base = base.darker(); }
            g.setPaint(new GradientPaint(0, 0, base, getWidth(), getHeight(), base.darker()));
            g.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 20, 20);
            g.setColor(hasFocus() ? java.awt.Color.WHITE : new java.awt.Color(255, 255, 255, 65));
            g.setStroke(new java.awt.BasicStroke(hasFocus() ? 3 : 1));
            g.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 20, 20);
            g.setColor(color == Color.YELLOW ? new java.awt.Color(0x302A11) : java.awt.Color.WHITE);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 19));
            String label = color.name();
            g.drawString(label, (getWidth() - g.getFontMetrics().stringWidth(label)) / 2, 45);
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            String shortcut = "Press " + color.name().charAt(0);
            g.drawString(shortcut, (getWidth() - g.getFontMetrics().stringWidth(shortcut)) / 2, 67);
            g.dispose();
        }
    }
}
