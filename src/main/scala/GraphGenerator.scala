import akka.actor.ActorRef
import scala.util.Random
import Graph._
import GraphMessages._

object GraphGenerator {
  def generateRandomGraph (graph: ActorRef, nVertices: Int, avDegree: Int,
    nLabels: Int, nEdgeTypes: Int): Unit = {
    graph ! StartLoading
    val rand = new Random
    (0 until nVertices).foreach { i => 
      val children = (0 until rand.nextInt(avDegree*2)).map (_ => 
        (rand.nextInt(nVertices).toString.asInstanceOf[Id],
          Edge(rand.nextInt(nEdgeTypes).toString))
      ).toSet
      graph ! AddVertex(Vertex(i.toString, Map("label" ->
        rand.nextInt(nLabels).toString), children))
    }
    graph ! StopLoading
  }
}
