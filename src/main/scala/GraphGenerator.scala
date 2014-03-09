object GraphGenerator {
  import scala.util.Random
  import Graph._
  def generateRandomGraph (nVertices: Int, avDegree: Int, nLabels: Int,
    nEdgeTypes: Int) = {
    val rand = new Random
    (0 until nVertices).map { i => 
      val children = (0 until rand.nextInt(avDegree*2)).map (_ => 
        (rand.nextInt(nVertices).toString.asInstanceOf[Id],
          Edge(rand.nextInt(nEdgeTypes).toString))
      ).toSet
      Vertex(i.toString, Map("label" -> rand.nextInt(nLabels).toString), children) 
    }
  }
}
