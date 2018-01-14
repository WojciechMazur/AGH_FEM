package wmazur.fem

import java.text.SimpleDateFormat
import java.util.Date

import com.google.gson.{Gson, GsonBuilder}
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.indexing.NDArrayIndex
import org.nd4s.Implicits._
import play.api.libs.json._

import scala.io.Source
import scala.language.postfixOps
import scala.reflect.io.File

case class SteadyStateSolution() {
  val grid: Grid = new Grid()
  val globalHMatrix: INDArray = Nd4j.zeros(grid.nodes.size, grid.nodes.size)
  val globalCMatrix: INDArray = Nd4j.zeros(grid.nodes.size, grid.nodes.size)
  val globalPVector: INDArray = Nd4j.zeros(grid.nodes.size,1)
  val temperature: INDArray = grid.nodes.map(node => node.temperature).asNDArray(grid.nodes.size, 1)
  val options: GlobalOptions = GlobalOptions.default

  private var iteration: Int = 0
  private val phaseCriteria: JsValue = Json.parse(Source.fromFile("resources//phaseCriteria.json").getLines.mkString)
  private val timestamp:String= new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(new Date)
  private val outputFile: File = reflect.io.Path(s"resources//output//$timestamp.csv").createFile()
  private val outputTemperatureFile: File = reflect.io.Path(s"resources//output//$timestamp-temperature.csv").createFile()
  private val outputConfigurationFile:File = reflect.io.Path(s"resources//output//$timestamp-configuration.txt").createFile()
  private val phases:Vector[Phase] = Phase.arrayFromFile("resources/phaseCriteria.json")

  def run(verbose: Boolean = false, liveGraph:Boolean=false): Unit = {
    outputTemperatureFile.appendAll("Time, Time in phase, Phase, Average, Max, Min, Core2Surface Delta Temp., Temperature Delta\n")
    val asr = options.conductivity / (options.specificHeat * options.density)
    options.simulationStepTime=((options.lengthVertical/options.edgesVertical) * (options.lengthHorizontal/options.edgesHorizontal)/(0.5 * asr)).toInt
    configurationToFile()

    if(liveGraph) {
      import sys.process._
      s"python ${new java.io.File(".").getCanonicalPath}/src/main/python/HeatMap.py filename=resources//output//$timestamp width=${options.edgesVertical} height=${options.edgesHorizontal} live=$liveGraph" !
    }
    def avgTemperature: Double = temperature.sumT / temperature.rows()
    def maxTemperature: Double = Nd4j.max(temperature).getDouble(0)
    def minTemperature: Double = Nd4j.min(temperature).getDouble(0)
    for(phase <-phases) {
      options.ambientTemperature=phase.ambientTemperature
      options.alpha=phase.alpha
      while (!phase.checkConditions(avgTemperature, maxTemperature, minTemperature)){
        val (time: Int, tempDelta: Double) = iterateSimulation(verbose)
        phase.phaseTime+=options.simulationStepTime
        outputTemperatureFile.appendAll(s"${secondsToTime(time)},${secondsToTime(phase.phaseTime)},${phase.name},$avgTemperature,$maxTemperature,$minTemperature,${maxTemperature-minTemperature},$tempDelta\n")
        println(s"Phase: ${phase.name}\t Temperatures: Avg: $avgTemperature\tMax: $maxTemperature\tMin: $minTemperature\tCore-surface: ${maxTemperature-minTemperature}\tDeltaT: $tempDelta\n")
      }
    }
    if(!liveGraph) {
      import sys.process._
      s"python ${new java.io.File(".").getCanonicalPath}/src/main/python/HeatMap.py filename=resources//output//$timestamp width=${options.edgesVertical} height=${options.edgesHorizontal} live=$liveGraph" !
    }
  }


  private def iterateSimulation(verbose: Boolean = false): (Int, Double) = {
    eraseMatrices()
    iteration += 1
    val oldAvgTemperature = Nd4j.mean(temperature).getDouble(0)
    for ((element, elementId) <- grid.elements.zipWithIndex) {
      val elementHMatrix = countElementHMatrix(element, elementId)
      moveToGlobalMatrix(elementHMatrix, element)
      val cMatrix = countElementCMatrix(element, elementId)
      moveToGlobalMatrix(cMatrix, element, globalCMatrix)
      moveToGlobalVector(countElementPVector(element, elementId), element)
    }
    if (verbose) {
      logIteration(s"H matrix at $iteration. iteration", globalHMatrix.toString)
      logIteration(s"C matrix at $iteration. iteration", globalCMatrix.toString)
      logIteration(s"P vector at $iteration. iteration", globalPVector.toString)
    }
    globalCMatrix divi options.simulationStepTime
    globalHMatrix addi globalCMatrix
    globalPVector addi countPPrim()
    temperature assign calcNextTemperature()

    if (verbose) {
      logIteration(s"[C matrix / dt]  at $iteration. iteration", globalCMatrix.toString)
      logIteration(s"[H matrix  + C matrix / dt] at $iteration. iteration", globalHMatrix.toString)
      logIteration(s"[P vector + P'] at $iteration. iteration", globalPVector.toString)
      logIteration(s"Temperature vector after $iteration. iteration [t=${iteration * Element.globalOptions.simulationStepTime}]", temperature.toString)
    }
    outputFile.appendAll(s"$iteration, ${secondsToTime(iteration*options.simulationStepTime)}, ${temperature.data().asDouble().mkString(",")}\n")
    logIteration(s"Temperature visualization after $iteration. iteration [t=${secondsToTime(iteration * options.simulationStepTime)}]",
      temperature.reshape(Element.globalOptions.edgesHorizontal, Element.globalOptions.edgesVertical).toString)
    (iteration*options.simulationStepTime, (Nd4j.mean(temperature).getDouble(0)-oldAvgTemperature)/options.simulationStepTime*60)
  }

  private def countElementHMatrix(element: Element, elementId: Int): INDArray = {
    val localHMatrix: INDArray = Nd4j.zeros(element.nodes.size, 4, 4)
    val convection = Nd4j.zeros(8, 4, 4)

    for (i <- 0 until 8; j <- 0 until 4; k <- 0 until 4) {
      val nodeId: Int = i / 2
      if (element.nodes(nodeId).status && element.nodes((nodeId + 1) % 4).status) {
        val surface = Element.surfaceShapeFunctions
        val conv = Element.globalOptions.alpha * surface.getDouble(i, j) * surface.getDouble(i, k) * element.edgeLength(nodeId) / 2
        convection.putScalar(Array(i, j, k), conv)
      }
    }

    for ((_, nodeId) <- element.nodes.zipWithIndex; i <- 0 until 4; j <- 0 until 4) {
      val rowX = element.jacobianTransformation.getRow(nodeId).getRow(0)
      val rowY = element.jacobianTransformation.getRow(nodeId).getRow(1)
      val tmpX: Double = rowX.getDouble(i) * rowX.getDouble(j)
      val tmpY: Double = rowY.getDouble(i) * rowY.getDouble(j)
      val conduction = Element.globalOptions.conductivity * (tmpX + tmpY) * element.jacobianDet
      val total = conduction + convection.getDouble(nodeId * 2, i, j) + convection.getDouble(nodeId * 2 + 1, i, j)
      localHMatrix.putScalar(Array(nodeId, i, j), total)
    }

    val sepMatrix: Seq[INDArray] = for (i <- 0 until 4)
      yield localHMatrix.get(NDArrayIndex.point(i), NDArrayIndex.all(), NDArrayIndex.all())

    sepMatrix.foldLeft(Nd4j.zeros(4, 4)) { case (sum: INDArray, mat: INDArray) => sum add mat }
  }

  private def countElementCMatrix(element: Element, elementId: Int): INDArray = {
    val localCMatrix: INDArray = (for (i <- 0 until 4; j <- 0 until 4; k <- 0 until 4) yield
      Element.volumeShapeFunctions.getDouble(i, j) * Element.volumeShapeFunctions.getDouble(i, k) *
        Element.globalOptions.density * Element.globalOptions.specificHeat * element.jacobianDet)
      .asNDArray(4, 4, 4)

    val sepMatrix: Seq[INDArray] = for (i <- 0 until 4)
      yield localCMatrix.get(NDArrayIndex.point(i), NDArrayIndex.all(), NDArrayIndex.all())

    sepMatrix.foldLeft(Nd4j.zeros(4, 4)) { case (sum: INDArray, mat: INDArray) => sum add mat }
  }

  private def countElementPVector(element: Element, elementId: Int): INDArray = {
    val localPMatrix: INDArray = Nd4j.zeros(8, 4)
    for (i <- 0 until 8; j <- 0 until 4) {
      val edgeId: Int = i / 2
      if (element.nodes(edgeId).status && element.nodes((edgeId + 1) % 4).status) {
        val value = Element.globalOptions.ambientTemperature * Element.globalOptions.alpha *
          Element.surfaceShapeFunctions.getDouble(i, j) * element.edgeLength(edgeId) / 2
        localPMatrix.putScalar(Array(i, j), value)
      }
    }
    val sepMatrix: Seq[INDArray] = for (i <- 0 until 8)
      yield localPMatrix.get(NDArrayIndex.point(i), NDArrayIndex.all())
    sepMatrix.foldLeft(Nd4j.zeros(4)) { case (sum: INDArray, mat: INDArray) => sum add mat }
  }

  private def countPPrim(): INDArray = {
    (for (i <- grid.nodes.indices) yield
      (globalCMatrix.getRow(i) mul temperature).data().asDouble()
        .foldLeft(0.0) {
          {
            case (sum: Double, value: Double) => sum + value
          }
        }
      ).toNDArray
  }

  private def calcNextTemperature(): INDArray = {
   // val result: INDArray = InvertMatrix.invert(globalHMatrix, false) mmul globalPVector
    import breeze.linalg._
   val matrix: DenseMatrix[Double] = new DenseMatrix(grid.nodes.size, grid.nodes.size, globalHMatrix.data().asDouble())
   val vector: DenseVector[Double] = new DenseVector[Double](globalPVector.data().asDouble())
   val result: DenseVector[Double] = (matrix.t * matrix) \ (matrix.t * vector)
  result.toArray.toNDArray
  }

  private def moveToGlobalMatrix(matrix: INDArray, element: Element, globalMatrix: INDArray = globalHMatrix): Unit = {
    for (i <- 0 until 4; j <- 0 until 4) {
      val row = element.nodes(i).id
      val column = element.nodes(j).id
      val currentValue = globalMatrix.getDouble(row, column)
      globalMatrix.putScalar(Array(row, column), currentValue + matrix.getDouble(i, j))
    }
  }

  private def moveToGlobalVector(matrix: INDArray, element: Element, globalVector: INDArray = globalPVector): Unit = {
    for (i <- 0 until 4) {
      val column = element.nodes(i).id
      val currentValue = globalVector.getDouble(column)
      globalVector.putScalar(column, currentValue + matrix.getDouble(i))
    }
  }

  private def logIteration(strings: String*): Unit = {
    strings.foreach(s => println(s))
  }

  private def eraseMatrices(): Unit ={
    val gSize=grid.nodes.size
    globalHMatrix assign Nd4j.zeros(gSize, gSize)
    globalCMatrix assign Nd4j.zeros(gSize, gSize)
    globalPVector assign Nd4j.zeros(gSize, 1)
  }

  private def secondsToTime(seconds:Int): String ={
    val hour: Int = Math.floor(seconds/3600).toInt
    val minute: Int = Math.floor((seconds%3600)/60).toInt
    val second:Int = seconds - hour*3600 - minute*60
    s"${hour}h ${minute}m ${second}s "
  }

  private def configurationToFile():Unit={
    val gson: Gson = new GsonBuilder().setPrettyPrinting().create()
    outputConfigurationFile.appendAll("Global options:\n")
    options.toCSV(outputConfigurationFile)
    outputConfigurationFile.appendAll("\nPhases configuration:\n")
    outputConfigurationFile.appendAll(Json.prettyPrint(phaseCriteria))

  }
}

