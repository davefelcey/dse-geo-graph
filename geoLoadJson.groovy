/* SAMPLE INPUT
// Point reading
{
   "level": "20",
   "id": "0",
   "loc": "POINT(88.42 33.49)",
   "time": "2016-01-11T03:15:24.000Z"
}
// Region reading 
{
   "level": "20",
   "id": "1",
   "region": "POLYGON(1.0 2.0 5.0 4.0)",
   "time": "2016-01-11T03:15:24.000Z"
}
// Sensor 
{
   "name": "FRT556Y"
}
// Sensor reading connections 
{
   "name": "FRT556Y",
   "id": "0"
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
	
