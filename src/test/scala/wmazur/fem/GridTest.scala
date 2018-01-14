package wmazur.fem

import org.scalatest.FlatSpec

class GridTest extends FlatSpec{

  "Grid constructor" should "create proper grid" in {
    val grid = new Grid
    for (x <- grid.nodes) {
      printf("#%d : (%f, %f) -> %b\n",x.id, x.point.x, x.point.x, x.status)
    }
    for (x <- grid.elements) {
      for (y <- x.nodes) {
        print(y.id+1 + " -> ")
      }
      println()
    }

    for(i<- 0 until GlobalOptions.default.edgesVertical) {
      for (j <- 0 until GlobalOptions.default.edgesHorizontal)
        print(grid.nodes(i * GlobalOptions.default.edgesVertical + j).status + " ")
      println()
    }
  }
}
