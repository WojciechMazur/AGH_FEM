package wmazur.numerical

import org.scalatest.FlatSpec
import wmazur.utility.{Point2, Range}
import org.nd4j.linalg.inverse.InvertMatrix
class InterpolationTest extends FlatSpec{

  "Interpolation 2D " should "calculate jacobian" in{
   // val interpolation2D = Interpolation2D(
   //   Point2(-2, -1),
   //   Point2( 2, -1),
   //   Point2( 2,  1.0),
   //   Point2(-2,  1.0))
//
   // println("\nJacobian\n"+interpolation2D.jacobian)
   // println("\nInverted matrix\n"+InvertMatrix.invert(interpolation2D.jacobian, true))
   // println("\nShape functions derivatives Matrix\n"+Interpolation2D.derivativesMatrix)
   // println("\nJacobian transformation matrix\n"+interpolation2D.jacobianTransformation)

  }
}
