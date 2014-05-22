import akka.actor.{Props, Actor, ActorRef}
import Types._
import Messages._
import Exceptions._

object GraphPartitionActor {
  def props: Props = Props[GraphPartitionActor]
}

class GraphPartitionActor extends Actor {
  // Map from the id of a vertex to its ActorRef  
  var vertexRefMap = Map[Id, ActorRef]()

  def receive = {
    case AddVertex(v) => addVertex(v)

    case RemoveVertex(vId) => 
      //TODO: is there a problem respawning actor with same id later?
      context.stop(vertexRefMap(vId))
      vertexRefMap -= vId

    case SendMessageToVertex(vId, message) => sendMessageToVertex(vId, message)
    case BroadcastMessage(message) => 
      for ((_, ref) <- vertexRefMap) ref ! message
    case EdgeReferenceRequest(
        requestingVertId, 
        requestingVertPartition,
        edgeAttrs, 
        requestedVertId) =>
      processEdgeReferenceRequest(requestingVertId, requestingVertPartition, 
        edgeAttrs, requestedVertId)

    case AddEdgeToVertex(vId, childId, edge) => 
      addEdgeToVertex(vId, childId, edge)

    case RemoveEdge(v, childId) => vertexRefMap(v) ! RemoveChild(childId)
  }

  // TODO: Double-check ActorRef equality
  private def processEdgeReferenceRequest(
      requestingVertId: Id, 
      requestingVertPartition: ActorRef, 
      edgeAttrs: Set[String], 
      requestedVertId: Id) =  
    if (requestingVertPartition == self) 
      addEdgeToVertex(requestingVertId, requestedVertId,
        Edge(vertexRefMap(requestedVertId), edgeAttrs))
    else 
      requestingVertPartition ! AddEdgeToVertex(requestingVertId, 
        requestedVertId, Edge(vertexRefMap(requestedVertId), edgeAttrs))

  private def addVertex(v: Vertex): Unit = 
    vertexRefMap.get(v.id) match {
      case None =>
        val vRef = context.actorOf(VertexActor.props(v), s"${v.id}")
        vertexRefMap += (v.id -> vRef)
      case Some(existingVertexRef) =>
        existingVertexRef ! UpdateVertex(v)
    }

  private def sendMessageToVertex(vId: Id, message: Any): Unit = 
    vertexRefMap.get(vId) match {
      case None => throw new VertexDoesNotExistException(
        s"GraphPartitionActor.scala, VertexDoesNotExistException: Tried to send " +
        s"$message to vertex $vId, which does not exist")
      case Some(vRef) => vRef ! message
    }

  private def addEdgeToVertex(fromVertId: Id, childId: Id, edge: Edge) = 
    vertexRefMap(fromVertId) ! AddChild(childId, edge)
}