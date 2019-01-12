
import at.pwd.boardgame.game.base.WinState;
import at.pwd.boardgame.game.mancala.MancalaGame;
import at.pwd.boardgame.game.mancala.MancalaState;
import at.pwd.boardgame.game.mancala.agent.MancalaAgent;
import at.pwd.boardgame.game.mancala.agent.MancalaAgentAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SuppressWarnings("Duplicates")
public class Knuckles_HeuristicDefault implements MancalaAgent {
  private Random r = new Random();
  private MancalaState originalState;
  //Increase C to force more exploration, else the search tree might not find certain winning states
  //TODO: probably needs tweaking
  private static final double C = 2.5;

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
      Integer reward = defaultPolicy(best.game);
      backup(best, reward);
    }

    MCTSTree selected = root.getBestNode();
    System.out.println("Selected action " + selected.winCount + " / " + selected.visitCount);
    return new MancalaAgentAction(selected.action);
  }

  private void backup(MCTSTree current, Integer reward) {

    while (current != null) {
      // always increase visit count
      current.visitCount++;

      // add up the reward
      current.winCount += reward;

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

    //remove already expanded moves
    for(MCTSTree move : best.children)
      legalMoves.remove(move.action);

    return best.move(legalMoves.get(r.nextInt(legalMoves.size())));
  }

  private Integer defaultPolicy(MancalaGame game) {
    return heuristic(game);
  }

  @Override
  public String toString() {
    return "Knuckles HeuristicDefault";
  }

  private int heuristic(MancalaGame node) {
    int currentPlayer = originalState.getCurrentPlayer();
    String ownDepot = node.getBoard().getDepotOfPlayer(currentPlayer);
    String enemyDepot = node.getBoard().getDepotOfPlayer(1 - currentPlayer);
    return node.getState().stonesIn(ownDepot) - node.getState().stonesIn(enemyDepot);
  }
}
