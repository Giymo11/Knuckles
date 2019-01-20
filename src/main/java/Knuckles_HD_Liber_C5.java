
import at.pwd.boardgame.game.mancala.MancalaGame;
import at.pwd.boardgame.game.mancala.agent.MancalaAgentAction;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

@SuppressWarnings("Duplicates")
public class Knuckles_HD_Liber_C5 extends Knuckles_HeuristicDefault {
  private double C_p1 = 5.;
  private double C_p0 = 5;

  private String filename = "liber.csv";

  private HashMap<Integer, String> openingP0 = new HashMap<>();
  private HashMap<Integer, String> openingP1 = new HashMap<>();

  public Knuckles_HD_Liber_C5() {
    super();
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

  @Override
  public MancalaAgentAction doTurn(int computationTime, MancalaGame game) {
    int player = game.getState().getCurrentPlayer();
    long start = System.nanoTime();
    // TODO: check if we should do that as P2 as well
    String action = player == 0 ? openingP0.get(new KnucklesGameState(game.getState()).hashCode()) : null;
    int dur = (int) (System.nanoTime() - start);
    System.out.println("Lookup time: " + dur);
    if(action != null) {
      return new MancalaAgentAction(action);
    }
    return super.doTurn(computationTime, game);
  }

  @Override
  protected MancalaAgentAction doTurnMCTS(int computationTime, MancalaGame game) {
    C = game.getState().getCurrentPlayer() == 0 ? C_p0 : C_p1;
    return super.doTurnMCTS(computationTime, game);
  }

  @Override
  public String toString() {
    return super.toString() + " Cswitch";
  }
}
