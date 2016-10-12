// Get sensors with in range of a point
g.V().has("sensor", "loc", Geo.inside(Geo.distance(50, 25, 15)))
// Get sensors that cover a certain area 
g.V().has("sensor", "loc", Geo.inside(Geo.distance(50, 25, 15)))


