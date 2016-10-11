#!/bin/bash

GRAPH_LOADER_HOME=/Users/davidfelcey/dse503/dse-graph-loader-5.0.3

$GRAPH_LOADER_HOME/graphloader geoLoadJson.groovy -graph ExampleGeo \
  -address localhost -load_failure_log load.log

