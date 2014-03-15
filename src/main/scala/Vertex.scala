import akka.actor.{Actor, Props, ActorSystem, ActorRef, ActorSelection, Stash,
Identify, ActorIdentity}
import GraphMessages._
import Graph._

object Graph {
  type Id = String
  case class Edge (attribute: String)
  case class Vertex (id: Id, attributes: Map[String, String], 
    children: Set[(Id, Edge)]) 
}

class VertexActor (val vertex: Graph.Vertex) extends Actor with Stash {

  def handleProbe (p: Probe) = p.nextSteps(this).foreach { 
    case (destination, message) => destination ! message
  }

  var childRefs: Set[(ActorRef, Edge)] = _
  override def preStart() = {
    context.become(loading)
    childRefs = Set[(ActorRef, Edge)]()
  }
  
  def loading: Receive = {
    case GraphFinishedLoading =>
      // get only distinct cids if there can be more than one with a different
      // edge
      vertex.children match {
        case c if c.isEmpty => context.parent ! VertexReady
        case c => c.map(_._1).foreach ( cid =>
          context.actorSelection(s"../${cid}") ! Identify(cid)
        )
      }
    case ActorIdentity(cid, Some(ref)) => 
      println("ref: " + ref)
     // TODO: assumes can be more than one edge per id 
     // TODO: THIS IS THE CASE WITH CURRENT GENERATOR
      vertex.children.filter(_._1 == cid).foreach {
        case (_, edge) => childRefs += ((ref, edge))
      }
      // TODO: send VertexReady message to Graph, potentially
      if (childRefs.size == vertex.children.size) {
        context.parent ! VertexReady
        context.unbecome()
      }
    case msg => stash()
  }

  def receive = {
    // TODO: case PrepareForAlgorithm (r: Receive) => context.become(r)
    case p: Probe => handleProbe(p)
    //case PrepareForBSPAlgorithm => context.become(bsp)
    case x => println(s"Vertex.scala: Unknown message $x")
  }

/*  def bsp: Receive = {*/
    //case p: Probe => 
      //handleProbe(p) //TODO: For all probes CURRENTLY in inbox
      //become(waitingForSuperstep)
    //case PrepareForBSPAlgorithm => println("Vertex.scala: Already in BSPAlgorithm!")
    //case _ => println("Vertex.scala: Unknown message")
  //}

  //def waitingForSuperStep: Receive = {

  /*}*/
}
