
import at.pwd.boardgame.game.base.WinState;
import at.pwd.boardgame.game.mancala.MancalaGame;
import at.pwd.boardgame.game.mancala.MancalaState;
import at.pwd.boardgame.game.mancala.agent.MancalaAgent;
import at.pwd.boardgame.game.mancala.agent.MancalaAgentAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Final form
 */
@SuppressWarnings("Duplicates")
public class Knuckles_WOOB_C5 extends Knuckles_WOOB_C3 {
  @Override
  protected double getC() {
    return 5.d;
  }
}