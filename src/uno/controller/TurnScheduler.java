package uno.controller;

import javax.swing.Timer;
import uno.util.Constants;

public class TurnScheduler {
    private Timer timer;

    public TurnScheduler(GameController gameController) {
        if (gameController == null) {
            throw new IllegalArgumentException("GameController cannot be null");
        }
        timer = new Timer(Constants.BOT_DELAY_MS, e -> gameController.playBotTurn());
        timer.setRepeats(false);
    }

    public void schedule() {
        timer.restart();
    }

    public void stop() {
        timer.stop();
    }
}
