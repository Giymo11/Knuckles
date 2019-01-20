import at.pwd.boardgame.game.mancala.MancalaGame;
import at.pwd.boardgame.game.mancala.agent.MancalaAgentAction;

/**
 * Final form
 */
@SuppressWarnings("Duplicates")
public class Knuckles_HD_Cswitch extends Knuckles_HeuristicDefault {
  private double C_p1 = 5.;
  private double C_p0 = 2.5;

  @Override
  protected MancalaAgentAction doTurnMCTS(int computationTime, MancalaGame game) {
    C = game.getState().getCurrentPlayer() == 0 ? C_p0 : C_p1;
    return super.doTurnMCTS(computationTime, game);
  }

  @Override
  public String toString() {
    return "Knuckles HD Cswitch";
  }
}
