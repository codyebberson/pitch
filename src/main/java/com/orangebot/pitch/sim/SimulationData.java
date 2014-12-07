package com.orangebot.pitch.sim;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimulationData {
    private final Map<String, SimulationRow> rows;

    public SimulationData() {
        rows = new HashMap<>();
    }

    public void add(String token, int points) {
        SimulationRow row = rows.get(token);
        if (row == null) {
            row = new SimulationRow(token);
            rows.put(token, row);
        }
        row.getBuckets()[points]++;
    }

    public void write(File file) throws IOException {
        try (PrintWriter out = new PrintWriter(file)) {
            write(out);
        }
    }

    public void write(PrintWriter out) throws IOException {
        for (SimulationRow row : rows.values()) {
            row.calculateStats();
            out.println(row);
        }
    }

    public void print(int n) {
        List<SimulationRow> list = new ArrayList<>(rows.values());

        for (SimulationRow row : list) {
            row.calculateStats();
        }

        Collections.sort(list, new Comparator<SimulationRow>() {
            @Override
            public int compare(SimulationRow r1, SimulationRow r2) {
                return -Double.compare(r1.getMean(), r2.getMean());
            }});

        System.out.println("Best hands");
        for (int i = 0; i < Math.min(list.size(), n); i++) {
            System.out.println(list.get(i));
        }

        Collections.sort(list, new Comparator<SimulationRow>() {
            @Override
            public int compare(SimulationRow r1, SimulationRow r2) {
                return -Integer.compare(r1.getCount(), r2.getCount());
            }});

        System.out.println("Most common hands");
        for (int i = 0; i < Math.min(list.size(), n); i++) {
            System.out.println(list.get(i));
        }

        System.out.println(rows.size() + " unique hands");
    }
}
