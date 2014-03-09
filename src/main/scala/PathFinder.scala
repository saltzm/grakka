import akka.actor.{Actor, Props, ActorSystem, ActorRef, ActorSelection} 
import Graph._
import GraphMessages._

class PathMatcher (graph: ActorRef, query: List[(Vertex, Edge)], input: String) 
  extends Actor {
  // convert input string into query data structure (too much for the moment)

  // launches intermediate result collectors? nah, not right now, but possible
  // sends probes to all nodes in graph
  override def preStart() = {
    graph ! Broadcast(PathQueryProbe(query, List[Vertex]()))
    println("PathFinder loading complete")
  }

  var numberOfMessagesReceived = 0
  def receive = {
    case Stop => println("PathFinder stopping"); context.stop(self)
    case mat: List[Vertex] => 
      numberOfMessagesReceived += 1
      if (numberOfMessagesReceived < 10)
        println(s"${self.path}: ${mat.reverse}") // TODO
        //print(numberOfMessagesReceived + " ")
  }

  /**
    * Creates Probe class
    */
  case class PathQueryProbe (
    queryPath: List[(Vertex, Edge)], 
    matchSoFar: List[Vertex]  // TODO: could make more efficient by using id
  ) extends GraphMessages.Probe (self) {
    import Graph._

    // vcf: vertex comparison function
    private def vcf (qv: Vertex, v2: Vertex): Boolean =
      qv.attributes.toSet.subsetOf(v2.attributes.toSet) // TODO

    // creates probe nextStep function 
    // check if the vertex label matches first vertex in the path
    // if so, send the probe to all of the children vertices except with the
    // current vertex id added to the cargo and a new query without the first
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
    
  // waits for incoming cargo (Option[AnyRef])
}
