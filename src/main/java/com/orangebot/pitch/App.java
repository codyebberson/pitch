package com.orangebot.pitch;

import com.orangebot.pitch.strats.SimpleStrategy;

public class App {
    public static void main(String[] args) {
        if (args.length >= 1 && args[0].equals("bigdata")) {
            bigData();
        } else {
            singleGame();
        }
    }

    public static void singleGame() {
        final SimpleStrategy s = new SimpleStrategy();
        final PitchGame pitch = new PitchGame(s, s, s, s);
        pitch.setLoggingEnabled(true);
        pitch.playRound();
    }


    public static void bigData() {
        final SimpleStrategy s = new SimpleStrategy();
        for (int i = 0; i < 100; i++) {
            final PitchGame pitch = new PitchGame(s, s, s, s);
            pitch.playRound();
            System.out.println(pitch.getBidToken() + "," + pitch.getScore(0));
        }
    }
}
