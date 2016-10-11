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
	
