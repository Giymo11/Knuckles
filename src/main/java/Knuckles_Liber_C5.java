
import at.pwd.boardgame.game.mancala.MancalaGame;
import at.pwd.boardgame.game.mancala.agent.MancalaAgentAction;

/**
 * Knuckles WithOut Opening Book
 */
@SuppressWarnings("Duplicates")
public class Knuckles_Liber_C5 extends Knuckles_Liber {
  //Increase C to force more exploration, else the search tree might not find certain winning states

  @Override
  protected MancalaAgentAction doTurnMCTS(int computationTime, MancalaGame game) {
    C = 5.0;
    return super.doTurnMCTS(computationTime, game);
  }

  @Override
  public String toString() {
    return super.toString() + " C5";
  }

}
