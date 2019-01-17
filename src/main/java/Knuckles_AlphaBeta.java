import at.pwd.boardgame.game.base.WinState;
import at.pwd.boardgame.game.mancala.MancalaGame;
import at.pwd.boardgame.game.mancala.agent.MancalaAgent;
import at.pwd.boardgame.game.mancala.agent.MancalaAgentAction;

import java.util.List;

/**
 * Created by rfischer on 18/04/2017.
 */
public class Knuckles_AlphaBeta extends MancalaAlphaBetaAgent {
    @Override
    public int heuristic(MancalaGame node, int currentPlayer) {
        return DefaultPolicies.heuristicBoard(node, currentPlayer);
    }

    @Override
    public String toString() {
        return "Knuckles AlphaBeta";
    }
}
