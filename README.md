grakka
======

A Reactive Multiagent Framework for Distributed Graph Mining

Note: The following ideas are pretty preliminary.

Actor Hierarchy
* GraphRunner
  * GraphActor
    * GraphPartitionActor
      * VertexActor 
  * GraphAlgorithm

Example with two partitions and m vertices:
* GraphActor
  * GraphPartitionActor 1
    * VertexActor 1
    * VertexActor 2
    * ... 
    * VertexActor m
  * GraphPartitionActor 2
    * VertexActor m + 1
    * VertexActor m + 2
    * ...
    * VertexActor m

General Execution Pattern:

1.  GraphRunner starts
2.  GraphActor starts
  * Starts m GraphPartitionActors (or 1, and resizes later on, need to decide)
    * Each GraphPartitionActor instantiates an empty Map from vertex id to ActorRef that it will use to determine if it contains a vertex and to send messages to a vertex
3.  Add vertices and edges to graph by:
  * Using GraphGenerator
  * Loading graph from file
  * Manually sending AddVertex and AddEdge messages
    * Adding a vertex (several ideas, need to choose):
      1. Vertex assigned to partition based on *id % (# of paritions)*. GraphActor messages the partition based on the hash to determine if a vertex with its id already exists
      2. Collects responses from the partitions
        * If one of them responds positively, send the new vertex to that one to update (Map-style add, replaces old value without complaining, also possible to distinguish between add and update, decide later)
        * If none respond positively, send the new vertex to the next GraphPartitionActor to be added (in a round robin manner, keeping partitions balanced)
    * Adding an edge 
      1. GraphActor sends GetVertexRef(toVertexId) message to the correct partition 
      2. In the case that the vertex does not exist, either (decide): throw VertexDoesNotExist exception in the partition, or send a VertexDoesNotExist message to the graph.  The first makes a bit more sense, I think, as the failure handling strategy should be encapsulated in the supervisor strategy. Need to decide if trying to add an invalid edge is enough to shut the program down or if it should just log an error and keep going. Need to further consider use cases, whether or not users can add edges at runtime via command line (in which case a simple error message might be nice, without shutting down), etc.
      3. n AddEdge(fromVertexId, toVertexId) to the partition with fromVertexId (based on a hash)
      2. If 
