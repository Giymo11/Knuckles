import at.pwd.boardgame.game.mancala.MancalaGame;
import at.pwd.boardgame.game.mancala.agent.MancalaAgentAction;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;


public class Liber extends Roadrunner {

  Set<KnucklesGameState> openingStatesForP0 = new HashSet<>();
  Set<KnucklesGameState> openingStatesForP1 = new HashSet<>();

  public Liber(int thinkingTime, int workerCount) {
    super(thinkingTime, workerCount);
  }

  String filename = "wotomato.csv";


  @SuppressWarnings("Duplicates")
  public static void main(String[] args) {
    int thinkingTime = 10;
    int ab_depth = 13;
    int ob_depth = 4;
    int workerCount = 12;
    Liber runner = new Liber(thinkingTime, workerCount);
    try {
      runner.loadBoard();
      //runner.loadAgents(args);
      runner.init(ob_depth);
      runner.run(ab_depth);
    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("We done here.");
  }

  private void addStatesForGame(MancalaGame origGame, int ob_depth) {
    if (ob_depth == 0)
      return;

    int player = origGame.getState().getCurrentPlayer();

    List<String> legalMoves = origGame.getSelectableSlots();
    for (String move : legalMoves) {
      MancalaGame game = new MancalaGame(origGame);
      boolean mayPlayAgain = game.selectSlot(move);
      if (player == 0) {
        if (mayPlayAgain) {
          openingStatesForP0.add(new KnucklesGameState(game.getState()));
        } else {
          game.nextPlayer();
          openingStatesForP1.add(new KnucklesGameState(game.getState()));
        }
      } else {
        if (mayPlayAgain) {
          openingStatesForP1.add(new KnucklesGameState(game.getState()));
        } else {
          game.nextPlayer();
          openingStatesForP0.add(new KnucklesGameState(game.getState()));
        }
      }
      addStatesForGame(game, ob_depth - 1);
    }
  }

  public void init(int ob_depth) {
    super.init();

    MancalaGame game = new MancalaGame(defaultGame);

    openingStatesForP0.add(new KnucklesGameState(game.getState()));

    // TODO: go deeper
    addStatesForGame(game, ob_depth);

  }

  @Override
  public List<Future> run(int repetitions) throws InterruptedException {
    List<Future> futures = new LinkedList<>();

    MancalaAlphaBetaAgent.DEPTH = repetitions;

    filename = System.currentTimeMillis() + "_" + repetitions + ".csv";

    System.out.println("Filename: " + filename);
    System.out.println("entries: " + (openingStatesForP0.size() + openingStatesForP1.size()));

    submit(futures, openingStatesForP0, 0);
    submit(futures, openingStatesForP1, 1);

    System.out.println("submitted");

    return futures;
  }

  private void submit(List<Future> futures, Set<KnucklesGameState> openingStatesForP2, int playerId) {
    for (KnucklesGameState opening : openingStatesForP2) {
      MancalaGame game = new MancalaGame(opening.state, board);

      futures.add(executor.submit(() -> {
        MancalaAlphaBetaAgent alphaBetaAgent = new MancalaAlphaBetaAgent();
        MancalaAgentAction bestAction = alphaBetaAgent.doTurn(100, game);
        MancalaGame newGame = new MancalaGame(game);
        bestAction.applyAction(newGame);
        KnucklesGameState newState = new KnucklesGameState(newGame.getState());
        System.out.println("\nCurrent player: " + game.getState().getCurrentPlayer() + " should be " + playerId);
        //System.out.println("Current state: \n" + opening.toString() + ", best action: " + alphaBetaAgent.currentBest + " for new state: \n" + newState);

        try {
          BufferedWriter writer = new BufferedWriter(new FileWriter(filename + "_" + Thread.currentThread().getId(), true));

          writer.write(playerId + "\t" + opening.hashCode() + "\t" +  alphaBetaAgent.currentBest + "\n");

          writer.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }));
    }
  }
}
