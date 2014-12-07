package com.orangebot.pitch.sim;

import java.io.File;
import java.io.IOException;

import com.orangebot.pitch.PitchGame;
import com.orangebot.pitch.strats.SimpleStrategy;

public class Simulation {
    private final Object lockObject;
    private final SimulationData data;
    private long lastWriteTime;
    private int count;

    public Simulation() throws InterruptedException {
        this.lockObject = new Object();
        this.data = new SimulationData();
        this.lastWriteTime = System.currentTimeMillis();

        SimulationThread[] threads = new SimulationThread[4];

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new SimulationThread();
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }
    }

    public void add(String token, int points) {
        synchronized (lockObject) {
            data.add(token, points);
            count++;

            if (count % 10000 == 0) {
                System.out.print(".");
            }

            if (count % 1000000 == 0) {
                final long endTime = System.currentTimeMillis();
                final double duration = (endTime - lastWriteTime) / 1000.0;
                System.out.println("  " + duration + " seconds");
                try {
                    data.write(new File("output-" + System.currentTimeMillis() + ".csv"));
                } catch (IOException e) {
                    System.out.println(e);
                }
                lastWriteTime = System.currentTimeMillis();
            }
        }
    }

    public class SimulationThread extends Thread {
        @Override
        public void run() {
            final SimpleStrategy s = new SimpleStrategy();
            final PitchGame pitch = new PitchGame(s, s, s, s);
            while (true) {
                pitch.resetGame();
                pitch.playRound();
                add(pitch.getBidToken(), pitch.getScore(0));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        new Simulation();
    }
}
