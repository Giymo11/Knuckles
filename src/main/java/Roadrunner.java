import at.pwd.boardgame.game.agent.Agent;
import at.pwd.boardgame.game.mancala.MancalaBoard;
import at.pwd.boardgame.game.mancala.MancalaGame;
import at.pwd.boardgame.services.AgentService;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class Roadrunner {
  protected int thinkingTime;
  protected int workerCount;

  protected Agent toTest;
  protected List<Agent> agents;

  protected MancalaBoard board;
  protected MancalaGame defaultGame;

  protected ThreadPoolExecutor executor;
  protected String prefix;

  public Roadrunner(int thinkingTime, int workerCount) {
    this.thinkingTime = thinkingTime;
    this.workerCount = workerCount;
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

  public void init() {
    defaultGame = new MancalaGame(null, board);
    defaultGame.nextPlayer();

    System.out.println("Agents: " + agents);

    //MancalaState defaultState = defaultGame.getState();

    executor = new ThreadPoolExecutor(workerCount, workerCount, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(workerCount));

    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

    prefix = Long.toString(System.currentTimeMillis());
  }

  public static void main(String[] args) {
    int thinkingTime = 10;
    int repetitions = 10;
    int workerCount = 6;
    Roadrunner runner = new Roadrunner(thinkingTime, workerCount);
    try {
      runner.loadBoard();
      runner.loadAgents(args);
      runner.init();
      runner.run(repetitions);
    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("We done here.");
  }

  public List<Future> run(int repetitions) throws InterruptedException {
    List<Future> futures = new LinkedList<>();
    for (int i = 0; i < repetitions; ++i) {
      for (Agent agent : agents) {
        Thread.sleep(50); // to not have them all write at the same time
        final int rep = i;
        futures.add(executor.submit(() -> {
          String filename = prefix + "_" + toTest.toString() + "_first_VS_" + agent.toString() + "_last.csv";
          KnucklesGame myGame = new KnucklesGame(thinkingTime, Arrays.asList(toTest, agent), filename);
          if (rep == 0)
            myGame.writeHeader();
          myGame.nextTurn(new MancalaGame(defaultGame));
          filename = prefix + "_" + agent.toString() + "_first_VS_" + toTest.toString() + "_last.csv";
          myGame = new KnucklesGame(thinkingTime, Arrays.asList(agent, toTest), filename);
          if (rep == 0)
            myGame.writeHeader();
          myGame.nextTurn(new MancalaGame(defaultGame));
          System.out.println("done rep " + rep + " for " + agent);
        }));
        System.out.println("submitted no " + i + " vs " + agent.toString());
      }
    }
    return futures;
  }

  public List<Agent> loadAgents(String[] classnames) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
    for (String name : classnames) {
      Agent agent = null;

      agent = (Agent) Class.forName(name).newInstance();

      if (agent != null)
        AgentService.getInstance().register(agent);
    }

    List<Agent> allAgents = AgentService.getInstance().getAgents();

    toTest = allAgents.get(0);
    agents = allAgents.subList(1, allAgents.size());

    return allAgents;
  }


  public MancalaBoard loadBoard() throws Exception {
    final String GAME_BOARD = "normal_mancala_board.xml";

    System.out.println(getClass().getResourceAsStream(GAME_BOARD));
    board = new Persister().read(MancalaBoard.class, new File(GAME_BOARD));

    return board;
  }


}
