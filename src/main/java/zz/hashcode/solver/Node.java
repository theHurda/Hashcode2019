package zz.hashcode.solver;

import java.util.ArrayList;

import gnu.trove.list.array.TIntArrayList;

class Node {
  private boolean isRoot;
  private int number;
  private ArrayList<Node> children;

  Node(int _n, boolean _isRoot) {
    number = _n;
    children = null;
    this.isRoot = _isRoot;
  }

  void addChild(Node _node) {
    if (children == null) {
      children = new ArrayList<>();
    }
    children.add(_node);
  }

  void visitBuildRoute(TIntArrayList _route) {
    _route.add(number);
    if (children == null) {
      return;
    }
    for (Node child : children) {
      child.visitBuildRoute(_route);
    }
  }

  void visitFindOddDegreeNodes(TIntArrayList _oddNodes) {
    if (children == null) {
      _oddNodes.add(number);
      return;
    }
    if (isRoot && children.size() % 2 != 0) {
      _oddNodes.add(number);
    }
    if (!isRoot && children.size() % 2 == 0) {
      _oddNodes.add(number);
    }
    for (Node child : children) {
      child.visitFindOddDegreeNodes(_oddNodes);
    }
  }

}