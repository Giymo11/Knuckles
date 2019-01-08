
import at.pwd.boardgame.game.base.WinState;
import at.pwd.boardgame.game.mancala.MancalaGame;
import at.pwd.boardgame.game.mancala.MancalaState;
import at.pwd.boardgame.game.mancala.agent.MancalaAgent;
import at.pwd.boardgame.game.mancala.agent.MancalaAgentAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SuppressWarnings("Duplicates")
public class Knuckles implements MancalaAgent {
  private Random r = new Random();
  private MancalaState originalState;
  private static final double C = 1.0f/Math.sqrt(2.0f);

  private class MCTSTree {
    private int visitCount;
    private int winCount;

    private MancalaGame game;
    private WinState winState;
    private MCTSTree parent;
    private List<MCTSTree> children;
    String action;

    MCTSTree(MancalaGame game) {
      this.game = game;
      this.children = new ArrayList<>();
      this.winState = game.checkIfPlayerWins();
    }

    boolean isNonTerminal() {
      return winState.getState() == WinState.States.NOBODY;
    }

    MCTSTree getBestNode() {
      MCTSTree best = null;
      double value = 0;
      for (MCTSTree m : children) {
        double wC = (double)m.winCount;
        double vC = (double)m.visitCount;
        double currentValue =  wC/vC + C*Math.sqrt(2*Math.log(visitCount) / vC);


        if (best == null || currentValue > value) {
          value = currentValue;
          best = m;
        }
      }

      return best;
    }

    boolean isFullyExpanded() {
      return children.size() == game.getSelectableSlots().size();
    }

    MCTSTree move(String action) {
      MancalaGame newGame = new MancalaGame(this.game);
      if (!newGame.selectSlot(action)) {
        newGame.nextPlayer();
      }

      MCTSTree tree = new MCTSTree(newGame);
      tree.action = action;
      tree.parent = this;

      this.children.add(tree);

      return tree;
    }
  }


  @Override
  public MancalaAgentAction doTurn(int computationTime, MancalaGame game) {
    long start = System.currentTimeMillis();
    this.originalState = game.getState();

    MCTSTree root = new MCTSTree(game);

    while ((System.currentTimeMillis() - start) < (computationTime*1000 - 100)) {
      MCTSTree best = treePolicy(root);
      WinState winning = defaultPolicy(best.game);
      backup(best, winning);
    }

    MCTSTree selected = root.getBestNode();
    System.out.println("Selected action " + selected.winCount + " / " + selected.visitCount);
    return new MancalaAgentAction(selected.action);
  }

  private void backup(MCTSTree current, WinState winState) {
    boolean hasWon = winState.getState() == WinState.States.SOMEONE && winState.getPlayerId() == originalState.getCurrentPlayer();

    while (current != null) {
      // always increase visit count
      current.visitCount++;

      // if it ended in a win => increase the win count
      current.winCount += hasWon ? 1 : 0;

      current = current.parent;
    }
  }

  private MCTSTree treePolicy(MCTSTree current) {
    while (current.isNonTerminal()) {
      if (!current.isFullyExpanded()) {
        return expand(current);
      } else {
        current = current.getBestNode();
      }
    }
    return current;
  }

  private MCTSTree expand(MCTSTree best) {
    List<String> legalMoves = best.game.getSelectableSlots();
    return best.move(legalMoves.get(r.nextInt(legalMoves.size())));
  }

  private WinState defaultPolicy(MancalaGame game) {
    return DefaultPolicies.random(game);
  }

  @Override
  public String toString() {
    return "Knuckles";
  }
}
