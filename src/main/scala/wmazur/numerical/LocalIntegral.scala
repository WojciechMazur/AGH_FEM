package wmazur.numerical

case class LocalIntegral(integrationPoints: IntegrationPoints){

  def calc(f: Double =>Double): Double = {
    def accCalc(seq: List[Double], acc:Double=0):Double = seq match {
      case Nil => acc
      case x::xs => accCalc(xs, acc+f(x))
    }
    accCalc(this.integrationPoints.factors.toList)
  }

    def calc(f: (Double, Double) => Double): Double = {
      val xs: List[(Double, Double)] = this.integrationPoints.factors
        .zip(this.integrationPoints.weights).toList
      val ys = xs map { x => x }

      def accCalc(a: List[(Double, Double)], b: List[(Double, Double)], acc: Double = 0): Double = (a, b) match {
        case (Nil, _) => acc
        case (_ :: xt, Nil) => accCalc(xt, ys, acc)
        case (x :: xt, y :: yt) => accCalc(x :: xt, yt, acc + f(x._1, y._1) * x._2 * y._2)
      }

      accCalc(xs, ys)
    }

  def calc(f: (Double, Double, Double)=>Double):Double ={
    val xs: List[(Double, Double)] = this.integrationPoints.factors
      .zip(this.integrationPoints.weights).toList
    val ys = xs map { x => x }
    val zs = xs map {x => x}

    def accCalc(a: List[(Double, Double)], b: List[(Double, Double)], c: List[(Double, Double)], acc: Double = 0): Double = (a, b, c) match {
      case (Nil, _,  _) => acc
      case (x, _::yt, Nil) => accCalc(x,  yt, zs, acc)
      case (_ :: xt,Nil,_) => accCalc(xt, ys, zs, acc)
      case (x :: xt, y :: yt, z :: zt) => accCalc(x :: xt, y::yt, zt, acc + f(x._1, y._1, z._1) * x._2 * y._2 * z._2)
    }
    accCalc(xs, ys, zs)
  }
}
