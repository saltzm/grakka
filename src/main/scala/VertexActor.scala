import akka.actor.{Actor, Props, ActorRef, Terminated}
import Types._
import Messages._

case class Vertex(id: Id, attributes: Map[String, String]) {
  def addAttribute(k: String, v: String) = Vertex(id, attributes + (k -> v))
  def removeAttribute(k: String) = Vertex(id, attributes - k)
}

case class Edge(childRef: ActorRef, attributes: Set[String])

object VertexActor {
 def props(v: Vertex): Props = Props(new VertexActor(v))
}

class VertexActor(var vertex: Vertex) extends Actor {
  // Place for probes to drop off and pick up stuff
  // Map key: the id of an algorithm, i.e. "alg1"
  // Map value: a map mapping from attribute name to value
  var bin = Map[String, Map[String, Any]]()

  // Assuming for now Edge contains destination ActorRef and some attributes.
  // Subject to change. Alternative could be (ActorRef, Edge) tuple with Edge
  // just being a container for attributes. Mapping from childId to the Edge
  var children = Map[Id, Edge]() 

  def receive = {
    case Probe => //TODO
    case UpdateVertex(v) => vertex = v 

    case AddAttribute(attrKey, attrVal) =>
      vertex = vertex.addAttribute(attrKey, attrVal)
      
    case RemoveAttribute(attrKey) => 
      vertex = vertex.removeAttribute(attrKey)

    case AddChild(childId, edge) => 
      children += (childId -> edge)
      context watch edge.childRef

    case RemoveChild(childId) => children -= childId 
    // TODO: This seems inefficient since it has to loop through all of the
    // children rather than taking advantage of the fact that I know that
    // there's only one child for a given ActorRef
    // Since we're only watching the children of this vertex, we can assume the
    // Terminated message is coming from a child. When the child terminates (if
    // it is removed from the graph, for example), we simply remove it from
    // our children
    case Terminated(childRef) => children = children.filter { 
      case (_, Edge(ref, _)) => ref != childRef
    }
  }
}
