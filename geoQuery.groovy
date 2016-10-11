// Get sensors with in range of a point
g.V().has("sensor", "pt::loc", Geo.inside(Geo.distance(50, 30, 10))).profile()


