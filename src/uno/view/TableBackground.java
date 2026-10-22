package uno.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

final class TableBackground {
    private final TexturePaint feltTexture;
    private BufferedImage cache;
    private int cachedWidth;
    private int cachedHeight;
    private double cachedScale;

    TableBackground() {
        BufferedImage tile = new BufferedImage(96, 96, BufferedImage.TYPE_INT_ARGB);
        Random random = new Random(27);
        for (int y = 0; y < tile.getHeight(); y++) {
            for (int x = 0; x < tile.getWidth(); x++) {
                int alpha = random.nextInt(15);
                int rgb = random.nextBoolean() ? 0xFFFFFF : 0;
                tile.setRGB(x, y, (alpha << 24) | rgb);
            }
        }
        feltTexture = new TexturePaint(tile, new Rectangle(0, 0, 96, 96));
    }

    void paint(Graphics2D graphics, int width, int height) {
        if (width <= 0 || height <= 0) { return; }
        double scale = graphics.getDeviceConfiguration().getDefaultTransform().getScaleX();
        if (cache == null || cachedWidth != width || cachedHeight != height || cachedScale != scale) {
            cache = graphics.getDeviceConfiguration().createCompatibleImage(
                    (int) Math.ceil(width * scale), (int) Math.ceil(height * scale));
            Graphics2D g = cache.createGraphics();
            g.scale(scale, scale);
            render(g, width, height);
            g.dispose();
            cachedWidth = width;
            cachedHeight = height;
            cachedScale = scale;
        }
        graphics.drawImage(cache, 0, 0, width, height, null);
    }

    private void render(Graphics2D g, int width, int height) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(new GradientPaint(0, 0, new Color(0x805135), width, height, new Color(0x35251E)));
        g.fillRect(0, 0, width, height);
        for (int y = 2; y < height; y += 5) {
            g.setColor(new Color(0, 0, 0, y % 3 == 0 ? 26 : 12));
            g.drawLine(0, y, width, y + 3);
        }
        RoundRectangle2D felt = new RoundRectangle2D.Double(14, 14,
                Math.max(1, width - 28), Math.max(1, height - 28), 55, 55);
        g.setColor(new Color(0, 0, 0, 100));
        g.setStroke(new BasicStroke(10));
        g.draw(felt);
        g.setPaint(new RadialGradientPaint(width * 0.5f, height * 0.42f,
                Math.max(width, height) * 0.72f,
                new float[]{0f, 0.65f, 1f},
                new Color[]{new Color(0x28765C), new Color(0x15513E), new Color(0x0C352C)}));
        g.fill(felt);
        g.setPaint(feltTexture);
        g.fill(felt);
        g.setColor(new Color(204, 190, 132, 90));
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(23, 23, Math.max(1, width - 46), Math.max(1, height - 46), 40, 40);
    }
}
