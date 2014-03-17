import akka.actor.ActorRef
import scala.util.Random
import Graph._
import GraphMessages._


object GraphGenerator {
  private def uniform (mean: Int) = rand.nextInt(mean*2 + 1)
  private def lognormal (mean: Double, stdDev: Double) = 
    math.exp(mean + rand.nextGaussian() * stdDev).toInt
  private val rand = new Random

  def generateRandomGraph (graph: ActorRef, nVertices: Int, avDegree: Int,
    nLabels: Int, nEdgeTypes: Int): Unit = {
    var nEdges = 0
    graph ! StartLoading
    (0 until nVertices).foreach { i => 
      val deg = uniform(avDegree)
      nEdges += deg
      val children = (0 until deg).map (_ => 
        (rand.nextInt(nVertices).toString.asInstanceOf[Id],
          Edge(rand.nextInt(nEdgeTypes).toString))
      ).toSet
      graph ! AddVertex(Vertex(i.toString, Map("label" ->
        rand.nextInt(nLabels).toString, "id" -> i.toString), children))
    }
    println(s"nEdges: $nEdges")
    graph ! StopLoading
  }

  def generateLognormalGraph (graph: ActorRef, nVertices: Int, mean: Double,
    stdDev: Double, nLabels: Int, nEdgeTypes: Int): Unit = {
    graph ! StartLoading
    var nEdges = 0
    (0 until nVertices).foreach { i => 
      val deg = lognormal(mean, stdDev)
      nEdges += deg
      val children = (0 until deg).map (_ => 
        (rand.nextInt(nVertices).toString.asInstanceOf[Id],
          Edge(rand.nextInt(nEdgeTypes).toString))
      ).toSet
      graph ! AddVertex(Vertex(i.toString, Map("label" ->
        rand.nextInt(nLabels).toString), children))
    }
    println(s"nEdges: $nEdges")
    graph ! StopLoading
  }

}
