package com.orangebot.pitch.sim;

import com.orangebot.pitch.PitchGame;
import com.orangebot.pitch.strats.SimpleStrategy;

public class Simulation {

    public static void main(String[] args) {
        final long startTime = System.currentTimeMillis();

        final SimpleStrategy s = new SimpleStrategy();
        final SimulationData data = new SimulationData();

        final PitchGame pitch = new PitchGame(s, s, s, s);

        for (int i = 0; i < 1000000; i++) {
            pitch.resetGame();
            pitch.playRound();
            data.add(pitch.getBidToken(), pitch.getScore(0));
        }

        final long endTime = System.currentTimeMillis();
        final double duration = (endTime - startTime) / 1000.0;

        data.print(10);

        System.out.println(duration + " seconds");
    }
}
