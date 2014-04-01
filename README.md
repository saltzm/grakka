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
      1. GraphActor broadcasts the AddEdge(fromVertexId, toVertexId) to the partitions
      2. The partition 
