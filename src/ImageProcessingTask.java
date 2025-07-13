import java.util.concurrent.RecursiveAction;

public class ImageProcessingTask extends RecursiveAction {

    private final String inputPath;
    private final String outputPath;

    public ImageProcessingTask(String inputPath, String outputPath) {
        this.inputPath = inputPath;
        this.outputPath = outputPath;
    }

    @Override
    protected void compute() {
        try {
            ImageProcessor.processAndSave(inputPath, outputPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
