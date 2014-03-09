import akka.actor.{ActorRef, ActorSelection} 

object GraphMessages {
  import Graph._
  // right now assuming 1 probeMonitor per probe. if not add id
  case class Broadcast(message: AnyRef)
  case class Stop
  case class GraphFinishedLoading
  abstract class Probe (probeMonitor: ActorRef) {
    def nextSteps (v: VertexActor): List[(ActorSelection, AnyRef)]
  }
}
