import akka.actor.{Actor, Props, ActorSystem, ActorRef, ActorSelection} 
import Graph._
import GraphMessages._

class PathMatcher (graph: ActorRef, query: List[(Vertex, Edge)], input: String) 
  extends Actor {
  case class PathMatch (mat: List[Vertex])
  case class NewProbesCreated (n: Int)
  case object ProbeFinished
  // convert input string into query data structure (too much for the moment)

  // launches intermediate result collectors? nah, not right now, but possible
  // sends probes to all nodes in graph
  var start: Long = _
  var numberOfOutstandingProbes = 0
  var numberOfMatches = 0
  override def preStart() = {
    graph ! GetNumberOfVertices
    graph ! Broadcast(PathQueryProbe(query, List[Vertex]()))
    start = System.currentTimeMillis()
    println("PathFinder loading complete")
  }

  def decrementProbes: Unit = {
    numberOfOutstandingProbes -= 1
    if (numberOfOutstandingProbes <= 0) {
      println(s"\nFinished with $numberOfMatches matches and " +
        s"$numberOfOutstandingProbes outstanding probes at " +
        s"${System.currentTimeMillis() - start} ms")
      context.stop(self)
    }
  }

  def receive = {
    case PathMatch(mat) => 
      numberOfMatches += 1
      val formattedMessage = mat.reverse.map(v=> (v.id, "label: " + v.attributes("l")))
      if (numberOfMatches < 10)
        println(s"${self.path}: $formattedMessage") // TODO
      else if (numberOfMatches % 100 == 0)
        print(numberOfMatches + " at " + (System.currentTimeMillis() -
        start) + "ms, ")
      decrementProbes
    case NewProbesCreated(n: Int) => 
      numberOfOutstandingProbes += n
    case ProbeFinished => 
      decrementProbes
    case NumberOfVertices(n: Int) => 
      numberOfOutstandingProbes += n
  }

  /**
    * Creates Probe class
    * TODO: could make matchSoFar more efficient by using id
    */
  case class PathQueryProbe (queryPath: List[(Vertex, Edge)], 
    matchSoFar: List[Vertex]) extends Probe (self) {

    // vcf: vertex comparison function
    // TODO make more efficient
    private def vcf (qv: Vertex, v2: Vertex): Boolean =
      qv.attributes.toSet.subsetOf(v2.attributes.toSet) 

    // Checks if the vertex label matches first vertex in the path
    // If so, sends the probe to all of the children vertices except with the
    // current vertex id added to the matchSoFar and a new query without the first
    // node in the path
    def nextSteps (va: VertexActor): List[(ActorRef, AnyRef)] = {
      val (qVert, qEdge) = queryPath.head
      if (vcf(qVert, va.vertex)) { // query head matches current vertex
        // finished with success
        if (queryPath.tail.isEmpty) 
          List((self, PathMatch(va.vertex::matchSoFar))) 
        else {
          //need list of children where the edge going to that child matches the
          //edge in the head of the queryPath
          //TODO maybe dangerous to call va.children, but i think since the
          //nextSteps function is always called from inside the VertexActor, it
          //shouldn't break encapsulation
          val childrenWithMatchingEdges = va.childRefs.collect { 
            case (cid, edge) if qEdge == edge => cid
          } 
          // TODO: gross toList
          val childMessages = childrenWithMatchingEdges.toList.distinct.map ( c =>  
            (c, PathQueryProbe(queryPath.tail, va.vertex::matchSoFar))
          )
          // -1 is because it itself is finishing
          childMessages match {
            case List() =>
            println(probeMonitor.path)
            List((probeMonitor, ProbeFinished))
            case _ => 
              (self, NewProbesCreated(childMessages.size - 1))::childMessages 
          }
        }
      } else { println(probeMonitor.path); List((probeMonitor, ProbeFinished))} // finish with no match found
    }
  }
}
