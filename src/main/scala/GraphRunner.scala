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
  val (graphSize, mean, stdDev, nLabels, nEdgeTypes) = (10, 2, 1.3, 2, 1)
  GraphGenerator.generateLognormalGraph(graph, graphSize, mean, stdDev, nLabels,
    nEdgeTypes)

  // Create path query
  val query1 = List(
    (Vertex("1", Map("l" -> "1"), Set()), Edge("0"))
  )
  val query2 = List(
    (Vertex("1", Map("l" -> "1"), Set()), Edge("0")),
    (Vertex("2", Map("l" -> "1"), Set()), Edge("0"))
  )
  val query3 = List(
    (Vertex("1", Map("l" -> "1"), Set()), Edge("0")),
    (Vertex("2", Map("l" -> "2"), Set()), Edge("1")),
    (Vertex("3", Map("l" -> "3"), Set()), Edge("1"))
//    (Vertex("4", Map("label" -> "4"), Set()), Edge("1")),
//    (Vertex("5", Map("label" -> "5"), Set()), Edge("0"))
  )


  // Detect user input for quit signal
  var run = 0
  var alg: ActorRef = _
  try {
    for(ln <- io.Source.stdin.getLines) {
      if (ln == "q") {
        system.shutdown() 
        throw new Exception("shutdown")
      } else if (ln == "r1") {
        // Execute path query
        // TODO: AWFUL
        try{ system.stop(alg) } catch { case _: Throwable => }
        alg = system.actorOf(Props(classOf[PathMatcher], graph, query1, ""),
          s"alg${run}")
        run += 1
      } else if (ln == "r2") {
        // Execute path query
        // TODO: AWFUL
        try{ system.stop(alg) } catch { case _: Throwable => }
        alg = system.actorOf(Props(classOf[PathMatcher], graph, query2, ""),
          s"alg${run}")
        run += 1
      } else if (ln == "r3") {
        // Execute path query
        // TODO: AWFUL
        try{ system.stop(alg) } catch { case _: Throwable => }
        alg = system.actorOf(Props(classOf[PathMatcher], graph, query3, ""),
          s"alg${run}")
        run += 1
      }
    }
  } catch { case e: Throwable => println(e.getMessage) }
}


