import java.awt.image.BufferedImage;
import java.awt.Color;

public class Filters {
    public static BufferedImage apply(String type, BufferedImage img) {
        BufferedImage result = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                Color c = new Color(img.getRGB(x, y));
                Color newColor;
                switch (type.toLowerCase()) {
                    case "gray":
                        int avg = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
                        newColor = new Color(avg, avg, avg);
                        break;
                    case "warm":
                        newColor = new Color(
                            Math.min(255, c.getRed() + 30),
                            c.getGreen(),
                            Math.max(0, c.getBlue() - 30)
                        );
                        break;
                    case "vangogh":
                        return ImageProcessor.applyVanGoghFilter(img);
                    default:
                        newColor = c;
                        break;
                }
                result.setRGB(x, y, newColor.getRGB());
            }
        }
        return result;
    }
}
