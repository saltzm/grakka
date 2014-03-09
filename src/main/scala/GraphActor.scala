import akka.actor.{Actor, Props, ActorSystem, ActorRef, ActorSelection} 

class GraphActor extends Actor {
  import Graph._
  import GraphMessages._
  import GraphGenerator._

  val graphSize = 100000

  override def preStart() = {
    for (v <- generateRandomGraph(graphSize, 20, 10, 2)) 
      context.actorOf(Props(classOf[VertexActor], v), v.id)

    println("done starting vertices")
//    context.parent ! GraphFinishedLoading
  }

  def receive = {
    case Broadcast(message) => context.children.foreach (_ ! message)
    case Stop => println("Graph stopping"); context.stop(self)
    case _ => println("i love garbage")
  }
}
