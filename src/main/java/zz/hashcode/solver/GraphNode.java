package zz.hashcode.solver;

import java.util.ArrayList;

import gnu.trove.list.array.TIntArrayList;

class GraphNode {
  private ArrayList<GraphNode> childList;
  private int name;

  GraphNode(int name) {
    this.name = name;
    childList = new ArrayList<>();
  }

  void addChild(GraphNode node) {
    if (!(this.getName() == node.getName())) {
      childList.add(node);
    }
  }

  private void removeChild(GraphNode node) {
    childList.remove(node);
  }

  boolean hasMoreChilds() {
    return childList.size() > 0;
  }

  int getName() {
    return name;
  }

  void getNextChild(int goal, TIntArrayList path, boolean firstTime) {
    if (this.getName() == goal && !firstTime) {
      path.add(this.getName());
    } else {
      if (childList.size() > 0) {
        GraphNode tmpNode = (GraphNode) childList.remove(0);
        tmpNode.removeChild(this);
        path.add(this.getName());
        tmpNode.getNextChild(goal, path, false);
      }
    }
  }
}