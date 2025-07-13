import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.File;
import java.util.concurrent.*;
import javax.imageio.ImageIO;

public class ImageProcessor {

        public static BufferedImage applyVanGoghFilter(BufferedImage src) {
        int width = src.getWidth();
        int height = src.getHeight();
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int rgb = src.getRGB(x, y);
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                float[] hsv = rgbToHsv(red, green, blue);

                if (hsv[0] < 60 || hsv[0] > 180) {
                    hsv[0] = 210f; // Blue
                } else {
                    hsv[0] = 50f; // Yellow
                }

                hsv[1] = Math.min(1f, hsv[1] * 1.3f);
                hsv[2] = Math.min(1f, hsv[2] * 1.2f);

                int[] newRgb = hsvToRgb(hsv[0], hsv[1], hsv[2]);

                newRgb[0] = (newRgb[0] / 64) * 64;
                newRgb[1] = (newRgb[1] / 64) * 64;
                newRgb[2] = (newRgb[2] / 64) * 64;

                int edge = detectEdge(src, x, y);
                if (edge > 60) {
                    newRgb[0] = Math.max(0, newRgb[0] - 50);
                    newRgb[1] = Math.max(0, newRgb[1] - 50);
                    newRgb[2] = Math.max(0, newRgb[2] - 50);
                }

                int finalRgb = (newRgb[0] << 16) | (newRgb[1] << 8) | newRgb[2];
                out.setRGB(x, y, finalRgb);
            }
        }

        return out;
    }

    private static int detectEdge(BufferedImage img, int x, int y) {
        int rgb = img.getRGB(x, y);
        int right = img.getRGB(x + 1, y);
        int down = img.getRGB(x, y + 1);

        int dx = Math.abs((rgb & 0xFF) - (right & 0xFF));
        int dy = Math.abs((rgb & 0xFF) - (down & 0xFF));

        return dx + dy;
    }

    private static float[] rgbToHsv(int r, int g, int b) {
        float[] hsv = new float[3];
        java.awt.Color.RGBtoHSB(r, g, b, hsv);
        hsv[0] *= 360; 
        return hsv;
    }

    private static int[] hsvToRgb(float h, float s, float v) {
        h /= 360f;
        int rgb = java.awt.Color.HSBtoRGB(h, s, v);
        return new int[] {
            (rgb >> 16) & 0xFF,
            (rgb >> 8) & 0xFF,
            rgb & 0xFF
        };
    }
    public static void processAndSave(String inputPath, String outputPath) throws Exception {
        BufferedImage input = ImageIO.read(new File(inputPath));
        BufferedImage output = applyVanGoghFilter(input);
        ImageIO.write(output, "jpeg", new File(outputPath));
    }

    public static void processFolderParallel(File[] files, String outputDir, int threads) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        for (File file : files) {
            executor.submit(() -> {
                try {
                    String outPath = outputDir + "/" + file.getName().replace(".jpeg", "_stylized.jpeg");
                    processAndSave(file.getPath(), outPath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);
    }

    public static void processFolderSequential(File[] files, String outputDir) {
        for (File file : files) {
            try {
                String outPath = outputDir + "/" + file.getName().replace(".jpeg", "_stylized.jpeg");
                processAndSave(file.getPath(), outPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
