package wmazur.fem

import java.io.{BufferedReader, FileNotFoundException, FileReader}

import com.google.gson.Gson

import scala.reflect.io.File
object GlobalOptions{
  val default= new GlobalOptions()

  def loadFromJSON(jsonPath: String = "resources/globalOptions.json"): GlobalOptions = {
    val gson = new Gson
    var bufferedReader:BufferedReader = null
    try
      bufferedReader = new BufferedReader(new FileReader(jsonPath))
    catch {
      case _: FileNotFoundException =>
        System.err.printf("File %s not found", jsonPath)
        return null
    }
    gson.fromJson(bufferedReader, classOf[GlobalOptions])
  }
}

class GlobalOptions(val initialTemperature:Double,var ambientTemperature:Double, val simulationTime:Int, var simulationStepTime:Int,
                    var alpha: Double,
                    val lengthVertical:Double, val lengthHorizontal:Double, val edgesVertical:Int, val edgesHorizontal:Int,
                    var specificHeat:Double, var conductivity:Double, var density:Double) {
  var elementsCount:Int=0

  def this(copy: GlobalOptions) = this(copy.initialTemperature, copy.ambientTemperature,
    copy.simulationTime, copy.simulationStepTime, copy.alpha,
    copy.lengthVertical, copy.lengthHorizontal, copy.edgesVertical, copy.edgesHorizontal, copy.specificHeat,
    copy.conductivity, copy.density)

  def this(path: String = "resources/globalOptions.json") = this(GlobalOptions.loadFromJSON(path))

  def incElementsCount():Int = {
    elementsCount+=1
    elementsCount
  }

  def postIncElementCount():Int = {
    val prev = elementsCount
    elementsCount+=1
    prev
  }

  def toCSV(file:File):File={
    for(field<-this.getClass.getDeclaredFields){
      field.setAccessible(true)
      file.appendAll(s"${field.getName},${field.get(this)}\n")
    }
    file
  }

}
