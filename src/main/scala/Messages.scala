import akka.actor.{ActorRef, ActorSelection} 
import Graph._

object GraphMessages {
  // right now assuming 1 probeMonitor per probe. if not add id
  case class Broadcast (message: AnyRef)
  case class StartLoading
  case class StopLoading
  case class AddVertex (v: Vertex)
  abstract class Probe (val probeMonitor: ActorRef) {
    def nextSteps (va: VertexActor): List[(ActorSelection, AnyRef)]
  }
}
