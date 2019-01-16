import at.pwd.boardgame.game.agent.Agent;
import at.pwd.boardgame.game.agent.AgentAction;
import at.pwd.boardgame.game.base.WinState;
import at.pwd.boardgame.game.mancala.MancalaBoard;
import at.pwd.boardgame.game.mancala.MancalaGame;
import at.pwd.boardgame.game.mancala.MancalaState;
import at.pwd.boardgame.game.mancala.agent.MancalaAgent;
import at.pwd.boardgame.game.mancala.agent.MancalaAgentAction;
import at.pwd.boardgame.services.AgentService;
import at.pwd.boardgame.services.GameFactory;
import at.pwd.boardgame.services.XSLTService;
import org.simpleframework.xml.core.Persister;

import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;


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
    AgentAction<MancalaGame> action = agent.doTurn(time, new MancalaGame(game));

    AgentAction.NextAction nextPlayer = action.applyAction(game);

    WinState winState = game.checkIfPlayerWins();
    if (winState.getState() != WinState.States.NOBODY) {
      gameEnded(winState, game);
    } else {
      if (nextPlayer == AgentAction.NextAction.NEXT_PLAYER) {
        currentAgent = (currentAgent + 1) % 2;
      }
      turn++;
      nextTurn(game);
    }
  }

  public void gameEnded(WinState winState, MancalaGame game) {
    String str = "Hello";
    BufferedWriter writer = null;
    try {
      writer = new BufferedWriter(new FileWriter(filename, true));

      if (winState.getState() == WinState.States.SOMEONE) {
        int depotStones = game.getState().stonesIn(game.getBoard().getDepotOfPlayer(winState.getPlayerId()));
        writer.write(agents.get(winState.getPlayerId()) + " won after " + turn + " turns with " + depotStones + " stones\n");
      }

      if (winState.getState() == WinState.States.MULTIPLE) {
        int depotStones = game.getState().stonesIn(game.getBoard().getDepotOfPlayer(0));
        writer.write("Draw after " + turn + " turns with " + depotStones + " stones\n");
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

// from https://stackoverflow.com/questions/4521983/java-executorservice-that-blocks-on-submission-after-a-certain-queue-size/4522411
  public class LimitedQueue<E> extends LinkedBlockingQueue<E> {
    public LimitedQueue(int maxSize) {
      super(maxSize);
    }

    @Override
    public boolean offer(E e) {
      // turn offer() and add() into a blocking calls (unless interrupted)
      try {
        put(e);
        return true;
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
      }
      return false;
    }
  }

  public static void main(String[] args) throws InterruptedException {
    int thinkingTime = 10;
    int repetitions = 100;
    int workerCount = 4;
    Roadrunner runner = new Roadrunner(thinkingTime);

    MancalaBoard board = runner.loadBoard();

    List<Agent> all_agents = runner.loadAgents(args);

    Agent toTest = all_agents.get(0);
    List<Agent> agents = all_agents.subList(1, all_agents.size());

    MancalaGame defaultGame = new MancalaGame(null, board);
    System.out.println(defaultGame.nextPlayer());

    MancalaState defaultState = defaultGame.getState();

    ThreadPoolExecutor executor = new ThreadPoolExecutor(workerCount, workerCount, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(8));
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

    String prefix = Long.toString(System.currentTimeMillis());

    for (Agent agent : agents) {
      for (int i = 0; i < repetitions; ++i) {
        Thread.sleep(50); // to not have them all write at the same time
        executor.submit(() -> {
          String filename = prefix + "_" + toTest.toString() + "_first_VS_" + agent.toString() + "_last.txt";
          KnucklesGame myGame = new KnucklesGame(thinkingTime, Arrays.asList(toTest, agent), filename);
          myGame.nextTurn(new MancalaGame(defaultGame));
          filename = prefix + "_" + agent.toString() + "_first_VS_" + toTest.toString() + "_last.txt";
          myGame = new KnucklesGame(thinkingTime, Arrays.asList(agent, toTest), filename);
          myGame.nextTurn(new MancalaGame(defaultGame));
        });
        System.out.println("submitted no " + i + " vs " + agent.toString());
      }
    }

    executor.awaitTermination(12, TimeUnit.HOURS);
    System.out.println("We done here.");
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
    final String GAME_BOARD = "normal_mancala_board.xml";
    MancalaBoard board = null;
    try {
      System.out.println(getClass().getResourceAsStream(GAME_BOARD));
      board = new Persister().read(MancalaBoard.class, new File(GAME_BOARD));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return board;
  }


}
