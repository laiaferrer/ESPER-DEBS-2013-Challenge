package com.example;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.xy.DefaultXYZDataset;

import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;

import javax.swing.*;
import java.awt.*;

public class HeatmapVisualization extends JFrame {
    /*private EPAdministrator admin;
    private DefaultXYZDataset dataset;
    private XYPlot plot;

    // Field dimensions
    private static final int GRID_ROWS = 8;
    private static final int GRID_COLS = 13;
    private static final double FIELD_X_MIN = 0;
    private static final double FIELD_X_MAX = 52489;
    private static final double FIELD_Y_MIN = -33960;
    private static final double FIELD_Y_MAX = 33965;
    private static final double CELL_WIDTH = (FIELD_X_MAX - FIELD_X_MIN) / GRID_COLS;
    private static final double CELL_HEIGHT = (FIELD_Y_MAX - FIELD_Y_MIN) / GRID_ROWS;

    // Heatmap matrix & total duration tracker
    private final double[][] heatmapMatrix = new double[GRID_ROWS][GRID_COLS];
    private double totalDuration = 0;
    private boolean dataProcessingFinished = false;*/

    public HeatmapVisualization(EPAdministrator admin) {
        /*super("Football Player Heatmap (Final)");
        this.admin = admin;
        this.dataset = new DefaultXYZDataset();

        JFreeChart chart = createChart();

        // Chart Panel
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(1050, (int) (1050 * (67925.0 / 52477.0))));
        this.add(chartPanel);
        this.setSize(600, 800);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Listen for HeatMapEvents from Esper
        String eplQuery = "select ts, playerId, cellX, cellY, duration from HeatMapEvent";
        EPStatement statement = admin.createEPL(eplQuery);

        statement.addListener((newData, oldData) -> {
            if (newData != null) {
                for (EventBean event : newData) {
                    int col = ((Double) event.get("cellX")).intValue();
                    int row = ((Double) event.get("cellY")).intValue();
                    long duration = (long) event.get("duration");

                    System.out.println("Received Event -> Row: " + row + ", Col: " + col + ", Duration: " + duration);

                    updateHeatmap(row, col, duration);
                }
            }
        });

        // Wait for processing to finish, then render the heatmap
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                dataProcessingFinished = true; 
                renderHeatmap();
                this.setVisible(true);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();*/
    }

    /*private JFreeChart createChart() {
        JFreeChart chart = ChartFactory.createScatterPlot(
                "Final Heatmap - Player Time in Cells",
                "Field X Position (mm)",
                "Field Y Position (mm)",
                dataset
        );

        plot = chart.getXYPlot();
        XYBlockRenderer renderer = new XYBlockRenderer();

        // **Smooth Paint Scale with 15+ Colors**
        PaintScale paintScale = new PaintScale() {
            @Override
            public double getLowerBound() { return 0; }
            @Override
            public double getUpperBound() { return 100; }

            @Override
            public Paint getPaint(double value) {
                if (value == 0) return Color.GRAY; // No activity

                float ratio = (float) value / 100; // Normalize (0 - 1)

                // **Color transitions:**
                // Blue (0%) → Light Blue (10%) → Cyan (25%) → Green (40%) → Yellow (55%)
                // → Orange (70%) → Red (85%) → Dark Red (100%)

                int red, green, blue;
                if (ratio <= 0.10) {  // **Dark Blue → Light Blue**
                    red = (int) (0 * ratio / 0.10);
                    green = (int) (50 * ratio / 0.10);
                    blue = 255;
                } else if (ratio <= 0.25) { // **Light Blue → Cyan**
                    red = 0;
                    green = (int) (255 * (ratio - 0.10) / 0.15);
                    blue = 255;
                } else if (ratio <= 0.40) { // **Cyan → Green**
                    red = 0;
                    green = 255;
                    blue = (int) (255 - 255 * (ratio - 0.25) / 0.15);
                } else if (ratio <= 0.55) { // **Green → Yellow**
                    red = (int) (255 * (ratio - 0.40) / 0.15);
                    green = 255;
                    blue = 0;
                } else if (ratio <= 0.70) { // **Yellow → Orange**
                    red = 255;
                    green = (int) (255 - 155 * (ratio - 0.55) / 0.15);
                    blue = 0;
                } else if (ratio <= 0.85) { // **Orange → Red**
                    red = 255;
                    green = (int) (100 - 100 * (ratio - 0.70) / 0.15);
                    blue = 0;
                } else { // **Red → Dark Red**
                    red = (int) (255 - 50 * (ratio - 0.85) / 0.15);
                    green = 0;
                    blue = 0;
                }

                return new Color(red, green, blue);
            }
        };

        renderer.setPaintScale(paintScale);
        plot.setRenderer(renderer);

        // Set Axis limits
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setLowerBound(FIELD_X_MIN);
        domainAxis.setUpperBound(FIELD_X_MAX);
        domainAxis.setTickUnit(new NumberTickUnit(CELL_WIDTH));

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setLowerBound(FIELD_Y_MIN);
        rangeAxis.setUpperBound(FIELD_Y_MAX);
        rangeAxis.setTickUnit(new NumberTickUnit(CELL_HEIGHT));

        return chart;
    }

    private void updateHeatmap(int row, int col, long duration) {
        if (row < 0 || row >= GRID_ROWS || col < 0 || col >= GRID_COLS) {
            System.err.println("Invalid grid position: Row " + row + ", Col: " + col);
            return;
        }

        heatmapMatrix[row][col] += duration;
        totalDuration += duration;

        //System.out.println("Updated HeatMap Matrix -> Row: " + row + ", Col: " + col + " = " + heatmapMatrix[row][col]);
    }

    private void renderHeatmap() {
        if (!dataProcessingFinished || totalDuration == 0) return;

        double[][] heatmapData = new double[3][GRID_ROWS * GRID_COLS];
        int index = 0;

        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                double x = FIELD_X_MIN + (col * CELL_WIDTH) + (CELL_WIDTH / 2);
                double y = FIELD_Y_MAX - (row * CELL_HEIGHT) - (CELL_HEIGHT / 2);

                double intensity = (heatmapMatrix[row][col] / totalDuration) * 100;

                heatmapData[0][index] = x;
                heatmapData[1][index] = y;
                heatmapData[2][index] = intensity;
                index++;
            }
        }

        ((XYBlockRenderer) plot.getRenderer()).setBlockHeight(CELL_HEIGHT);
        ((XYBlockRenderer) plot.getRenderer()).setBlockWidth(CELL_WIDTH);

        dataset = new DefaultXYZDataset();
        dataset.addSeries("Heatmap Data", heatmapData);
        plot.setDataset(dataset);

        repaint();
    }*/
}
