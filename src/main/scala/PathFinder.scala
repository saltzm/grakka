import akka.actor.{Actor, Props, ActorSystem, ActorRef, ActorSelection} 
import Graph._
import GraphMessages._

class PathMatcher (graph: ActorRef, query: List[(Vertex, Edge)], input: String) 
  extends Actor {
  // convert input string into query data structure (too much for the moment)

  // launches intermediate result collectors? nah, not right now, but possible
  // sends probes to all nodes in graph
  var start: Long = _
  override def preStart() = {
    graph ! Broadcast(PathQueryProbe(query, List[Vertex]()))
    start = System.currentTimeMillis()
    println("PathFinder loading complete")
  }

  var numberOfMessagesReceived = 0
  def receive = {
    case mat: List[Vertex] => 
      numberOfMessagesReceived += 1
      val formattedMessage = mat.reverse.map(v=> (v.id, "label: " + v.attributes("label")))
      if (numberOfMessagesReceived < 10)
        println(s"${self.path}: $formattedMessage") // TODO
      else if (numberOfMessagesReceived % 100 == 0 && numberOfMessagesReceived <
      1000)
        print(numberOfMessagesReceived + " at " + (System.currentTimeMillis() -
        start) + "ms, ")
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
    def nextSteps (va: VertexActor): List[(ActorSelection, AnyRef)]  = {
      val (qVert, qEdge) = queryPath.head
      if (vcf(qVert, va.vertex)) { // query head matches current vertex
        // finished with success
        // TODO: GROSS with actorSelection on self
        if (queryPath.tail.isEmpty) 
          List((context.actorSelection(self.path), va.vertex::matchSoFar)) 
        else {
          //need list of children where the edge going to that child matches the
          //edge in the head of the queryPath
          //TODO maybe dangerous to call va.children, but i think since the
          //nextSteps function is always called from inside the VertexActor, it
          //shouldn't break encapsulation
          val childrenWithMatchingEdges = va.children.collect { 
            case (cid, edge) if qEdge == edge => cid
          } 
          // TODO: gross toList
          childrenWithMatchingEdges.toList.map ( c =>  
            (c, PathQueryProbe(queryPath.tail, va.vertex::matchSoFar))
          )
        }
      } else List() // finish with no match found
    }
  }
}
