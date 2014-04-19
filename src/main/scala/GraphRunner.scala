import akka.actor.ActorSystem
import Messages._

object CreateGraphTest extends Application {
  val system = ActorSystem("grakka")
  val graph = system.actorOf(GraphActor.props(2), "graph")
  val vertices = List(
    Vertex(0, Map("id" -> "0")),
    Vertex(1, Map("id" -> "1")),
    Vertex(2, Map("id" -> "2"))
  )
  graph ! StartLoading
  vertices.foreach(graph ! AddVertex(_))
  graph ! AddEdge(0, 1, Set("likes"))
  graph ! RemoveVertex(1)
  graph ! StopLoading
  Thread.sleep(2000)
  graph ! StartLoading
  // If you don't wait, you'll get a "name not unique" error
  // when creating a new vertex
  graph ! AddVertex(Vertex(1, Map("new" -> "bla")))
  graph ! StopLoading
  Thread.sleep(2000)
  system.shutdown()
}

object PathFindingTest1 extends Application {
  val system = ActorSystem("grakka")
  val graph = system.actorOf(GraphActor.props(4), "graph")
//  val (graphSize, mean, stdDev, nLabels, nEdgeTypes) = (100000, 2, 1.3, 10, 1)
//  GraphGenerator.generateLognormalGraph(graph, graphSize, mean, stdDev, nLabels,
//    nEdgeTypes)

  val vertices = List(

  )
  val edges = List(
    (0, 1, Set("e")),
    (0, 2, Set("e"))
  )

  graph ! StartLoading
  vertices.foreach (graph ! AddVertex(_))
  edges.foreach { case (from, to, attrs) => graph ! AddEdge(from, to, attrs) }
  graph ! StopLoading
}
