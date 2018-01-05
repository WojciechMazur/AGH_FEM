package wmazur.fem

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4s.Implicits._
import wmazur.numerical.Interpolation2D

object Element {
  val globalOptions = new GlobalOptions()
  val eta: Double = 1/Math.sqrt(3)
  val ksi: Double = eta
  lazy val shapeFunctionsArguments = IndexedSeq((-eta, -1.0), (eta, -1.0), (1.0, -eta), (1.0, eta), (eta, 1.0), (-eta, 1.0), (-1.0, eta), (-1.0, -eta))
  val surfaceShapeFunctions: INDArray = (for ((ksi:Double, eta:Double) <- shapeFunctionsArguments) yield IndexedSeq(
    0.25 * (1 - ksi) * (1 - eta),
    0.25 * (1 + ksi) * (1 - eta),
    0.25 * (1 + ksi) * (1 + eta),
    0.25 * (1 - ksi) * (1 + eta)
  )).toNDArray

  val ksiDerivaties: INDArray = {
    (for(i<-0 until 4; fn <- Interpolation2D.derivativeByKsiFunctions) yield i match{
      case `i` if i>1 => fn(eta)
      case _ => fn(-eta)
    }).asNDArray(4,4)
  }

  val etaDerivaties: INDArray = {
    (for(i<-0 until 4; fn <- Interpolation2D.derivativeByEtaFunctions) yield i match{
      case `i` if i==1 || i==2 => fn(ksi)
      case _ => fn(-ksi)
    }).asNDArray(4,4)
  }

  val volumeShapeFunctions: INDArray = {
    (for(i<-0 until 4; fn <- Interpolation2D.shapeFunctions) yield i match{
      case `i` if i==0 => fn(-ksi, -eta)
      case `i` if i==1 => fn(ksi, -eta)
      case `i` if i==2 => fn(ksi, eta)
      case _ => fn(-ksi, eta)
    }).asNDArray(4,4)
  }
}

case class Element(nodes: Node*) {
  val edgeLength = IndexedSeq(
    nodes(3) distance nodes(0),
    nodes(1) distance nodes(0),
    nodes(2) distance nodes(1),
    nodes(3) distance nodes(2)
  )
  lazy val interpolation=Interpolation2D(nodes:_*)
  lazy val jacobian: INDArray = interpolation.jacobian
  lazy val jacobianDet: Double = interpolation.jacobianDet
  lazy val jacobianTransformation: INDArray =interpolation.jacobianTransformation

}
