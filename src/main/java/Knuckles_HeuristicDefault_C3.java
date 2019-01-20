/**
 * Final form
 */
@SuppressWarnings("Duplicates")
public class Knuckles_HeuristicDefault_C3 extends Knuckles_HeuristicDefault {
  @Override
  protected double getC() {
    return 3.d;
  }

  @Override
  public String toString() {
    return super.toString() + " C" + getC();
  }
}
