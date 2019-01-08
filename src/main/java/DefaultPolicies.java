import at.pwd.boardgame.game.base.WinState;
import at.pwd.boardgame.game.mancala.MancalaGame;

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

  public static WinState alwaysChooseDoublePlay(MancalaGame game) {
    game = new MancalaGame(game); // copy original game
    WinState state = game.checkIfPlayerWins();

    while(state.getState() == WinState.States.NOBODY) {
      String play;
      do {
        List<String> legalMoves = game.getSelectableSlots();

        boolean endsInOwnDepot = false;
        String bestMove = legalMoves.get(0);

        for(String move: legalMoves) {
          MancalaGame temp_game = new MancalaGame(game);
          endsInOwnDepot = temp_game.selectSlot(move);
          if(endsInOwnDepot) {
            bestMove = move;
            break;
          }
        }
        if(endsInOwnDepot)
          play = bestMove;
        else
          play = legalMoves.get(r.nextInt(legalMoves.size()));
      } while(game.selectSlot(play));
      game.nextPlayer();

      state = game.checkIfPlayerWins();
    }

    return state;
  }
}
