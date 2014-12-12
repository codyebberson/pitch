package com.orangebot.pitch;

import com.orangebot.pitch.strats.SimpleStrategy;

public class App {
    public static void main(String[] args) {
        final SimpleStrategy s = new SimpleStrategy();
        final PitchGame pitch = new PitchGame(s, s, s, s);
        pitch.setLoggingEnabled(true);
        pitch.playGame();
    }
}
