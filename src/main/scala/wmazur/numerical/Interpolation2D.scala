package wmazur.numerical

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.inverse.InvertMatrix
import org.nd4s.Implicits._
import wmazur.fem.{Element, Node}
import wmazur.utility.Point2

object Interpolation2D{
  val ksi: Double = 1/Math.sqrt(3)
  val eta: Double = ksi
  val shapeFunctions = IndexedSeq(
    (ksi:Double, eta:Double)=>0.25*(1-ksi)*(1-eta),
    (ksi:Double, eta:Double)=>0.25*(1+ksi)*(1-eta),
    (ksi:Double, eta:Double)=>0.25*(1+ksi)*(1+eta),
    (ksi:Double, eta:Double)=>0.25*(1-ksi)*(1+eta)
  )

  val derivativeByKsiFunctions = IndexedSeq(
    (eta: Double) => -0.25*(1-eta),
    (eta: Double) =>  0.25*(1-eta),
    (eta: Double) =>  0.25*(1+eta),
    (eta: Double) => -0.25*(1+eta)
  )

  val derivativeByEtaFunctions = IndexedSeq(
    (ksi: Double)=> -0.25*(1-ksi),
    (ksi: Double)=> -0.25*(1+ksi),
    (ksi: Double)=>  0.25*(1+ksi),
    (ksi: Double)=>  0.25*(1-ksi)
  )

  val derivativeByEta: IndexedSeq[Double] = for(n<-derivativeByEtaFunctions) yield n(ksi)
  val derivativeByKsi: IndexedSeq[Double] = for(n<-derivativeByKsiFunctions) yield n(eta)
  val derivativesMatrix: INDArray = IndexedSeq(derivativeByEta, derivativeByKsi).flatten.asNDArray(2,4)
}

case class Interpolation2D(nodes: Node* ) {
  val xs: Seq[Double] = nodes.map(n => n.point.x)
  val ys: Seq[Double] = nodes.map(n => n.point.y)

  val jacobian: INDArray = (for((derivative, i)<-Seq(Element.ksiDerivaties, Element.etaDerivaties).zipWithIndex;
                                points<-Seq(xs, ys))
    yield points.zip(derivative.getRow(i).data().asDouble())
      .foldLeft(0.0){{case (sum:Double, (dn:Double, x:Double))=>sum+dn*x}}
    ).asNDArray(2,2)

  val jacobianDet: Double = jacobian(0,0)*jacobian(1,1)-jacobian(0,1)*jacobian(1,0)
  //val jacobianTransformation: INDArray = InvertMatrix.invert(jacobian, false) dot Interpolation2D.derivativesMatrix
  val jacobianTransformation:INDArray = Nd4j.zeros(4,2,4)
  for(i<-0 until nodes.size; j<-0 until Element.ksiDerivaties.columns()){
    jacobianTransformation(i,0,j)=1.0/jacobianDet*( jacobian(1,1)*Element.ksiDerivaties(i,j)+(-jacobian(0,1)*Element.etaDerivaties(i,j)))
    jacobianTransformation(i,1,j)=1.0/jacobianDet*(-jacobian(1,0)*Element.ksiDerivaties(i,j)+( jacobian(0,0)*Element.etaDerivaties(i,j)))
  }
}
