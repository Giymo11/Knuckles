import at.pwd.boardgame.game.mancala.MancalaState;

class KnucklesGameState {
  final MancalaState state;
  int hash = 0;

  KnucklesGameState(MancalaState stateToCopy) {
    state = (MancalaState) stateToCopy.copy();
    for (int i = 1; i <= 14; ++i) {
      hash ^= state.stonesIn(Integer.toString(i));
      hash *= 72;
    }
  }

  @Override
  public int hashCode() {
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    return obj.hashCode() == hashCode();
  }

  @Override
  public String toString() {
    String str = "";
    for (int i = 1; i <= 14; ++i) {
      if(i == 8) {
        // str += "\n  " + (state.stonesIn("1") / 10 > 0 ? " " : "");
        str += " | ";
      }

      else
        str += state.stonesIn(Integer.toString(i)) + " ";
    }
    return str + " " + state.stonesIn("8");
  }
}
