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

