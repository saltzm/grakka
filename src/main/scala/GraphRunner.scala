import akka.actor.{Actor, Props, ActorSystem, ActorRef, ActorSelection} 
import GraphMessages._
import Graph._

object GraphRunner extends Application {
  val system = ActorSystem("graphSystem")

  // Start graph actor
  val graph = system.actorOf(Props[GraphActor], "graph")

  // Create random graph, adding vertices to the graph through message-passing
  //val (graphSize, avDegree, nLabels, nEdgeTypes) = (200000, 20, 30, 2)
  //GraphGenerator.generateRandomGraph(graph, graphSize, avDegree, nLabels, nEdgeTypes)
  val (graphSize, mean, stdDev, nLabels, nEdgeTypes) = (200000, 2, 1.3, 10, 2)
  GraphGenerator.generateLognormalGraph(graph, graphSize, mean, stdDev, nLabels,
    nEdgeTypes)

  // Create path query
  val query = List(
    (Vertex("1", Map("label" -> "1"), Set()), Edge("0")),
    (Vertex("2", Map("label" -> "2"), Set()), Edge("1")),
    (Vertex("3", Map("label" -> "3"), Set()), Edge("1"))/*,*/
    //(Vertex("4", Map("label" -> "4"), Set()), Edge("1")),
    /*(Vertex("5", Map("label" -> "5"), Set()), Edge("0"))*/
  )


  // Detect user input for quit signal
  var run = 0
  var alg: ActorRef = _
  try {
    for(ln <- io.Source.stdin.getLines) {
      if (ln == "q") {
        system.shutdown() 
        throw new Exception("shutdown")
      } else if (ln == "r") {
        // Execute path query
        // TODO: AWFUL
        try{ system.stop(alg) } catch { case _: Throwable => }
        alg = system.actorOf(Props(classOf[PathMatcher], graph, query, ""),
          s"alg${run}")
        run += 1
      }
    }
  } catch { case e: Throwable => println(e.getMessage) }
}


