
import at.pwd.boardgame.game.base.WinState;
import at.pwd.boardgame.game.mancala.MancalaGame;
import at.pwd.boardgame.game.mancala.MancalaState;
import at.pwd.boardgame.game.mancala.agent.MancalaAgent;
import at.pwd.boardgame.game.mancala.agent.MancalaAgentAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SuppressWarnings("Duplicates")
public class Knuckles_HeuristicDefault_Cswitch extends Knuckles_HeuristicDefault {
  private double C_p1 = 5.;
  private double C_p0 = 2.5;

  @Override
  protected MancalaAgentAction doTurnMCTS(int computationTime, MancalaGame game) {
    C = game.getState().getCurrentPlayer() == 0 ? C_p0 : C_p1;
    return super.doTurnMCTS(computationTime, game);
  }

  @Override
  public String toString() {
    return super.toString() + " Cswitch";
  }
}
