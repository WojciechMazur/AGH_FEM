package wmazur.numerical

case class IntegrationPoints(numberOfPoints: Int) {
  val factors: Seq[Double] = numberOfPoints match {
    case 2 => Seq(-1 / Math.sqrt(3), 1 / Math.sqrt(3))
    case 3 => Seq(-Math.sqrt(3.0 / 5), 0.0, Math.sqrt(3.0 / 5))
    case _ => throw new NotImplementedError("Only 2 and 3 integration points are currently supported")
  }
  val weights: Seq[Double] = numberOfPoints match {
    case 2 => Seq(1.0, 1.0)
    case 3 => Seq(5.0 / 9, 8.0 / 9, 5.0 / 9)
  }
}
