import at.pwd.boardgame.game.base.WinState;
import at.pwd.boardgame.game.mancala.MancalaGame;
import at.pwd.boardgame.game.mancala.agent.MancalaAgentAction;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

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
