import at.pwd.boardgame.game.mancala.MancalaGame;
import at.pwd.boardgame.game.mancala.agent.MancalaAgentAction;

/**
 * Final form
 */
@SuppressWarnings("Duplicates")
public class Knuckles_HeuristicDefault_C5 extends Knuckles_HeuristicDefault_C3 {

  @Override
  protected double getC() {
    return 5.0;
  }

  @Override
  public MancalaAgentAction doTurn(int computationTime, MancalaGame game) {
    Knuckles_HeuristicDefault knuckles = new Knuckles_HeuristicDefault_C5();
    knuckles.C = 5.;
    return knuckles.doTurnMCTS(computationTime, game);
  }
}
