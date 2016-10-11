# Loading JSON geo-spactial Data into DSE Graph to perform geo-spacial queries using Gremlin and DSE Search for indexing

A key property of some graph data is geo-spacial information and this can often arrive in a Json format. This post will show how the DSE Graph Loader can be used to load geo-spacial JSON data into DSE Graph. I'll also show how simple it is to use the DSE Search function to index and perform geo queries against the graph vertices.

First lets take a look at some input data;

Connections between sensors

{"out_id": "0","in_id": "1"}
{"out_id": "0","in_id": "2"}

One thing to note here is that the geo-point sytax. There is a space between the X and Y coordinates.

The graph we are going to load the JSON data into is pretty simple, in fact it only contains one vertex type and one edge type.

To create your graph you need to first start DSE Graph;

<DSE Home Dir>/bin/dse cassandra -k -s -g -f

Once DSE has started start the Gremlin console;

<DSE Home>/bin/dse gremlin-console

In the Gremlin console, create the ExampleGeo graph as follows;

system.graph('ExampleGeo').create()
:remote config alias g ExampleGeo.g
:load <full path>/geoSchema.groovy
schema.describe()

If you have already created the graph and simply want to erase it and start again you do this as follows;

:remote config alias g ExampleGeo.g
g.V().drop().iterate()
schema.clear()

// Create the schema with vertices and edges with associated labels and properties
 
// Properties
schema.propertyKey('pt::level').Int().create()
schema.propertyKey('pt::id').Text().create()
schema.propertyKey('pt::loc').Point().create()
schema.propertyKey('pt::time').Timestamp().create()
 
// Verticies 
schema.vertexLabel('sensor').properties('pt::level','pt::id','pt::loc','pt::time').create()

// Edges 
schema.edgeLabel('connection').connection('sensor','sensor').create()

// Index for Geo search
schema.vertexLabel('sensor').index('search').search().by('pt::loc').ifNotExists().add()

Note the index statement at the end of the schema creation the uses DSE Search to index the geo points.

This data can be loaded into DSE Graph using the DSE Graph Loader. To map he JSON data to graph schema a mapping script is used;

/* SAMPLE INPUT
{
   "pt::level": "20",
   "pt::id": "0",
   "pt::loc": "POINT(88.42 33.49)",
   "pt::time": "2016-01-11T03:15:24.000Z"
}

{
   "in_id": "20",
   "out_id": "0"
}
*/

// Configures the data loader to create the schema
config preparation: false 
config create_schema: false 
config load_new: true
config load_edge_threads: 2 
config load_vertex_threads: 2 
config batch_size: 10
 
// Defines the data input source 
inputFileDir = '/Users/davidfelcey/demos/geo-graph-test/';
senInput = File.json(inputFileDir + 'sen.json')
conInput = File.json(inputFileDir + 'con.json')

// Defines the mapping from input file and loads graph
load(senInput).asVertices {
    label "sensor"
    key "pt::id"
}

load(conInput).asEdges {
    label "connection"
    outV "out_id", {
        label "sensor"
	key "pt::id"
    }
    inV "in_id", {
	label "sensor"
	key "pt::id" 
    }
}
	

It loads the vertex and edge details and then maps and creates first the sensor verticies and then the connection edges. The following script will run this mapping script using the DSE Graph Loader;

#!/bin/bash

GRAPH_LOADER_HOME=/Users/davidfelcey/dse503/dse-graph-loader-5.0.3

$GRAPH_LOADER_HOME/graphloader geoLoadJson.groovy -graph ExampleGeo \
  -address localhost -load_failure_log load.log

Once the loading is complete geo-spacial queries can be run against the graph,

gremlin> g.V().has("sensor", "pt::loc", Geo.inside(Geo.distance(50, 30, 10))).profile()
==>Traversal Metrics
Step                                                               Count  Traversers       Time (ms)    % Dur
=============================================================================================================
DsegGraphStep([~label.=(sensor), pt::loc.inside...                                            19.009   100.00
  query-optimizer                                                                              0.600
  query-setup                                                                                  0.013
  index-query                                                                                 16.279
                                            >TOTAL                     -           -          19.009        -


I hope this brief over view of the geo-spatial features of DSE Graph has been helpful and will trigger some insteresting applications.

