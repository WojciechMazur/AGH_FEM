package wmazur.fem

import wmazur.utility.Point2

case class Node(point: Point2, temperature:Double, status:Boolean=false, id:Int){
  def distance(target: Node): Double ={
    Math.sqrt(
      Math.pow(this.point.x - target.point.x, 2)+Math.pow(this.point.y-target.point.y,2)
    )
  }
}
