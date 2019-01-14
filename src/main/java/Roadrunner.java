import at.pwd.boardgame.game.agent.Agent;
import at.pwd.boardgame.game.agent.AgentAction;
import at.pwd.boardgame.game.base.WinState;
import at.pwd.boardgame.game.mancala.MancalaBoard;
import at.pwd.boardgame.game.mancala.MancalaGame;
import at.pwd.boardgame.game.mancala.MancalaState;
import at.pwd.boardgame.services.AgentService;
import at.pwd.boardgame.services.GameFactory;
import at.pwd.boardgame.services.XSLTService;
import org.simpleframework.xml.core.Persister;

import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


class KnucklesGame {
  public int turn = 0;
  public int currentAgent = 0;

  public int time = 10;
  List<Agent> agents;
  public String filename;

  public KnucklesGame(int time, List<Agent> agents, String filename) {
    this.time = time;
    this.agents = agents;
    this.filename = filename;
  }

  public void nextTurn(MancalaGame game) {
    System.out.println(filename + " - turn " + turn);

    Agent agent = agents.get(currentAgent);
    AgentAction<MancalaGame> action = agent.doTurn(time, new MancalaGame(game));
    AgentAction.NextAction nextPlayer = action.applyAction(game);

    WinState winState = game.checkIfPlayerWins();
    if(winState.getState() != WinState.States.NOBODY)
      gameEnded(winState);

    if(nextPlayer == AgentAction.NextAction.NEXT_PLAYER) {
      currentAgent = (currentAgent + 1) % 2;
    }
    turn++;
    nextTurn(game);
  }

  public void gameEnded(WinState winState) {
    String str = "Hello";
    BufferedWriter writer = null;
    try {
      writer = new BufferedWriter(new FileWriter(filename, true));

      if(winState.getState() == WinState.States.SOMEONE) {
        writer.write(winState.getPlayerId() + " won after " + turn + " turns");
      }

      if(winState.getState() == WinState.States.MULTIPLE) {
        writer.write("Draw after " + turn + " turns");
      }

      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}

public class Roadrunner {
  int time = 10;

  public Roadrunner(int time) {
    this.time = time;
  }

  private static int parFactor = 8;

  public static void main(String[] args) {
    Roadrunner runner = new Roadrunner(10);

    MancalaBoard board = runner.loadBoard();
    List<Agent> all_agents = runner.loadAgents(args);

    Agent toTest = all_agents.get(0);
    List<Agent> agents = all_agents.subList(1, all_agents.size());

    MancalaGame defaultGame = new MancalaGame(null, board);
    MancalaState defaultState = defaultGame.getState();

    ExecutorService executor = Executors.newFixedThreadPool(parFactor);

    String prefix = Long.toString(System.currentTimeMillis());

    for(Agent agent : agents) {
      int currentTurn = 0;
      for(int i = 0; i < 10; ++i) {
        String filename = prefix + "_" + toTest.toString() + "_first_" + agent.toString() + "_last.txt";
        KnucklesGame myGame = new KnucklesGame(10, Arrays.asList(toTest, agent), filename);
        myGame.nextTurn(new MancalaGame(defaultGame));
        filename = prefix + "_" + agent.toString() + "_first_" + toTest.toString() + "_last.txt";
        myGame = new KnucklesGame(10, Arrays.asList(agent, toTest), filename);
        myGame.nextTurn(new MancalaGame(defaultGame));
      }
    }
  }

  public List<Agent> loadAgents(String[] classnames) {
    for (String name : classnames) {
      Agent agent = null;
      try {
        agent = (Agent) Class.forName(name).newInstance();
      } catch (IllegalAccessException | ClassNotFoundException | InstantiationException e) {
        e.printStackTrace();
      }
      if (agent != null) {
        AgentService.getInstance().register(agent);
      }
    }

    return AgentService.getInstance().getAgents();
  }


  public MancalaBoard loadBoard() {
    final String GAME_BOARD= "normal_mancala_board.xml";
    MancalaBoard board = null;
    try {
      System.out.println(getClass().getResourceAsStream(GAME_BOARD));
      board  = new Persister().read(MancalaBoard.class, new File(GAME_BOARD));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return board;
  }





}
