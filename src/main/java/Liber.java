import at.pwd.boardgame.game.mancala.MancalaGame;
import at.pwd.boardgame.game.mancala.MancalaState;
import at.pwd.boardgame.game.mancala.agent.MancalaAgentAction;

import java.util.*;
import java.util.concurrent.Future;


public class Liber extends Roadrunner {

  Set<KnucklesGameState> openingStatesForP0 = new HashSet<>();
  Set<KnucklesGameState> openingStatesForP1 = new HashSet<>();

  public Liber(int thinkingTime, int workerCount) {
    super(thinkingTime, workerCount);
  }


  @SuppressWarnings("Duplicates")
  public static void main(String[] args) {
    int thinkingTime = 10;
    int repetitions = 10;
    int workerCount = 6;
    Liber runner = new Liber(thinkingTime, workerCount);
    try {
      runner.loadBoard();
      //runner.loadAgents(args);
      runner.init();
      runner.run(repetitions);
    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("We done here.");
  }

  @Override
  public void init() {
    super.init();

    MancalaGame game = new MancalaGame(defaultGame);

    openingStatesForP0.add(new KnucklesGameState(game.getState()));

    // TODO: go deeper
    List<String> legalMoves = game.getSelectableSlots();
    for (String move : legalMoves) {
      game = new MancalaGame(defaultGame);
      boolean mayPlayAgain = game.selectSlot(move);
      if (mayPlayAgain) {
        openingStatesForP0.add(new KnucklesGameState(game.getState()));
      } else {
        game.nextPlayer();
        openingStatesForP1.add(new KnucklesGameState(game.getState()));
      }
    }
  }

  @Override
  public List<Future> run(int repetitions) throws InterruptedException {
    List<Future> futures = new LinkedList<>();

    MancalaAlphaBetaAgent.DEPTH = 15;

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
        System.out.println("Current state: \n" + opening.toString() + ", best action: " + alphaBetaAgent.currentBest + " for new state: \n" + newState);
      }));
    }
  }
}
