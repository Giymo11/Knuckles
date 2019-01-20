
import at.pwd.boardgame.game.base.WinState;
import at.pwd.boardgame.game.mancala.MancalaGame;
import at.pwd.boardgame.game.mancala.MancalaState;
import at.pwd.boardgame.game.mancala.agent.MancalaAgent;
import at.pwd.boardgame.game.mancala.agent.MancalaAgentAction;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Knuckles WithOut Opening Book
 */
@SuppressWarnings("Duplicates")
public class Knuckles_Liber implements MancalaAgent {
  private Random r = new Random();
  private MancalaState originalState;
  //Increase C to force more exploration, else the search tree might not find certain winning states
  //TODO: probably needs tweaking
  protected double getC() {
    return 2.5;
  }

  private static final int ENDGAME = 24; //Test to find the best value for this

  private MancalaAlphaBetaAgent alphaBetaAgent = new MancalaAlphaBetaAgent();

  private String filename = "liber.csv";

  private HashMap<Integer, String> openingP0 = new HashMap<>();
  private HashMap<Integer, String> openingP1 = new HashMap<>();

  public Knuckles_Liber() {
    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] parts = line.split("\t");
        int playerId = Integer.parseInt(parts[0]);
        Integer hash = Integer.parseInt(parts[1]);
        String action = parts[2];
        if(playerId == 0) {
          openingP0.put(hash, action);
        } else {
          openingP1.put(hash, action);
        }
      }

      System.out.println("Loaded " + (openingP0.size() + openingP1.size()));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

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
      if(this.game.getState().getCurrentPlayer() == originalState.getCurrentPlayer()) {
        for (MCTSTree m : children) {
          double wC = (double) m.reward;
          double vC = (double) m.visitCount;
          double currentValue = wC / vC + getC() * Math.sqrt(2 * Math.log(visitCount) / vC);


          if (best == null || currentValue > value) {
            value = currentValue;
            best = m;
          }
        }
      }
      else {
        for (MCTSTree m : children) {
          double wC = (double) m.reward;
          double vC = (double) m.visitCount;
          double currentValue = wC / vC - getC() * Math.sqrt(2 * Math.log(visitCount) / vC);


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
    long start = System.nanoTime();
    String action = player == 0 ? openingP0.get(new KnucklesGameState(game.getState()).hashCode()) : openingP1.get(new KnucklesGameState(game.getState()).hashCode());
    int dur = (int) (System.nanoTime() - start);
    System.out.println("Lookup time: " + dur);
    if(action != null) {
      return new MancalaAgentAction(action);
    }
    if(isNearTheEnd(game)) {
      return alphaBetaAgent.doTurn(computationTime - (dur/1000 + 2), game);
    }
    else {
      return doTurnMCTS(computationTime - (dur/1000 + 2), game);
    }
  }


  private MancalaAgentAction doTurnMCTS(int computationTime, MancalaGame game) {
    long start = System.currentTimeMillis();
    this.originalState = game.getState();

    MCTSTree root = new MCTSTree(game);

    while ((System.currentTimeMillis() - start) < (computationTime*1000 - 100)) {
      MCTSTree best = treePolicy(root);
      long reward = defaultPolicy(best.game);
      backup(best, reward);
    }

    MCTSTree selected = root.getBestNode();
    System.out.println("Selected action " + selected.reward + " / " + selected.visitCount);
    return new MancalaAgentAction(selected.action);
  }

  private void backup(MCTSTree current, long reward) {

    while (current != null) {
      // always increase visit count
      current.visitCount++;

      // add up the reward
      current.reward += reward;

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

  private long defaultPolicy(MancalaGame game) {
    return heuristic(game);
  }

  @Override
  public String toString() {
    return "Knuckles Liber";
  }

  private long heuristic(MancalaGame node) {
    int currentPlayer = originalState.getCurrentPlayer();
    String ownDepot = node.getBoard().getDepotOfPlayer(currentPlayer);
    String enemyDepot = node.getBoard().getDepotOfPlayer(1 - currentPlayer);
    return node.getState().stonesIn(ownDepot) - node.getState().stonesIn(enemyDepot);
  }

  /**
   * Checks wheter we are near the end of the game, by counting the remaining stones and comparing it to a preset
   * constant.
   * @param g The game to check
   * @return wheter the game is near the end or not
   */
  private boolean isNearTheEnd(MancalaGame g) {
    String p1Depot = g.getBoard().getDepotOfPlayer(0);
    String p2Depot = g.getBoard().getDepotOfPlayer(1);

    int p1DepotStones = g.getState().stonesIn(p1Depot);
    int p2DepotStones = g.getState().stonesIn(p2Depot);

    int stonesInPlay = 72  - (p1DepotStones + p2DepotStones);

    return stonesInPlay <= ENDGAME; //if we have less stones than a certain value, we are near the end
  }
}
