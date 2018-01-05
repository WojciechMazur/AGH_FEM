package wmazur.fem

import java.io.{BufferedReader, FileNotFoundException, FileReader}

import com.google.gson.Gson
object GlobalOptions{

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

class GlobalOptions(val initialTemperature:Double,val ambientTemperature:Double, val simulationTime:Int, val simulationStepTime:Int,
                    val alpha: Double,
                    val lengthVertical:Double, val lengthHorizontal:Double, val edgesVertical:Int, val edgesHorizontal:Int,
                    val specificHeat:Double, val conductivity:Double, val density:Double) {
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


}
