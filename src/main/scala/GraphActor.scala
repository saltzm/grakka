import akka.actor.{Actor, Props, ActorRef, Stash}
import Messages._
import Types._

object GraphActor {
  def props(nPartitions: Int): Props = Props(new GraphActor(nPartitions))
}

class GraphActor(nPartitions: Int) extends Actor with Stash{
  import context._

  var partitions: IndexedSeq[ActorRef] = _

  override def preStart() = {
    partitions =  
      for (i <- 0 until nPartitions) 
      yield actorOf(GraphPartitionActor.props, i.toString)
    become(loading)
  }

  def loading: Receive = {
    case StartLoading => //already loading
    case StopLoading => 
      println("Graph done loading") 
      unstashAll()
      unbecome()

    case AddVertex(vertex) => 
      partitionFor(vertex.id) ! AddVertex(vertex)
    case RemoveVertex(v) => 
      partitionFor(v) ! RemoveVertex(v)
    case AddEdge(v, child, edgeAttrs) =>
      partitionFor(child) ! EdgeReferenceRequest(v, partitionFor(v), edgeAttrs, child) 
    // TODO: no way to remove a single edge attribute
    case RemoveEdge(v, child) => 
      partitionFor(v) ! RemoveEdge(v, child)
    case AddAttributeToVertex(v, attrKey, attrVal) =>
      partitionFor(v) ! SendMessageToVertex(v, AddAttribute(attrKey, attrVal))
    case RemoveAttributeFromVertex(v, attrKey) =>
      partitionFor(v) ! SendMessageToVertex(v, RemoveAttribute(attrKey))

    case msg => stash()
  }

  def receive = {
    case StartLoading => become(loading)
    case SendMessageToVertex(v, message) =>
      partitionFor(v) ! SendMessageToVertex(v, message)
    case BroadcastMessage(message) =>
      partitions foreach (_ ! BroadcastMessage(message))
    case msg => println("GraphActor.scala: Received unknown message $msg")
  }

  private def partitionFor(id: Id) = partitions(id % partitions.size)
}
