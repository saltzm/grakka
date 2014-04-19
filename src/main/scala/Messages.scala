import Types._
import akka.actor.ActorRef

object Messages {
  case object Probe
  case class UpdateVertex(v: Vertex)
  case class AddAttribute(attrKey: String, attrVal: String) 
  case class RemoveAttribute(attrKey: String)
  case class AddChild(childId: Id, edge: Edge)
  case class RemoveChild(childId: Id)
  
  // GraphPartitionActor
  case class AddVertex(v: Vertex)
  case class RemoveVertex(vId: Id)
  case class SendMessageToVertex(vId: Id, message: Any) 
  case class EdgeReferenceRequest(
    requestingVertexId: Id,
    requestingVertexPartition: ActorRef,
    edgeAttributes: Set[String],
    requestedVertexId: Id)
  case class AddEdgeToVertex(vertexId: Id, childId: Id, edge: Edge)

  //GraphActor
  case class AddEdge(vertexId: Id, childId: Id, edgeAttrs: Set[String])
  case class RemoveEdge(vertexId: Id, childId: Id)
  case class AddAttributeToVertex(vertexId: Id, attrName: String, attrVal:
    String)
  case class RemoveAttributeFromVertex(vertexId: Id, attrName: String)
  case class BroadcastMessage(message: Any)
  case object StartLoading
  case object StopLoading
}

case class Vertex(id: Id, attributes: Map[String, String]) {
  def addAttribute(k: String, v: String) = Vertex(id, attributes + (k -> v))
  def removeAttribute(k: String) = Vertex(id, attributes - k)
}

case class Edge(childRef: ActorRef, attributes: Set[String])



