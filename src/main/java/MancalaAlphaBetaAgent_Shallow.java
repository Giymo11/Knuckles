import at.pwd.boardgame.game.base.WinState;
import at.pwd.boardgame.game.mancala.MancalaGame;
import at.pwd.boardgame.game.mancala.agent.MancalaAgent;
import at.pwd.boardgame.game.mancala.agent.MancalaAgentAction;

import java.util.List;

/**
 * Created by rfischer on 18/04/2017.
 */
public class MancalaAlphaBetaAgent_Shallow implements MancalaAgent {

    public static int DEPTH = 1;
    private int currentPlayer;
    public String currentBest;

    @Override
    public MancalaAgentAction doTurn(int computationTime, MancalaGame initialGame) {
        currentPlayer = initialGame.getState().getCurrentPlayer();
        currentBest = null;

        alphabeta(initialGame, DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, true);

        return new MancalaAgentAction(currentBest);
    }

    private int alphabeta(MancalaGame node, int depth, int alpha, int beta, boolean maximizingPlayer) {
        if (depth == 0 || node.checkIfPlayerWins().getState() != WinState.States.NOBODY) {
            return heuristic(node, currentPlayer);
        }

        List<String> legalMoves = node.getSelectableSlots();
        for (String move : legalMoves) {
            MancalaGame newGame = new MancalaGame(node);
            boolean moveAgain = newGame.selectSlot(move);
            if (!moveAgain) {
                newGame.nextPlayer();
            }

            if (maximizingPlayer) {
                int oldAlpha = alpha;
                alpha = Math.max(alpha, alphabeta(newGame, depth - 1, alpha, beta, moveAgain));
                if (depth == DEPTH && (oldAlpha < alpha || currentBest == null)) {
                    currentBest = move;
                }
            } else {
                beta = Math.min(beta, alphabeta(newGame, depth - 1, alpha, beta, !moveAgain));
            }

            if (beta <= alpha) {
                break;
            }
        }
        return maximizingPlayer ? alpha : beta;
    }

    public int heuristic(MancalaGame node, int currentPlayer) {
        return DefaultPolicies.heuristic(node, currentPlayer);
    }


    @Override
    public String toString() {
        return "Alpha Beta Pruning";
    }
}
