package wmazur.numerical

import org.scalatest.FlatSpec
import wmazur.utility.{Point, Range}

class LocalIntegralTest extends FlatSpec{

  "Local integral" should "successfully calculate area" in{
    println(LocalIntegral(IntegrationPoints(2)).calc((x, y) => 2*x*x*y*y+6*x+5))
    println(LocalIntegral(IntegrationPoints(3)).calc((x, y) => 2*x*x*y*y+6*x+5))

    val interpolation1D = Interpolation1D(Point(-2), Point(4))
    println(s"Jacobian: ${interpolation1D.jacobianDet} | reverse jacobian: ${interpolation1D.reverseJacobianDet} ")
    val integral=LocalIntegral(IntegrationPoints(2)).calc((x) => 2*x+5)
    println(s"Local integral: $integral \nGlobal integral in ${interpolation1D.points}: ${integral*interpolation1D.jacobianDet}" )
    assert(integral*interpolation1D.jacobianDet==40)
    //val integral2 = LocalIntegral.LocalIntegral((x,y) => 2*x*y)(2.0, 2.0)
    //val integral3 = LocalIntegral.LocalIntegral((x, y, z) => 2*x*y*z)(2.0, 2.0, 2.0)

  }

}
