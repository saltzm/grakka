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
3.  Add vertices and edges to graph by:
  * Using GraphGenerator
  * Loading graph from file
  * Manually sending AddVertex and AddEdge messages
    * Adding a vertex (several ideas, need to choose):
      1. Check if vertex already exists
