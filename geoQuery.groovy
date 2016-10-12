// Get sensors with in range of a point - Geo.distance is (x,y,radius)
g.V().hasLabel('sensor').out('hasPoint').has('loc', Geo.inside(Geo.distance(10,30,50)))
// Get sensors that cover a certain area inside another area 
g.V().hasLabel('sensor').out('hasRegion').has('reg', Geo.inside(Geo.polygon(0.0,0.0,0.0,100.0,100.0,100.0,100.0,0.0)))

