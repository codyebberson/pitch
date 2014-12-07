package com.orangebot.pitch.sim;

public class SimulationRow {
    private final String token;
    private final int[] buckets;
    private int count;
    private double mean;

    public SimulationRow(final String token) {
        this.token = token;
        this.buckets = new int[11];
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
            mean += i * (((double)buckets[i]) / ((double)count));
        }
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
}
