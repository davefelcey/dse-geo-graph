# Loading JSON geo-spactial Data into DSE Graph to perform geo-spacial queries using Gremlin and DSE Search for indexing

A key property of some graph data is geo-spacial information and this can often arrive in a Json format. This example shows how the DSE Graph Loader can be used to load geo-spacial JSON data into DSE Graph. It also shows how simple it is to use the DSE Search function to index and perform geo queries against the graph vertices.

First lets take a look at some input data;

Sensor to point reading connection;
```
{"name": "PT4450","id": "0"}
```
Sensor to region reading connection;
```
{"name": "RG4450","id": "1"}
```
Sensors;
```
"name": "PT4450"}
{"name": "RG4450"}
```
Point reading;
```
{"id": "0","loc": "POINT(25.955 15.187)","time": "2016-01-11T03:15:24.000Z","level": "34"}
```
Region reading;
```
{"id": "1","reg": "POLYGON((0 0, 0 10, 10 10, 10 00, 0 0))","time": "2016-01-11T03:15:24.000Z","level": "34"}
```
One thing to note here is that the geo-point sytax. There is a space between the X and Y coordinates.

The graph we are going to load the JSON data into is pretty simple, in fact it only contains one vertex type and one edge type.

To create your graph you need to first start DSE Graph;

```
<DSE Home Dir>/bin/dse cassandra -k -s -g -f
```

Once DSE has started start the Gremlin console;

```
<DSE Home>/bin/dse gremlin-console
```

In the Gremlin console, create the ExampleGeo graph as follows;

```
system.graph('ExampleGeo').create()
:remote config alias g ExampleGeo.g
:load <full path>/geoSchema.groovy
schema.describe()
```

If you have already created the graph and simply want to erase it and start again you do this as follows;

```
:remote config alias g ExampleGeo.g
g.V().drop().iterate()
schema.clear()
system.graph('ExampleGeo').drop()
```

Here is the graph schema that will be created by the load command above.
```
// Create the schema with vertices and edges with associated labels and properties
 
// Properties
schema.propertyKey('level').Int().create()
schema.propertyKey('name').Text().create()
schema.propertyKey('id').Text().create()
schema.propertyKey('loc').Point().create()
schema.propertyKey('reg').Polygon().create()
schema.propertyKey('time').Timestamp().create()
 
// Verticies 
schema.vertexLabel('sensor').properties('name').create()
schema.vertexLabel('locReading').properties('level','id','loc','time').create()
schema.vertexLabel('regReading').properties('level','id','reg','time').create()

// Edges 
schema.edgeLabel('hasPoint').connection('sensor','locReading').create()
schema.edgeLabel('hasRegion').connection('sensor','regReading').create()

// Index for Geo search
schema.vertexLabel('locReading').index('search').search().by('loc').ifNotExists().add()
// Indexes on polygons are not supported at the moment 
// schema.vertexLabel('regReading').index('search').search().by('reg').ifNotExists().add()
```
And here is the loading and mapping script used by the DSE Graph Loader;
```
// Configures the data loader to create the schema
config preparation: false 
config create_schema: false 
config load_new: true
config load_edge_threads: 2 
config load_vertex_threads: 2 
config batch_size: 10
 
// Defines the data input source 
inputFileDir = '/Users/davidfelcey/demos/dse-geo-graph/';
senInput = File.json(inputFileDir + 'sen.json')
senPtInput = File.json(inputFileDir + 'sen_pt.json')
senRegInput = File.json(inputFileDir + 'sen_reg.json')
conPtInput = File.json(inputFileDir + 'con_pt.json')
conRegInput = File.json(inputFileDir + 'con_reg.json')

// Defines the mapping from input file and loads graph

load(senInput).asVertices {
    label "sensor"
    key "name"
}

load(senPtInput).asVertices {
    label "locReading"
    key "id"
}

load(senRegInput).asVertices {
    label "regReading"
    key "id"
}

load(conPtInput).asEdges {
    label "hasPoint"
    outV "name", {
        label "sensor"
	key "name"
    }
    inV "id", {
	label "locReading"
	key "id" 
    }
}
	
load(conRegInput).asEdges {
    label "hasRegion"
    outV "name", {
        label "sensor"
	key "name"
    }
    inV "id", {
	label "regReading"
	key "id" 
    }
}
```	

It loads the verticies and edges and then maps readings to sesnors. The following script will run this mapping script using the DSE Graph Loader. However, before you run it make sure the paths in the load.sh and geoLoadJson.groovy scripts reflect those in your environment.

```
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

```

I hope this brief over view of the geo-spatial features of DSE Graph has been helpful and will trigger some insteresting applications.

