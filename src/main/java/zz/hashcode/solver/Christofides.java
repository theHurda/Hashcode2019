package zz.hashcode.solver;

import java.util.Arrays;

import gnu.trove.list.array.TIntArrayList;

public class Christofides {

  public Christofides() {
  }

  public int[] solve(double[][] weightMatrix) {
    int[] mst = prim(weightMatrix, weightMatrix[0].length);
    int[][] match = greadyMatch(mst, weightMatrix, weightMatrix[0].length);
    GraphNode[] nodes = buildMultiGraph(match, mst);

    return getEulerCircuit(nodes);
  }

  private int[] getEulerCircuit(GraphNode[] nodes) {
    TIntArrayList path = new TIntArrayList();
    TIntArrayList tmpPath = new TIntArrayList();
    int j = 0;

    //lägg in första cykeln i path, getNextChild går djupet först och retu
    nodes[0].getNextChild(nodes[0].getName(), tmpPath, true);
    path.addAll(tmpPath);

    //gå igenom alla noder i vår path, om noden har fler utgående kanter så kolla cykler efter denna. stopp in cykeln
    // på rätt plats
    while (j < path.size()) {
      if (nodes[path.get(j)].hasMoreChilds()) {
        nodes[path.get(j)].getNextChild(nodes[path.get(j)].getName(), tmpPath, true);
        if (tmpPath.size() > 0) {
          //sätt ihop path och tmpPath
          for (int i = 0; i < path.size(); i++) {
            if (path.get(i) == tmpPath.get(0)) {
              // ZZZZZZZZZZZZ path.addAll(i, tmpPath);
              TIntArrayList newPath = new TIntArrayList();
              newPath.addAll(path.subList(0, i));
              newPath.addAll(tmpPath);
              newPath.addAll(path.subList(i, path.size()));
              path = newPath;
              break;
            }
          }
          tmpPath.clear();
        }
        j = 0;
      } else {
        j++;
      }
    }

    boolean[] inPath = new boolean[nodes.length];
    int[] route = new int[nodes.length];
    j = 0;
    for (int i = 0; i < path.size(); i++) {
      if (!inPath[path.get(i)]) {
        route[j] = path.get(i);
        j++;
        inPath[path.get(i)] = true;
      }
    }

    return route;
  }

  /**
   * Builds the union of MST and MATCH, which is a multi graph
   *
   * @param match The "minimum" perfect match on the set of odd nodes.
   * @param mst   The minimal spanning tree
   * @return One dimensional nodes matrix representing the multi graph
   * @since 1.0
   */

  private GraphNode[] buildMultiGraph(int[][] match, int[] mst) {
    GraphNode[] nodes = new GraphNode[mst.length];
    //skapa tomma noder
    for (int i = 0; i < mst.length; i++) {
      nodes[i] = new GraphNode(i);
    }

    //lägg till noder och kanter från MST, symmetriska kanter!
    for (int i = 1; i < mst.length; i++) {
      nodes[i].addChild(nodes[mst[i]]);
      nodes[mst[i]].addChild(nodes[i]);
    }

    //lägg till noder och kanter från MATCHNING, symmetriska kanter!
    for (int[] subMatch : match) {
      nodes[subMatch[0]].addChild(nodes[subMatch[1]]);
      nodes[subMatch[1]].addChild(nodes[subMatch[0]]);
    }

    return nodes;
  }

  /**
   * Using Prim's algorithm to find the Minimal Spanning Tree.
   *
   * @param wt  Weightmatrix.
   * @param dim Number of dimensions in the problem.
   * @return The parentvector. p[i] gives the parent of node i.
   * @since 1.0
   */

  private int[] prim(double[][] wt, int dim) {

    TIntArrayList queue = new TIntArrayList();
    for (int i = 0; i < dim; i++) {
      queue.add(i);
    }

    // Prim's algorithm
    boolean[] isInTree = new boolean[dim];
    double[] key = new double[dim]; //avstånd från nod i och nod parent[i].
    int[] p = new int[dim]; //parent

    for (int i = 0; i < dim; i++) {
      key[i] = Integer.MAX_VALUE;
    }

    key[0] = 0; // root-node
    int u = 0;

    double temp;
    int elem;
    do {
      isInTree[u] = true; //lägg till noden i trädet
      queue.remove(u);
      for (int v = 0; v < dim; v++) { // kan forenkles om det ikke er en komplett graf!
        if (!isInTree[v] && wt[u][v] < key[v]) {
          p[v] = u;
          key[v] = wt[u][v];
        }
      }

      // ExtractMin, går igenom alla kvarvarande noder och tar ut den med kortast avstånd till trädet
      double mint = Double.MAX_VALUE;
      for (int i = 0; i < queue.size(); i++) {
        elem = queue.get(i);
        temp = key[elem];
        if (temp < mint) {
          u = elem;
          mint = temp;
        }
      }
    } while (!queue.isEmpty());

    return p;
  }

  /**
   * Finds a match between the nodes that hava odd number of edges. Not perfect that gready, that is take the
   * shortest distance found first. Then the next shortest of the remaining i chosen.
   *
   * @param p   Parentvector. p[i] gives the parent of node i.
   * @param wt  Weightmatrix of the complete graph.
   * @param dim Number of dimensions in the problem.
   * @return Twodimensional matrix containing the pairs. Two columns where each row represent a pair.
   * @since 1.0
   */

  private int[][] greadyMatch(int[] p, double[][] wt, int dim) {

    Node[] nodes = new Node[p.length];

    //skapa en skog
    nodes[0] = new Node(0, true); //roten
    for (int i = 1; i < p.length; i++) {
      nodes[i] = new Node(i, false);
    }

    //bygg ett träd av skogen
    for (int i = 0; i < p.length; i++) {
      if (p[i] != i) {
        nodes[p[i]].addChild(nodes[i]);
      }
    }

    //hitta udda noder
    TIntArrayList oddDegreeNodes = findOddDegreeNodes(nodes[0]);
    int nOdd = oddDegreeNodes.size();

    //försök hitta en så minimal matchning som möjligt med en girig metod
    //sortera alla kanter mellan de udda hörnen
    Edge[][] edges = new Edge[nOdd][nOdd];
    for (int i = 0; i < nOdd; i++) {
      for (int j = 0; j < nOdd; j++) {
        if (((Integer) oddDegreeNodes.get(i)).intValue() != ((Integer) oddDegreeNodes.get(j)).intValue()) {
          edges[i][j] = new Edge(oddDegreeNodes.get(i), oddDegreeNodes.get(j),
              wt[oddDegreeNodes.get(i)][oddDegreeNodes.get(j)]);
        } else {
          edges[i][j] = new Edge(oddDegreeNodes.get(i), oddDegreeNodes.get(j), Double.MAX_VALUE);
        }
      }
      Arrays.sort(edges[i]); //sortera alla kanter från nod i
    }

    boolean[] matched = new boolean[dim];
    int[][] match = new int[(nOdd / 2)][2];

    // för varje hörn plocka ut den kortaste kanten
    // vid krock välj den kortaste av de näst kortaste.
    // antalet noder med udda gradtal alltid delbart med 2
    int k = 0;
    for (int i = 0; i < nOdd; i++) {
      for (int j = 0; j < nOdd; j++) {
        if (matched[edges[i][j].getFrom()] || matched[edges[i][j].getTo()]) {
          continue;
        } else {
          matched[edges[i][j].getFrom()] = true;
          matched[edges[i][j].getTo()] = true;
          match[k][0] = edges[i][j].getFrom();
          match[k][1] = edges[i][j].getTo();
          k++;
        }
      }
    }

    return match;
  }

  /**
   * Activates the treetraversing-routine that builds the path given by DFS.
   *
   * @param _root The root which is the start node of the route.
   * @return The route which is the order of nodes after the traversing.
   * @since 1.0
   */

  private TIntArrayList buildRoute(Node _root) {
    TIntArrayList route = new TIntArrayList();
    _root.visitBuildRoute(route);
    return route;
  }

  /**
   * Activates the routine that finds vertexes which have odd number of edges.
   *
   * @param _root Startnode.
   * @return List of nodes with odd number of edges.
   * @since 1.0
   */

  private TIntArrayList findOddDegreeNodes(Node _root) {
    TIntArrayList oddNodes = new TIntArrayList();
    _root.visitFindOddDegreeNodes(oddNodes);
    return oddNodes;
  }
}