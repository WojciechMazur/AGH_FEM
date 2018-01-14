package wmazur.fem

import wmazur.utility.Point2

class Grid(options: GlobalOptions = new GlobalOptions()) {
  val sectionSizeX: Double = options.lengthHorizontal / (options.edgesHorizontal - 1)
  val sectionSizeY: Double = options.lengthVertical / (options.edgesVertical - 1)

  val nodes: IndexedSeq[Node] = for(i:Int <- 0 until options.edgesHorizontal; j:Int<- 0 until options.edgesVertical)
    yield Node(
      Point2(i * sectionSizeX,j * sectionSizeY),
      options.initialTemperature,
      i==options.edgesHorizontal - 1 || i == 0 || j == options.edgesVertical - 1 || j == 0,
      id = options.postIncElementCount()
    )

  val elements: IndexedSeq[Element] =
    for(node <- nodes
      if node.id<nodes.size-options.edgesVertical &&
         node.id%options.edgesVertical!=options.edgesVertical-1
    ) yield Element(
      nodes(node.id),
      nodes(node.id+options.edgesVertical),
      nodes(node.id+options.edgesVertical+1),
      nodes(node.id+1)
    )
}
