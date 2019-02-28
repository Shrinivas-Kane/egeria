/* SPDX-License-Identifier: Apache 2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.governanceservers.openlineage.connectors;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.attribute.Geo;
import org.janusgraph.core.attribute.Geoshape;
import org.janusgraph.example.GraphOfTheGodsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;
import java.util.Map;

public class GremlinConnector {


    private static final Logger log = LoggerFactory.getLogger(GremlinConnector.class);

    @Value("classpath:conf//janusgraph-berkeleyje-lucene.properties")
    Resource resourceFile;

    private String storageDirectory ;

    public GremlinConnector(){

        log.error(this.getClass().getClassLoader().getResource("application.properties").getFile());



        JanusGraph graph = JanusGraphFactory.open("application2.properties");
        GraphTraversalSource g = graph.traversal();
        if (g.V().count().next() == 0) {
            // load the schema and graph data
            GraphOfTheGodsFactory.load(graph);
        }
        Map<Object, Object> saturnProps = g.V().has("name", "saturn").valueMap(true).next();
        log.info(saturnProps.toString());
        List<Edge> places = g.E().has("place", Geo.geoWithin(Geoshape.circle(37.97, 23.72, 50))).toList();
        log.info(places.toString());
    }
}
