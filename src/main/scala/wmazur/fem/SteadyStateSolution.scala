package wmazur.fem

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.indexing.NDArrayIndex
import org.nd4s.Implicits._
import breeze.linalg._
import breeze.numerics._

case class SteadyStateSolution(){
  val grid: Grid = new Grid()
  val globalHMatrix: INDArray = Nd4j.zeros(grid.nodes.size, grid.nodes.size)
  val globalCMatrix: INDArray = Nd4j.zeros(grid.nodes.size, grid.nodes.size)
  val globalPVector: INDArray = Nd4j.zeros(grid.nodes.size)
  val temperature: INDArray = grid.nodes.map(node => node.temperature).toNDArray
  private var iteration: Int = 0
  def run(verbose:Boolean=false):Unit={
    val options = new GlobalOptions()
    val iterations: Int = options.simulationTime/options.simulationStepTime

    for(i<-0 until iterations)
      iterateSimulation(verbose)
  }

  private def iterateSimulation(verbose:Boolean=false):Unit = {
    iteration+=1
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

    globalCMatrix divi Element.globalOptions.simulationStepTime
    globalHMatrix addi globalCMatrix
    globalPVector addi countPPrim()
    temperature assign calcNextTemperature()

    if (verbose) {
      logIteration(s"[C matrix / dt]  at $iteration. iteration", globalCMatrix.toString)
      logIteration(s"[H matrix  + C matrix / dt] at $iteration. iteration", globalHMatrix.toString)
      logIteration(s"[P vector + P'] at $iteration. iteration", globalPVector.toString)
      logIteration(s"Temperature vector after $iteration. iteration [t=${iteration*Element.globalOptions.simulationStepTime}]",temperature.toString)
    }
      logIteration(s"Temperature visualization after $iteration. iteration [t=${iteration*Element.globalOptions.simulationStepTime}]",
        temperature.reshape(Element.globalOptions.edgesHorizontal, Element.globalOptions.edgesVertical).toString)
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

    for ((nodes, nodeId) <- element.nodes.zipWithIndex; i <- 0 until 4; j <- 0 until 4) {
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
  private def countPPrim():INDArray = {
    (for(i<- grid.nodes.indices) yield
      (globalCMatrix.getRow(i) mul temperature).data().asDouble()
      .foldLeft(0.0) {{case (sum:Double, value: Double)=>sum+value}}
      ).toNDArray
  }
  private def calcNextTemperature():INDArray = {
    val data = globalHMatrix.transpose().data().asDouble()
    val matrix: DenseMatrix[Double] = new DenseMatrix(grid.nodes.size, grid.nodes.size, data)
    val vector: DenseVector[Double] = new DenseVector[Double](globalPVector.data().asDouble())
    val  result: DenseVector[Double] = matrix \ vector
    result.data.asNDArray(1, grid.nodes.size)
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
  private def logIteration(strings: String*): Unit ={
    strings.foreach(s => println(s))
    println()
  }
}

