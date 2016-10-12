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

