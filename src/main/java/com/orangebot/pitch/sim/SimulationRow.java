package com.orangebot.pitch.sim;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;

public class SimulationRow {
    private final String token;
    private final int[] buckets;
    private final double[] percentages;
    private int count;
    private double mean;

    public SimulationRow(final String token) {
        this.token = token;
        this.buckets = new int[11];
        this.percentages = new double[11];
    }

    public String getToken() {
        return token;
    }

    public int[] getBuckets() {
        return buckets;
    }

    public int getCount() {
        return count;
    }

    public double getMean() {
        return mean;
    }

    public void calculateStats() {
        count = 0;
        for (int i = 0; i < buckets.length; i++) {
            count += buckets[i];
        }

        mean = 0.0;
        for (int i = 0; i < buckets.length; i++) {
            percentages[i] = ((double)buckets[i]) / ((double)count);
            mean += i * percentages[i];
        }
    }

    public int getPointsAtPercentile(double p) {
        double sum = 0.0;
        for (int i = 10; i >= 0; i--) {
            if (sum + percentages[i] > p) {
                return i;
            }
            sum += percentages[i];
        }
        return 0;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(token);
        b.append(",");
        b.append(count);
        b.append(",");
        b.append(String.format("%.2f", mean));

        for (int i = 0; i < buckets.length; i++) {
            b.append(",");
            b.append(buckets[i]);
        }

        return b.toString();
    }

    public void printHtml(PrintWriter out) {
        out.println("<!doctype html>");
        out.println("<html lang=\"en\">");
        out.println("<head>");
        out.println("<meta charset=\"utf-8\">");
        out.println("<title>Pitch - " + token + "</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>" + token + "</h1>");
        out.println("<table border=\"1\" cellspacing=\"0\" cellpadding=\"8\">");
        out.println("<tr>");
        out.println("<td>Average</td>");
        out.println("<td align=\"right\">" + String.format("%.2f", mean) + "</td>");
        out.println("</tr>");
        out.println("<tr>");
        out.println("<td>Median</td>");
        out.println("<td align=\"right\">" + getPointsAtPercentile(0.5) + "</td>");
        out.println("</tr>");
        out.println("<tr>");
        out.println("<td colspan=\"2\"><strong>Confidence Intervals</strong></td>");
        out.println("</tr>");

        double[] confidences = { 0.99, 0.95, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1 };
        for (double confidence : confidences) {
            out.println("<tr>");
            out.println("<td>" + String.format("%.0f", 100.0 * confidence) + "% Confidence</td>");
            out.println("<td align=\"right\">" + getPointsAtPercentile(confidence) + "</td>");
            out.println("</tr>");
        }

        out.println("<tr>");
        out.println("<td colspan=\"2\"><strong>Probability of Points (Cumulative)</strong></td>");
        out.println("</tr>");

        double cumulative = 0.0;
        for (int i = 10; i >= 0; i--) {
            cumulative += percentages[i];
            out.println("<tr>");
            out.println("<td>" + i + " " + (i == 1 ? "Point" : "Points") + "</td>");
            out.println("<td align=\"right\">" + String.format("%.1f", 100.0 * percentages[i]) + "%");
            out.println(" (" + String.format("%.1f", 100.0 * cumulative) + "%)</td>");
            out.println("</tr>");
        }

        out.println("<tr>");
        out.println("<td colspan=\"2\"><strong>Trivia</strong></td>");
        out.println("</tr>");
        out.println("<tr>");
        out.println("<td>Data Points</td>");
        out.println("<td align=\"right\">" + NumberFormat.getInstance().format(count) + "</td>");
        out.println("</tr>");
//        out.println("<tr>");
//        out.println("<td>Probability</td>");
//        out.println("<td>1 in 2,389,455</td>");
//        out.println("</tr>");
        out.println("</table>");
        out.println("</body>");
        out.println("</html>");
    }

    public static void main(String[] args) throws IOException {
//        int[] buckets = new int[] { 0, 0, 0, 1, 2, 56, 746, 244, 680, 4636, 13083 };
//        int[] buckets = new int[] { 0, 126, 270, 296, 279, 775, 2384, 3801, 5291, 4874, 1492 };
        int[] buckets = new int[] { 699034, 1476551, 1511058, 1386631, 1364806, 1321964, 1249990, 1226675, 1248977, 1015221, 368607 };
        SimulationRow row = new SimulationRow("A K Q LJ 2");
        for (int i = 0; i < buckets.length; i++) {
            row.getBuckets()[i] = buckets[i];
        }

        row.calculateStats();

        try (PrintWriter out = new PrintWriter(new File("display-" + System.currentTimeMillis() + ".html"))) {
            row.printHtml(out);
        }
    }
}
