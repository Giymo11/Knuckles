
import at.pwd.boardgame.game.base.WinState;
import at.pwd.boardgame.game.mancala.MancalaGame;
import at.pwd.boardgame.game.mancala.MancalaState;
import at.pwd.boardgame.game.mancala.agent.MancalaAgent;
import at.pwd.boardgame.game.mancala.agent.MancalaAgentAction;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Knuckles in its final form
 */
public class Knuckles implements MancalaAgent {
  private Random r = new Random();
  private MancalaState originalState;

  /**
   * exploration parameter
   */
  protected double C = 2.5;
  /**
   * threshold to switch to alphabeta for endgame
   */
  protected int endgame = 24;

  /**
   * to not look up the opening book unneccessarily
   */
  private int turn = 0;

  /**
   * alphabeta agent for endgame
   */
  private MancalaAlphaBetaAgent alphaBetaAgent = new MancalaAlphaBetaAgent();

  /**
   * filename of opening book
   */
  private String filename = "/liber.csv";

  /**
   * the opening book for player 1
   */
  private HashMap<Integer, String> openingP0 = new HashMap<>();
  /**
   * the opening book as player 2
   */
  private HashMap<Integer, String> openingP1 = new HashMap<>();

  public Knuckles() {
    // reads the opening book
    try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(filename)))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] parts = line.split("\t");
        int playerId = Integer.parseInt(parts[0]);
        Integer hash = Integer.parseInt(parts[1]);
        String action = parts[2];
        if (playerId == 0) {
          openingP0.put(hash, action);
        } else {
          openingP1.put(hash, action);
        }
      }

      System.out.println("Loaded opening book entries: " + (openingP0.size() + openingP1.size()));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // taken from framework example implementation
  private class MCTSTree {
    private int visitCount;
    private long reward;

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
      //Since the reward can now also be negative, we have to differentiate between a minimizing and maximizing node
      if (this.game.getState().getCurrentPlayer() == originalState.getCurrentPlayer()) {
        for (MCTSTree m : children) {
          double wC = (double) m.reward;
          double vC = (double) m.visitCount;
          double currentValue = wC / vC + C * Math.sqrt(2 * Math.log(visitCount) / vC);


          if (best == null || currentValue > value) {
            value = currentValue;
            best = m;
          }
        }
      } else {
        for (MCTSTree m : children) {
          double wC = (double) m.reward;
          double vC = (double) m.visitCount;
          double currentValue = wC / vC - C * Math.sqrt(2 * Math.log(visitCount) / vC);


          if (best == null || currentValue < value) {
            value = currentValue;
            best = m;
          }
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
    int player = game.getState().getCurrentPlayer();
    ++turn;
    if (turn <= 2) { // if still in opening book
      // get play for current state ID
      String action = player == 0 ? openingP0.get(new KnucklesGameState(game.getState()).hashCode()) : openingP1.get(new KnucklesGameState(game.getState()).hashCode());
      System.out.println("Opening book says " + action);
      if (action != null) {
        return new MancalaAgentAction(action);
      }
    }
    // we found that playing with alphabeta in endgame as defender (player 2) is advantageous
    endgame = player == 0 ? 0 : 24;
    if (isNearTheEnd(game)) {
      return alphaBetaAgent.doTurn(computationTime, game);
    } else {
      // we found that playing with higher exploration as defender (player 2) is advantageous
      C = player == 0 ? 2.5 : 5.;
      return doTurnMCTS(computationTime, game);
    }
  }

  // taken from framework example documentation
  protected MancalaAgentAction doTurnMCTS(int computationTime, MancalaGame game) {
    long start = System.currentTimeMillis();
    this.originalState = game.getState();

    MCTSTree root = new MCTSTree(game);

    // grace period
    while ((System.currentTimeMillis() - start) < (computationTime * 1000 - 100)) {
      MCTSTree best = treePolicy(root);
      long reward = defaultPolicy(best.game);
      backup(best, reward);
    }

    MCTSTree selected = root.getBestNode();
    System.out.println("Selected action " + selected.reward + " / " + selected.visitCount);
    return new MancalaAgentAction(selected.action);
  }

  /**
   * increases visitcount and reward
   *
   * @param current
   * @param reward
   */
  private void backup(MCTSTree current, long reward) {
    while (current != null) {
      current.visitCount++;
      current.reward += reward;
      current = current.parent;
    }
  }

  // taken from framework example implementation
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

  // fixed from framework example implementation
  private MCTSTree expand(MCTSTree best) {
    List<String> legalMoves = best.game.getSelectableSlots();

    //remove already expanded moves
    for (MCTSTree move : best.children)
      legalMoves.remove(move.action);

    return best.move(legalMoves.get(r.nextInt(legalMoves.size())));
  }

  /**
   * uses the heuristic of alphabeta. we tried different ones, but this one performed best.
   * improvement over random playout default policy
   *
   * @param game
   * @return
   */
  private long defaultPolicy(MancalaGame game) {
    int currentPlayer = originalState.getCurrentPlayer();
    return DefaultPolicies.heuristic(game, currentPlayer);
  }

  @Override
  public String toString() {
    return "Knuckles";
  }

  /**
   * Checks wheter we are near the end of the game, by counting the remaining stones and comparing it to a dynamic value
   *
   * @param g The game to check
   * @return wheter the game is near the end or not
   */
  private boolean isNearTheEnd(MancalaGame g) {
    String p1Depot = g.getBoard().getDepotOfPlayer(0);
    String p2Depot = g.getBoard().getDepotOfPlayer(1);

    int p1DepotStones = g.getState().stonesIn(p1Depot);
    int p2DepotStones = g.getState().stonesIn(p2Depot);

    int stonesInPlay = 72 - (p1DepotStones + p2DepotStones);

    return stonesInPlay <= endgame; //if we have less stones than a certain value, we are near the end
  }
}
