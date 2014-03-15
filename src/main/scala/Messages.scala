import akka.actor.{ActorRef, ActorSelection} 
import Graph._

object GraphMessages {
  // right now assuming 1 probeMonitor per probe. if not add id
  case object StartLoading
  case object StopLoading
  case object VertexReady
  case object GraphFinishedLoading
  case object PrepareForBSPAlgorithm
  case object GetNumberOfVertices
  case class NumberOfVertices (n: Int)
  case class Broadcast (message: AnyRef)
  case class AddVertex (v: Vertex)
  abstract class Probe (val probeMonitor: ActorRef) {
    def nextSteps (va: VertexActor): List[(ActorRef, AnyRef)]
  }
 /* abstract class BSPProbe (val probeMonitor: ActorRef) extends Probe{*/
    //def voteToHalt (va: VertexActor): Boolean
    //def  
  //}

}
