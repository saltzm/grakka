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
  case class SendProbeToVertex(vId: Id, probe: Probe) 
  case class EdgeReferenceRequest(
    requestingVertexId: Id,
    requestingVertexPartition: ActorRef,
    edgeAttributes: Set[String],
    requestedVertexId: Id)
  case class AddEdgeToVertex(vertexId: Id, childId: Id, edge: Edge)
}
