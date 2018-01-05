package wmazur.fem

import org.scalatest.FlatSpec
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4s.Implicits._
import wmazur.numerical.Interpolation2D

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

  }
}
