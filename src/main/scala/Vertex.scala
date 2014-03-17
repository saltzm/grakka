import akka.actor.{Actor, Props, ActorSystem, ActorRef, ActorSelection}
import GraphMessages._

object Graph {
  type Id = String
  case class Edge (attribute: String)
  case class Vertex (id: Id, attributes: Map[String, String], 
    children: Set[(Id, Edge)]) 
}

class VertexActor (val vertex: Graph.Vertex) extends Actor {

  def children = vertex.children.map { case (cid, e) =>
    (context.actorSelection(s"../${cid}"), e)
  }

  def receive = {
    case p: Probe =>
      p.nextSteps(this).foreach { case (destination, message) =>
        destination ! message
      }
    case _ => println("Unknown message")
  }
}
