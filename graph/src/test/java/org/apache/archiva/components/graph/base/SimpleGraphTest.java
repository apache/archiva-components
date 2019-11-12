package org.apache.archiva.components.graph.base;
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.archiva.components.graph.api.Edge;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimpleGraphTest {

    @Test
    void createNewVertex() {
        SimpleGraph graph = new SimpleGraph();
        SimpleVertex vtx = graph.createNewVertex();
        assertNotNull(vtx);
        assertNotNull(vtx.getId());
        assertNotNull(java.util.UUID.fromString(vtx.getId()));
    }

    @Test
    void createNewEdge() {
        SimpleGraph graph = new SimpleGraph();
        SimpleVertex vtx1 = graph.createNewVertex();
        SimpleVertex vtx2 = graph.createNewVertex();
        Edge<SimpleVertex> edge = graph.createNewEdge(vtx1, vtx2);
        assertNotNull(edge);
        assertNotNull(edge.getId());
        assertNotNull(java.util.UUID.fromString(edge.getId()));
        assertEquals(vtx1, edge.getSource());
        assertEquals(vtx2, edge.getDestination());
    }


    @Test
    void removeEdge() {
        SimpleGraph graph = new SimpleGraph();
        SimpleVertex vtx1 = graph.createNewVertex();
        SimpleVertex vtx2 = graph.createNewVertex();
        Edge<SimpleVertex> edge = graph.createNewEdge(vtx1, vtx2);

        graph.removeEdge(edge);

        assertEquals(0, vtx1.getInEdges().size());
        assertEquals(0, vtx2.getInEdges().size());
        assertEquals(0, vtx1.getOutEdges().size());
        assertEquals(0, vtx2.getOutEdges().size());

    }

    @Test
    void removeVertex() {
    }
}