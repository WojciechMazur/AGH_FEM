package wmazur.fem
import java.io.FileReader

import com.google.gson.Gson

object Phase{
  def arrayFromFile(path: String): Vector[Phase] = {
    val gson:Gson = new Gson()
    val arr: Array[Phase] = gson.fromJson(new FileReader(path), classOf[Array[Phase]])
    arr.toVector
  }
}
case class Phase(name:String, ambientTemperature:Double, alpha:Double,
                 endAvgTemperature:Double, endMaxTemperature:Double, endMinTemperature:Double,
                 conditions:Array[String]) {
  var phaseTime:Int = 0

  def checkConditions(avgTemperature: Double, maxTemperature:Double, minTemperature:Double):Boolean={
    import scala.reflect.runtime.currentMirror
    import scala.tools.reflect.ToolBox
    val condition = conditions.mkString(" && ")
      .replace("avgTemperature", s"$avgTemperature")
      .replace("minTemperature", s"$minTemperature")
      .replace("maxTemperature", s"$maxTemperature")
      .replace("endAvgTemperature", s"$endAvgTemperature")
      .replace("endMinTemperature", s"$endMinTemperature")
      .replace("endMaxTemperature", s"$endMaxTemperature")
      .replace("time", s"$phaseTime")
    val toolBox = currentMirror.mkToolBox()
    toolBox.eval(toolBox.parse(condition)).asInstanceOf[Boolean]
  }
}

