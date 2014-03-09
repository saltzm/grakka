import akka.actor.{Actor, Props, ActorSystem, ActorRef, ActorSelection} 
import GraphMessages._
import Graph._

object GraphRunner extends Application {
  val system = ActorSystem("graphSystem")

  // create graph
  val graph = system.actorOf(Props[GraphActor], "graph")
  val query = List(
    (Vertex("1", Map("label" -> "1"), Set()), Edge("0")),
    (Vertex("2", Map("label" -> "2"), Set()), Edge("1")),
    (Vertex("3", Map("label" -> "3"), Set()), Edge("1")),
    (Vertex("4", Map("label" -> "4"), Set()), Edge("1")),
    (Vertex("5", Map("label" -> "5"), Set()), Edge("0"))
  )
  val query2 = List(
    (Vertex("1", Map("label" -> "7"), Set()), Edge("1")),
    (Vertex("2", Map("label" -> "7"), Set()), Edge("1")),
    (Vertex("3", Map("label" -> "7"), Set()), Edge("1"))
  )

  // offer choice of algorithms to run
  // user enters choice with command line
  val input = "" // TODO
  // launches actor corresponding to specific algorithm
  // that algorithm should have access to graph ActorRef and interact with the
  // graph in that way

  val algorithm = system.actorOf(Props(classOf[PathMatcher], graph, query, input), "algorithm")
  val algorithm2 = system.actorOf(Props(classOf[PathMatcher], graph, query2,
    input), "algorithm2")
  try {
    for(ln <- io.Source.stdin.getLines) {
      if (ln == "q") {
        system.shutdown() 
        throw new Exception("quitting")
      }
    }
  } catch {
    case _: Throwable => println("shutdown")
  }

}


