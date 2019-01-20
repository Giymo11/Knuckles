import at.pwd.boardgame.game.agent.Agent;
import at.pwd.boardgame.game.agent.AgentAction;
import at.pwd.boardgame.game.base.WinState;
import at.pwd.boardgame.game.mancala.MancalaGame;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

class KnucklesGame {
  public int turn = 0;
  public int currentAgent = 0;

  public int time;
  List<Agent> agents;
  public String filename;

  public KnucklesGame(int time, List<Agent> agents, String filename) {
    this.time = time;
    this.agents = agents;
    this.filename = filename;
  }

  public void nextTurn(MancalaGame game) {
    Agent agent = agents.get(currentAgent);
    System.out.println("turn " + turn + " by agent " + agent + ", Player should be " + game.getState().getCurrentPlayer() + " but was " + currentAgent);

    AgentAction<MancalaGame> action = agent.doTurn(time, new MancalaGame(game));

    try {
      AgentAction.NextAction nextPlayer = action.applyAction(game);

      WinState winState = game.checkIfPlayerWins();
      if (winState.getState() != WinState.States.NOBODY) {
        gameEnded(winState, game);
      } else {
        if (nextPlayer == AgentAction.NextAction.NEXT_PLAYER) {
          currentAgent = (currentAgent + 1) % 2;
          game.nextPlayer();
        }
        turn++;
        nextTurn(game);
      }

    } catch (RuntimeException rex) {
      System.out.println("agent: " + agent);
      rex.printStackTrace();
    }
  }

  public void gameEnded(WinState winState, MancalaGame game) {
    String str = "Hello";
    BufferedWriter writer = null;
    try {
      writer = new BufferedWriter(new FileWriter(filename, true));

      if (winState.getState() == WinState.States.SOMEONE) {
        int depotStones = game.getState().stonesIn(game.getBoard().getDepotOfPlayer(winState.getPlayerId()));
        String player = Integer.toString(winState.getPlayerId() + 1);
        System.out.println(agents.get(winState.getPlayerId()) + " won after " + turn + " turns with " + depotStones + " stones, as player " + player + "\n");
        writer.write("true\t" + agents.get(winState.getPlayerId()) + "\t" + turn + "\t" + depotStones + "\t" + player + "\n");
      }

      if (winState.getState() == WinState.States.MULTIPLE) {
        int depotStones = game.getState().stonesIn(game.getBoard().getDepotOfPlayer(0));
        System.out.println("Draw after " + turn + " turns with " + depotStones + " stones\n");
        writer.write("\t\t" + turn + "\t" + depotStones + "\n");
      }

      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void writeHeader() {
    BufferedWriter writer = null;
    try {
      writer = new BufferedWriter(new FileWriter(filename, true));
      writer.write("hasWinner?\tWinner\tTurns\tStones\tStarting\n");
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}