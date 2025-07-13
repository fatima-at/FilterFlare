import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Choose a filter: gray, warm, vangogh");
        String filter = scanner.nextLine().toLowerCase();
        List<String> modes = List.of("sequential", "parallel", "forkjoin");

        File inputDir = new File("input");
        File[] files = inputDir.listFiles((dir, name) -> name.endsWith(".jpeg") || name.endsWith(".png"));
        if (files == null || files.length == 0) {
            System.out.println("No images found in 'images/' folder.");
            return;
        }

        List<Benchmark.SeriesData> results = new ArrayList<>();

        for (String mode : modes) {
            File outputDir = new File("output_" + mode + "_" + filter);
            outputDir.mkdirs();

            System.out.println("\nRunning mode: " + mode.toUpperCase());

            Runnable task;
            switch (mode) {
                case "sequential":
                    task = () -> runSequentialWithProgress(files, outputDir, filter);
                    break;
                case "parallel":
                    task = () -> runParallelWithProgress(files, outputDir, filter);
                    break;
                case "forkjoin":
                    task = () -> runForkJoinWithProgress(files, outputDir, filter);
                    break;
                default:
                    continue;
            }

            Benchmark.measure(task, mode, results);
        }

        SwingUtilities.invokeLater(() -> Benchmark.showCharts(results));
    }

    static void runSequentialWithProgress(File[] files, File outputDir, String filter) {
        for (int i = 0; i < files.length; i++) {
            process(files[i], outputDir, filter);
            printProgress(i + 1, files.length);
        }
    }

    static void runParallelWithProgress(File[] files, File outputDir, String filter) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        CountDownLatch latch = new CountDownLatch(files.length);
        for (File f : files) {
            executor.submit(() -> {
                process(f, outputDir, filter);
                synchronized (System.out) {
                    printProgress(files.length - (int) latch.getCount() + 1, files.length);
                }
                latch.countDown();
            });
        }
        executor.shutdown();
        try {
            latch.await();
        } catch (InterruptedException ignored) {}
    }

    static void runForkJoinWithProgress(File[] files, File outputDir, String filter) {
        AtomicInteger counter = new AtomicInteger(0);
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(new ImageTaskWithProgress(files, 0, files.length, outputDir, filter, counter));
    }

    static void process(File file, File outDir, String filter) {
        try {
            BufferedImage img = ImageIO.read(file);
            BufferedImage out = Filters.apply(filter, img);
            File output = new File(outDir, file.getName());
            ImageIO.write(out, "jpg", output);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void printProgress(int current, int total) {
        int width = 30;
        int done = (int)((current / (double) total) * width);
        String bar = "[" + "=".repeat(done) + " ".repeat(width - done) + "] " + current + "/" + total;
        System.out.print("\r" + bar);
        if (current == total) System.out.println();
    }

    static class ImageTaskWithProgress extends RecursiveAction {
        File[] files;
        int start, end;
        File outDir;
        String filter;
        AtomicInteger counter;

        public ImageTaskWithProgress(File[] files, int start, int end, File outDir, String filter, AtomicInteger counter) {
            this.files = files;
            this.start = start;
            this.end = end;
            this.outDir = outDir;
            this.filter = filter;
            this.counter = counter;
        }

        @Override
        protected void compute() {
            if (end - start <= 2) {
                for (int i = start; i < end; i++) {
                    process(files[i], outDir, filter);
                    int current = counter.incrementAndGet();
                    synchronized (System.out) {
                        printProgress(current, files.length);
                    }
                }
            } else {
                int mid = (start + end) / 2;
                invokeAll(
                    new ImageTaskWithProgress(files, start, mid, outDir, filter, counter),
                    new ImageTaskWithProgress(files, mid, end, outDir, filter, counter)
                );
            }
        }
    }
}
