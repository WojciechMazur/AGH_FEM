package wmazur.numerical

import wmazur.utility.{Point, Range}

case class Interpolation1D(points: Point*) {
  val ksi: Double = 1/ Math.sqrt(3)
  val shapeFunctions = IndexedSeq(
    (ksi:Double)=>0.5*(1-ksi),
    (ksi:Double)=>0.5*(1+ksi)
  )

  val shapeDerivative: IndexedSeq[Double] = IndexedSeq(-0.5, 0.5)

  val interpolatedPoints: Double = positionMapping(points:_*)
  val jacobianDet: Double = points.zip(shapeDerivative).foldLeft(0.0){{case (sum:Double, (p:Point, dn:Double))=> sum+dn*p.x}}
  val reverseJacobianDet: Double = 1/jacobianDet

  def positionMapping(p:Point*):Double = {
    p.zip(shapeFunctions).foldLeft(0.0) { case (sum: Double, (p: Point, n)) => sum + n(ksi)*p.x }
  }

}