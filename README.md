grakka
======

A Reactive Multiagent Framework for Distributed Graph Mining

Basic idea: Treat the graph as an environment that can be analyzed and explored
by Probes. Each vertex is an actor that contains a bin where Probes can leave
and pick up things, and it can pass Probes along to other vertices or actors
(like result collectors) by executing the nextStep function of the probe on
itself. (I know this sounds evil but it won't break encapsulation, don't worry.)
To define a new graph algorithm, it's only necessary to define a new Probe and
an actor that sends them and handles their responses. (For some algorithms, like
a path query algorithm, this is fairly easy. For others, maybe not so much. The
plan is that I'll provide the implementations of some more complex and common
algorithms here later on. I'm starting with path queries. By default, everything
is asynchronous, but BSP style algorithms can be implemented as well. I'll found
out later how effectively.) Since the behavior of the algorithm is contained
external to the vertices themselves (unlike Pregel, for example), the graph can
remain online in between the execution of different algorithms. Furthermore, it
is possible (though probably very slow, I'll need to test this) to run multiple
algorithms on the graph as the same time through the use of algorithm ids on the
probes.  The whole thing will be in-memory and distributed. Now let's get into
the juice:

Note: The following ideas are pretty preliminary. The code I have so far was a
quick prototype I whipped up, not distributed, and not fault-tolerant. But it
did work for path queries on up to 200,000 nodes on my Macbook Air with 2G of
RAM pretty quickly. So now I'm trying to make it better and actually a real
thing. Here ya go.

Actor Hierarchy
* GraphRunner
  * GraphActor
    * GraphPartitionActor
      * VertexActor 
  * GraphAlgorithm

GraphRunner Responsibilities:
* Starting the graph
* Interpreting commands and running algorithms
* Handling failure from either GraphActor or GraphAlgorithm

GraphActor Responsibilities:
* State: Partition refs
* Start and handle failure from partitions
* AddVertex(vertex: Vertex) 
* AddEdge(fromVertexId: Int, toVertexId: Int, edge: Edge) // Edge is just a container for attributes (right now)
* RemoveVertex(vertexId: Int)
* RemoveEdge(fromVertexId: Int, toVertexId: Int, edge: Edge)
* AddAttributeToVertex(vertexId: Int, attrName: String, attrValue: String)
* RemoveAttributeFromVertex(vertexId: Int, attrName: String) // Should this complain if the vertex doesn't exist?
* SendProbeToVertex(vertexId: Int, probe: Probe) // May make this any message and not just probe but that causes issues Vertex-side
* BroadcastProbeToVertices(probe: Probe) 

GraphPartitionActor Responsibilities:
* State: Map from vertexId to ActorRef. If a vertex is removed, depending on
  whether or not there are issues with respawning an actor with the same id
  (need to read back in the docs), I may add an "active" flag here so that the
  actor pointed to by the ActorRef can keep on existing (I don't like this at
  all) even when it's not in the graph, in case another one with the same id is
  added. (I don't think I'll do this, but I'm just writing it here to remember.)
* GetVertexRef(vertexId: Int) // May throw VertexDoesNotExist exception 
* SendProbeToVertex(vertexId: Int, probe: Probe) // May throw VertexDoesNotExist exception 
* Forward on appropriate messages from GraphActor to VertexActor
  (Add/Remove Attr to/from Vertex)
* Handle failures from VertexActor (Exceptions on removing attrs, if I decide to
  keep that)

VertexActor Responsibilities:
* State: 
  * Map from attribute names to attribute values.  Things like "name" ->
  "Matthew", "city" -> "Lyon" and so on.
  * Set of children ActorRefs, that it watches and handles on termination
  * A bin (data structure to be determined. probably a map from algorithm name
    to a value) for probes to leave and pick up things
* Handle adding/removing attributes/throwing exception if attribute to remove
  doesn't exist  
* Receive probes and execute their behavior to:
  1. Add things to the bin
  2. Send their generated messages to their desired destinations


General Execution Pattern:

1. GraphRunner starts
2. GraphActor starts
  * Starts m GraphPartitionActors (or 1, and resizes later on, need to decide)
    * Each GraphPartitionActor instantiates an empty Map from vertex id to ActorRef that it will use to determine if it contains a vertex and to send messages to a vertex
3.  Add vertices and edges to graph by:
  * Using GraphGenerator
  * Loading graph from file
  * Manually sending AddVertex and AddEdge messages
    * Adding a vertex (several ideas, need to choose):
      1. Vertex assigned to partition based on *id % (# of partitions)*. GraphActor messages the partition based on the hash to determine if a vertex with its id already exists
      2. Collects responses from the partitions
        * If one of them responds positively, send the new vertex to that one to update (Map-style add, replaces old value without complaining, also possible to distinguish between add and update, decide later)
        * If none respond positively, send the new vertex to the next GraphPartitionActor to be added (in a round robin manner, keeping partitions balanced)
    * Adding an edge 
      1. GraphActor sends GetVertexRef(toVertexId) message to the correct partition 
        * In the case that the vertex does not exist, either (decide): throw VertexDoesNotExist exception in the partition, or send a VertexDoesNotExist message to the graph.  The first makes a bit more sense, I think, as the failure handling strategy should be encapsulated in the supervisor strategy. Need to decide if trying to add an invalid edge is enough to shut the program down or if it should just log an error and keep going. Need to further consider use cases, whether or not users can add edges at runtime via command line (in which case a simple error message might be nice, without shutting down), etc.
      3. If it succeeds, receives a VertexRef(vertId, end an AddEdge(fromVertexId, toVertexRef) to the partition with fromVertexId (based on a hash)
      2. If 
