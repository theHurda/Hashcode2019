package zz.hashcode.solver;

public class Edge implements Comparable<Edge> {
  private int from, to;
  private double cost;

  Edge(int _from, int _to, double _cost) {
    from = _from;
    to = _to;
    cost = _cost;
  }

  public int compareTo(Edge e) {
    return Double.compare(this.cost, e.cost);
  }

  int getTo() {
    return to;
  }

  int getFrom() {
    return from;
  }
}