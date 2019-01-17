import at.pwd.boardgame.game.base.WinState;
import at.pwd.boardgame.game.mancala.MancalaGame;
import at.pwd.boardgame.game.mancala.agent.MancalaAgentAction;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("Duplicates")
public class DefaultPolicies {
  private static Random r = new Random();

  public static WinState random(MancalaGame game) {
    game = new MancalaGame(game); // copy original game
    WinState state = game.checkIfPlayerWins();

    while(state.getState() == WinState.States.NOBODY) {
      String play;
      do {
        List<String> legalMoves = game.getSelectableSlots();
        play = legalMoves.get(r.nextInt(legalMoves.size()));
      } while(game.selectSlot(play));
      game.nextPlayer();

      state = game.checkIfPlayerWins();
    }

    return state;
  }

  public static WinState greedy(MancalaGame game, int player) {
    MancalaGame g = new MancalaGame(game);
    double epsilon = 0.1;
    WinState state = g.checkIfPlayerWins();

    while(state.getState() == WinState.States.NOBODY) {
      String play;
      do {
        List<String> legalMoves = g.getSelectableSlots();
        play = legalMoves.get(r.nextInt(legalMoves.size())); //initialize, just in case
        int max = -72;
        for(String move: legalMoves) {
          MancalaGame copy = new MancalaGame(g);
          copy.selectSlot(move);
          //maximize heuristic value each move
          if(heuristic(copy, player) > max) {
            play = move;
            max = heuristic(copy, player);
          }
        }

        double chance = ThreadLocalRandom.current().nextDouble(1);
        //With a certain chance play a random move instead
        if(chance > epsilon) {
          play = legalMoves.get(r.nextInt(legalMoves.size()));
        }
      } while(g.selectSlot(play));
      g.nextPlayer();

      state = g.checkIfPlayerWins();
    }

    return state;
  }

  private static int heuristic(MancalaGame node, int currentPlayer) {
    String ownDepot = node.getBoard().getDepotOfPlayer(currentPlayer);
    String enemyDepot = node.getBoard().getDepotOfPlayer(1 - currentPlayer);
    return node.getState().stonesIn(ownDepot) - node.getState().stonesIn(enemyDepot);
  }


  public static WinState alwaysDoublePlay(MancalaGame game) {
    game = new MancalaGame(game); // copy original game
    WinState state = game.checkIfPlayerWins();

    while(state.getState() == WinState.States.NOBODY) {
      String play;
      do {
        List<String> legalMoves = game.getSelectableSlots();

        List<String> depotMoves = getDepotMoves(game, legalMoves);

        if(!depotMoves.isEmpty())
          play = depotMoves.get(r.nextInt(depotMoves.size()));
        else
          play = legalMoves.get(r.nextInt(legalMoves.size()));
      } while(game.selectSlot(play));
      game.nextPlayer();

      state = game.checkIfPlayerWins();
    }

    return state;
  }

  private static List<String> getDepotMoves(MancalaGame game, List<String> moves) {
    List<String> depotMoves = new LinkedList<>();

    for(String move: moves) {
      MancalaGame temp_game = new MancalaGame(game);
      boolean endsInOwnDepot = temp_game.selectSlot(move);
      if(endsInOwnDepot) {
        depotMoves.add(move);
      }
    }

    return depotMoves;
  }

  public static WinState preferentialDoublePlay(MancalaGame game, double percentage) {
    game = new MancalaGame(game); // copy original game
    WinState state = game.checkIfPlayerWins();

    while(state.getState() == WinState.States.NOBODY) {
      String play;
      do {
        List<String> legalMoves = game.getSelectableSlots();

        List<String> depotMoves = getDepotMoves(game, legalMoves);

        if(!depotMoves.isEmpty() && r.nextDouble() < percentage)
          play = depotMoves.get(r.nextInt(depotMoves.size()));
        else
          play = legalMoves.get(r.nextInt(legalMoves.size()));
      } while(game.selectSlot(play));
      game.nextPlayer();

      state = game.checkIfPlayerWins();
    }

    return state;
  }
}
