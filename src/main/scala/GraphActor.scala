import akka.actor.{Actor, Props, ActorSystem, ActorRef, ActorSelection, Stash} 
import Graph._
import GraphMessages._
import GraphGenerator._


class GraphActor extends Actor with Stash {
  import context._

  var readyVertices: Int = _
  override def preStart() = {
    become(loading)
    readyVertices = 0
  }

  def loading: Receive = {
    // TODO: Doesn't enforce that children already exist in the graph
    // This is to make graph generation easier/faster, for the moment
    // TODO: Doesn't check if vertex already exists in the graph
    case StartLoading => //already loading
    case AddVertex(v: Vertex) =>
      val child = actorOf(Props(classOf[VertexActor], v), v.id)
    case VertexReady =>
      readyVertices += 1
      println("readyVertices: " + readyVertices)
      if (readyVertices == children.size) { 
        println("All vertices ready. Graph done loading") 
        unbecome()
      }
    case StopLoading => 
      children.foreach (_ ! GraphFinishedLoading)
      println("Finished creating vertex actors")
    case msg => stash()
  }

  def receive = {
    case GetNumberOfVertices => sender ! NumberOfVertices(children.size)
    case StartLoading => become(loading)
    case Broadcast(message) => children.foreach (_ ! message)
    // TODO: maybe this should just work... 
    case AddVertex(_) => 
      println("REQUESTED TO ADD VERTEX WHEN NOT IN LOADING PHASE!!!")
  }
}
