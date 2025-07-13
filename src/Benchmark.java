import com.sun.management.OperatingSystemMXBean;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

public class Benchmark {
    private static final OperatingSystemMXBean osBean =
        ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

    public static void measure(Runnable task, String label, List<SeriesData> results) {
        XYSeries cpuSeries = new XYSeries(label + " CPU%");

        Thread monitor = new Thread(() -> {
            long begin = System.currentTimeMillis();
            while (!Thread.currentThread().isInterrupted()) {
                double cpu = osBean.getProcessCpuLoad() * 100;
                double t = System.currentTimeMillis() - begin;
                if (cpu >= 0) 
                    cpuSeries.add(t, cpu);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        System.gc();
        long memBefore = getUsedMemory();
        monitor.start();

        long start = System.nanoTime();
        task.run();
        long end = System.nanoTime();

        monitor.interrupt();

        System.gc();
        long memAfter = getUsedMemory();

        double elapsedMs = (end - start) / 1_000_000.0;
        long memUsedBytes = memAfter - memBefore;

        System.out.printf("%s took %.2f ms, Memory usage: %.2f MB%n", label, elapsedMs, memUsedBytes / (1024.0 * 1024.0));

        results.add(new SeriesData(label, cpuSeries, elapsedMs, memUsedBytes));
    }

    private static long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    public static void showCharts(List<SeriesData> results) {
        XYSeriesCollection cpuCollection = new XYSeriesCollection();
        for (SeriesData sd : results) cpuCollection.addSeries(sd.cpuSeries);
        JFreeChart cpuChart = ChartFactory.createXYLineChart(
            "CPU Usage Over Time", "Time (ms)", "CPU (%)", cpuCollection,
            PlotOrientation.VERTICAL, true, true, false);
        ChartPanel cpuPanel = new ChartPanel(cpuChart);

        DefaultCategoryDataset timeDataset = new DefaultCategoryDataset();
        for (SeriesData sd : results) {
            timeDataset.addValue(sd.elapsedMs, "Elapsed Time (ms)", sd.label);
        }
        JFreeChart timeChart = ChartFactory.createBarChart(
            "Total Processing Time", "Mode", "Time (ms)", timeDataset,
            PlotOrientation.VERTICAL, false, true, false);
        ChartPanel timePanel = new ChartPanel(timeChart);

        DefaultCategoryDataset memDataset = new DefaultCategoryDataset();
        for (SeriesData sd : results) {
            memDataset.addValue(sd.memoryBytes / (1024.0 * 1024.0), "Memory (MB)", sd.label);
        }
        JFreeChart memChart = ChartFactory.createBarChart(
            "Memory Usage", "Mode", "Memory (MB)", memDataset,
            PlotOrientation.VERTICAL, false, true, false);
        ChartPanel memPanel = new ChartPanel(memChart);

        JFrame frame = new JFrame("Performance Compare");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(3, 1));
        frame.add(cpuPanel);
        frame.add(timePanel);
        frame.add(memPanel);
        frame.pack();
        frame.setVisible(true);

        printSpeedup(results);
    }

    private static void printSpeedup(List<SeriesData> results) {
        double seqTime = -1, parTime = -1;
        for (SeriesData sd : results) {
            if (sd.label.equalsIgnoreCase("Sequential")) seqTime = sd.elapsedMs;
            else if (sd.label.equalsIgnoreCase("Parallel")) parTime = sd.elapsedMs;
        }
        if (seqTime > 0 && parTime > 0) {
            double speedup = seqTime / parTime;
            System.out.printf("Speedup of Parallel over Sequential: %.2f x%n", speedup);
        }
    }

    public static class SeriesData {
        public final String label;
        public final XYSeries cpuSeries;
        public final double elapsedMs;
        public final long memoryBytes;

        public SeriesData(String label, XYSeries cpuSeries, double elapsedMs, long memoryBytes) {
            this.label = label;
            this.cpuSeries = cpuSeries;
            this.elapsedMs = elapsedMs;
            this.memoryBytes = memoryBytes;
        }
    }
}
