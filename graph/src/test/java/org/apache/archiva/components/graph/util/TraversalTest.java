package org.apache.archiva.components.graph.util;
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

import org.apache.archiva.components.graph.api.TraversalStatus;
import org.apache.archiva.components.graph.base.SimpleGraph;
import org.apache.archiva.components.graph.base.SimpleVertex;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TraversalTest {

    private static final Logger log = LoggerFactory.getLogger(TraversalTest.class);

    @Test
    void depthFirstWithoutCycleAndWithoutError() {
        SimpleGraph graph = new SimpleGraph();
        SimpleVertex vtx0 = graph.addVertex("root");
        SimpleVertex vtx1 = graph.addVertex("vertex1");
        SimpleVertex vtx11 = graph.addVertex("vertex11");
        SimpleVertex vtx12 = graph.addVertex("vertex12");
        SimpleVertex vtx2 = graph.addVertex("vertex2");
        SimpleVertex vtx21 = graph.addVertex("vertex21");
        SimpleVertex vtx211 = graph.addVertex("vertex211");
        SimpleVertex vtx212 = graph.addVertex("vertex212");

        SimpleVertex vtx22 = graph.addVertex("vertex22");
        SimpleVertex vtx221 = graph.addVertex("vertex221");
        SimpleVertex vtx222 = graph.addVertex("vertex222");
        SimpleVertex vtx223 = graph.addVertex("vertex223");

        SimpleVertex vtx3 = graph.addVertex("vertex3");
        SimpleVertex vtx31 = graph.addVertex("vertex31");
        SimpleVertex vtx32 = graph.addVertex("vertex32");
        SimpleVertex vtx33 = graph.addVertex("vertex33");

        graph.addEdge("root->1", vtx0, vtx1);
        graph.addEdge("root->2", vtx0, vtx2);
        graph.addEdge("root->3", vtx0, vtx3);

        graph.addEdge("1->11", vtx1, vtx11);
        graph.addEdge("1->12", vtx1, vtx12);

        graph.addEdge("2->21", vtx2, vtx21);
        graph.addEdge("2->22", vtx2, vtx22);

        graph.addEdge("21->211", vtx21, vtx211);
        graph.addEdge("21->212", vtx21, vtx212);

        graph.addEdge("22->221", vtx22, vtx221);
        graph.addEdge("22->222", vtx22, vtx222);
        graph.addEdge("22->223", vtx22, vtx223);

        graph.addEdge("3->31", vtx3, vtx31);
        graph.addEdge("3->32", vtx3, vtx32);
        graph.addEdge("3->33", vtx3, vtx33);

        List<SimpleVertex> visitedNodes = new ArrayList<>();
        TraversalStatus<SimpleVertex> status = Traversal.depthFirst(vtx0, v -> {
            log.debug("Visiting vertex " + v);
            if (visitedNodes.contains(v)) {
                throw new RuntimeException("Double visit of vertex: " + v);
            }
            visitedNodes.add(v);
            return true;
        }, true);

        assertEquals(vtx0, visitedNodes.get(0));
        assertEquals(vtx1, visitedNodes.get(1));
        assertEquals(vtx11, visitedNodes.get(2));
        assertEquals(vtx12, visitedNodes.get(3));
        assertEquals(vtx2, visitedNodes.get(4));
        assertEquals(vtx21, visitedNodes.get(5));
        assertEquals(vtx211, visitedNodes.get(6));
        assertEquals(vtx212, visitedNodes.get(7));
        assertEquals(vtx22, visitedNodes.get(8));
        assertEquals(vtx221, visitedNodes.get(9));
        assertEquals(vtx222, visitedNodes.get(10));
        assertEquals(vtx223, visitedNodes.get(11));
        assertEquals(vtx3, visitedNodes.get(12));
        assertEquals(vtx31, visitedNodes.get(13));
        assertEquals(vtx32, visitedNodes.get(14));
        assertEquals(vtx33, visitedNodes.get(15));

        assertFalse(status.hasCycles());
        assertFalse(status.hasErrors());
    }

    @Test
    void depthFirstWithCycleAndWithoutError() {
        SimpleGraph graph = new SimpleGraph();
        SimpleVertex vtx0 = graph.addVertex("root");
        SimpleVertex vtx1 = graph.addVertex("vertex1");
        SimpleVertex vtx11 = graph.addVertex("vertex11");
        SimpleVertex vtx12 = graph.addVertex("vertex12");
        SimpleVertex vtx2 = graph.addVertex("vertex2");
        SimpleVertex vtx21 = graph.addVertex("vertex21");
        SimpleVertex vtx211 = graph.addVertex("vertex211");
        SimpleVertex vtx212 = graph.addVertex("vertex212");

        SimpleVertex vtx22 = graph.addVertex("vertex22");
        SimpleVertex vtx221 = graph.addVertex("vertex221");
        SimpleVertex vtx222 = graph.addVertex("vertex222");
        SimpleVertex vtx223 = graph.addVertex("vertex223");

        SimpleVertex vtx3 = graph.addVertex("vertex3");
        SimpleVertex vtx31 = graph.addVertex("vertex31");
        SimpleVertex vtx32 = graph.addVertex("vertex32");
        SimpleVertex vtx33 = graph.addVertex("vertex33");

        graph.addEdge("root->1", vtx0, vtx1);
        graph.addEdge("root->2", vtx0, vtx2);
        graph.addEdge("root->3", vtx0, vtx3);

        graph.addEdge("1->11", vtx1, vtx11);
        graph.addEdge("1->12", vtx1, vtx12);

        graph.addEdge("2->21", vtx2, vtx21);
        graph.addEdge("2->22", vtx2, vtx22);
        graph.addEdge("22->2", vtx22, vtx2);

        graph.addEdge("21->211", vtx21, vtx211);
        graph.addEdge("21->212", vtx21, vtx212);

        graph.addEdge("22->221", vtx22, vtx221);
        graph.addEdge("22->222", vtx22, vtx222);
        graph.addEdge("22->223", vtx22, vtx223);
        graph.addEdge("223->root", vtx223, vtx0);

        graph.addEdge("3->31", vtx3, vtx31);
        graph.addEdge("3->32", vtx3, vtx32);
        graph.addEdge("3->33", vtx3, vtx33);

        List<SimpleVertex> visitedNodes = new ArrayList<>();
        TraversalStatus<SimpleVertex> status = Traversal.depthFirst(vtx0, v -> {
            log.debug("Visiting vertex " + v);
            if (visitedNodes.contains(v)) {
                throw new RuntimeException("Double visit of vertex: " + v);
            }
            visitedNodes.add(v);
            return true;
        }, true);

        assertEquals(vtx0, visitedNodes.get(0));
        assertEquals(vtx1, visitedNodes.get(1));
        assertEquals(vtx11, visitedNodes.get(2));
        assertEquals(vtx12, visitedNodes.get(3));
        assertEquals(vtx2, visitedNodes.get(4));
        assertEquals(vtx21, visitedNodes.get(5));
        assertEquals(vtx211, visitedNodes.get(6));
        assertEquals(vtx212, visitedNodes.get(7));
        assertEquals(vtx22, visitedNodes.get(8));
        assertEquals(vtx221, visitedNodes.get(9));
        assertEquals(vtx222, visitedNodes.get(10));
        assertEquals(vtx223, visitedNodes.get(11));
        assertEquals(vtx3, visitedNodes.get(12));
        assertEquals(vtx31, visitedNodes.get(13));
        assertEquals(vtx32, visitedNodes.get(14));
        assertEquals(vtx33, visitedNodes.get(15));

        assertTrue(status.hasCycles());
        assertEquals(2, status.getCycleCount());
        assertFalse(status.hasErrors());

    }

    @Test
    void depthFirstWithCycleAndWithError() {
        SimpleGraph graph = new SimpleGraph();
        SimpleVertex vtx0 = graph.addVertex("root");
        SimpleVertex vtx1 = graph.addVertex("vertex1");
        SimpleVertex vtx11 = graph.addVertex("vertex11");
        SimpleVertex vtx12 = graph.addVertex("vertex12");
        SimpleVertex vtx2 = graph.addVertex("vertex2");
        SimpleVertex vtx21 = graph.addVertex("vertex21");
        SimpleVertex vtx211 = graph.addVertex("vertex211");
        SimpleVertex vtx212 = graph.addVertex("vertex212");

        SimpleVertex vtx22 = graph.addVertex("vertex22");
        SimpleVertex vtx221 = graph.addVertex("vertex221");
        SimpleVertex vtx222 = graph.addVertex("vertex222");
        SimpleVertex vtx223 = graph.addVertex("vertex223");

        SimpleVertex vtx3 = graph.addVertex("vertex3");
        SimpleVertex vtx31 = graph.addVertex("vertex31");
        SimpleVertex vtx32 = graph.addVertex("vertex32");
        SimpleVertex vtx33 = graph.addVertex("vertex33");

        graph.addEdge("root->1", vtx0, vtx1);
        graph.addEdge("root->2", vtx0, vtx2);
        graph.addEdge("root->3", vtx0, vtx3);

        graph.addEdge("1->11", vtx1, vtx11);
        graph.addEdge("1->12", vtx1, vtx12);

        graph.addEdge("2->21", vtx2, vtx21);
        graph.addEdge("2->22", vtx2, vtx22);
        graph.addEdge("22->2", vtx22, vtx2);

        graph.addEdge("21->211", vtx21, vtx211);
        graph.addEdge("21->212", vtx21, vtx212);

        graph.addEdge("22->221", vtx22, vtx221);
        graph.addEdge("22->222", vtx22, vtx222);
        graph.addEdge("22->223", vtx22, vtx223);
        graph.addEdge("223->root", vtx223, vtx0);

        graph.addEdge("3->31", vtx3, vtx31);
        graph.addEdge("3->32", vtx3, vtx32);
        graph.addEdge("3->33", vtx3, vtx33);

        List<SimpleVertex> visitedNodes = new ArrayList<>();
        TraversalStatus<SimpleVertex> status = Traversal.depthFirst(vtx0, v -> {
            log.debug("Visiting vertex " + v);
            if (visitedNodes.contains(v)) {
                throw new RuntimeException("Double visit of vertex: " + v);
            }
            visitedNodes.add(v);
            if (v == vtx223 || v == vtx33 || v == vtx212) {
                throw new RuntimeException("Error for node " + v);
            }
            return true;
        }, true);

        assertEquals(vtx0, visitedNodes.get(0));
        assertEquals(vtx1, visitedNodes.get(1));
        assertEquals(vtx11, visitedNodes.get(2));
        assertEquals(vtx12, visitedNodes.get(3));
        assertEquals(vtx2, visitedNodes.get(4));
        assertEquals(vtx21, visitedNodes.get(5));
        assertEquals(vtx211, visitedNodes.get(6));
        assertEquals(vtx212, visitedNodes.get(7));
        assertEquals(vtx22, visitedNodes.get(8));
        assertEquals(vtx221, visitedNodes.get(9));
        assertEquals(vtx222, visitedNodes.get(10));
        assertEquals(vtx223, visitedNodes.get(11));
        assertEquals(vtx3, visitedNodes.get(12));
        assertEquals(vtx31, visitedNodes.get(13));
        assertEquals(vtx32, visitedNodes.get(14));
        assertEquals(vtx33, visitedNodes.get(15));

        assertTrue(status.hasCycles());
        assertEquals(2, status.getCycleCount());
        assertTrue(status.hasErrors());
        assertEquals(3, status.getErrorList().size());
        assertTrue(status.getErrorList().get(0).getException().getMessage().startsWith("Error for node"));

    }

    @Test
    void breadthFirstWithoutCyclesAndWithoutError() {
        SimpleGraph graph = new SimpleGraph();
        SimpleVertex vtx0 = graph.addVertex("root");
        SimpleVertex vtx1 = graph.addVertex("vertex1");
        SimpleVertex vtx11 = graph.addVertex("vertex11");
        SimpleVertex vtx12 = graph.addVertex("vertex12");
        SimpleVertex vtx2 = graph.addVertex("vertex2");
        SimpleVertex vtx21 = graph.addVertex("vertex21");
        SimpleVertex vtx211 = graph.addVertex("vertex211");
        SimpleVertex vtx212 = graph.addVertex("vertex212");

        SimpleVertex vtx22 = graph.addVertex("vertex22");
        SimpleVertex vtx221 = graph.addVertex("vertex221");
        SimpleVertex vtx222 = graph.addVertex("vertex222");
        SimpleVertex vtx223 = graph.addVertex("vertex223");

        SimpleVertex vtx3 = graph.addVertex("vertex3");
        SimpleVertex vtx31 = graph.addVertex("vertex31");
        SimpleVertex vtx32 = graph.addVertex("vertex32");
        SimpleVertex vtx33 = graph.addVertex("vertex33");

        graph.addEdge("root->1", vtx0, vtx1);
        graph.addEdge("root->2", vtx0, vtx2);
        graph.addEdge("root->3", vtx0, vtx3);

        graph.addEdge("1->11", vtx1, vtx11);
        graph.addEdge("1->12", vtx1, vtx12);

        graph.addEdge("2->21", vtx2, vtx21);
        graph.addEdge("2->22", vtx2, vtx22);

        graph.addEdge("21->211", vtx21, vtx211);
        graph.addEdge("21->212", vtx21, vtx212);

        graph.addEdge("22->221", vtx22, vtx221);
        graph.addEdge("22->222", vtx22, vtx222);
        graph.addEdge("22->223", vtx22, vtx223);

        graph.addEdge("3->31", vtx3, vtx31);
        graph.addEdge("3->32", vtx3, vtx32);
        graph.addEdge("3->33", vtx3, vtx33);

        List<SimpleVertex> visitedNodes = new ArrayList<>();
        TraversalStatus<SimpleVertex> status = Traversal.breadthFirst(vtx0, v -> {
            log.debug("Visiting vertex " + v);
            if (visitedNodes.contains(v)) {
                throw new RuntimeException("Double visit of vertex: " + v);
            }
            visitedNodes.add(v);
            return true;
        }, true);

        assertEquals(vtx0, visitedNodes.get(0));
        assertEquals(vtx1, visitedNodes.get(1));
        assertEquals(vtx2, visitedNodes.get(2));
        assertEquals(vtx3, visitedNodes.get(3));
        assertEquals(vtx11, visitedNodes.get(4));
        assertEquals(vtx12, visitedNodes.get(5));
        assertEquals(vtx21, visitedNodes.get(6));
        assertEquals(vtx22, visitedNodes.get(7));
        assertEquals(vtx31, visitedNodes.get(8));
        assertEquals(vtx32, visitedNodes.get(9));
        assertEquals(vtx33, visitedNodes.get(10));
        assertEquals(vtx211, visitedNodes.get(11));
        assertEquals(vtx212, visitedNodes.get(12));
        assertEquals(vtx221, visitedNodes.get(13));
        assertEquals(vtx222, visitedNodes.get(14));
        assertEquals(vtx223, visitedNodes.get(15));

        assertFalse(status.hasCycles());
        assertFalse(status.hasErrors());
    }

    @Test
    void breadthFirstWithCyclesAndWithoutError() {
        SimpleGraph graph = new SimpleGraph();
        SimpleVertex vtx0 = graph.addVertex("root");
        SimpleVertex vtx1 = graph.addVertex("vertex1");
        SimpleVertex vtx11 = graph.addVertex("vertex11");
        SimpleVertex vtx12 = graph.addVertex("vertex12");
        SimpleVertex vtx2 = graph.addVertex("vertex2");
        SimpleVertex vtx21 = graph.addVertex("vertex21");
        SimpleVertex vtx211 = graph.addVertex("vertex211");
        SimpleVertex vtx212 = graph.addVertex("vertex212");

        SimpleVertex vtx22 = graph.addVertex("vertex22");
        SimpleVertex vtx221 = graph.addVertex("vertex221");
        SimpleVertex vtx222 = graph.addVertex("vertex222");
        SimpleVertex vtx223 = graph.addVertex("vertex223");

        SimpleVertex vtx3 = graph.addVertex("vertex3");
        SimpleVertex vtx31 = graph.addVertex("vertex31");
        SimpleVertex vtx32 = graph.addVertex("vertex32");
        SimpleVertex vtx33 = graph.addVertex("vertex33");

        graph.addEdge("root->1", vtx0, vtx1);
        graph.addEdge("root->2", vtx0, vtx2);
        graph.addEdge("root->3", vtx0, vtx3);

        graph.addEdge("1->11", vtx1, vtx11);
        graph.addEdge("1->12", vtx1, vtx12);

        graph.addEdge("2->21", vtx2, vtx21);
        graph.addEdge("2->22", vtx2, vtx22);

        graph.addEdge("21->211", vtx21, vtx211);
        graph.addEdge("21->212", vtx21, vtx212);
        graph.addEdge("211->21", vtx211, vtx21);

        graph.addEdge("22->221", vtx22, vtx221);
        graph.addEdge("22->222", vtx22, vtx222);
        graph.addEdge("22->223", vtx22, vtx223);
        graph.addEdge("223->root", vtx223, vtx0);

        graph.addEdge("3->31", vtx3, vtx31);
        graph.addEdge("3->32", vtx3, vtx32);
        graph.addEdge("3->33", vtx3, vtx33);

        List<SimpleVertex> visitedNodes = new ArrayList<>();
        TraversalStatus<SimpleVertex> status = Traversal.breadthFirst(vtx0, v -> {
            log.debug("Visiting vertex " + v);
            if (visitedNodes.contains(v)) {
                throw new RuntimeException("Double visit of vertex: " + v);
            }
            visitedNodes.add(v);
            return true;
        }, true);

        assertEquals(vtx0, visitedNodes.get(0));
        assertEquals(vtx1, visitedNodes.get(1));
        assertEquals(vtx2, visitedNodes.get(2));
        assertEquals(vtx3, visitedNodes.get(3));
        assertEquals(vtx11, visitedNodes.get(4));
        assertEquals(vtx12, visitedNodes.get(5));
        assertEquals(vtx21, visitedNodes.get(6));
        assertEquals(vtx22, visitedNodes.get(7));
        assertEquals(vtx31, visitedNodes.get(8));
        assertEquals(vtx32, visitedNodes.get(9));
        assertEquals(vtx33, visitedNodes.get(10));
        assertEquals(vtx211, visitedNodes.get(11));
        assertEquals(vtx212, visitedNodes.get(12));
        assertEquals(vtx221, visitedNodes.get(13));
        assertEquals(vtx222, visitedNodes.get(14));
        assertEquals(vtx223, visitedNodes.get(15));

        assertTrue(status.hasCycles());
        assertEquals(2, status.getCycleCount());
        assertFalse(status.hasErrors());
    }

    @Test
    void breadthFirstWithCyclesAndWithError() {
        SimpleGraph graph = new SimpleGraph();
        SimpleVertex vtx0 = graph.addVertex("root");
        SimpleVertex vtx1 = graph.addVertex("vertex1");
        SimpleVertex vtx11 = graph.addVertex("vertex11");
        SimpleVertex vtx12 = graph.addVertex("vertex12");
        SimpleVertex vtx2 = graph.addVertex("vertex2");
        SimpleVertex vtx21 = graph.addVertex("vertex21");
        SimpleVertex vtx211 = graph.addVertex("vertex211");
        SimpleVertex vtx212 = graph.addVertex("vertex212");

        SimpleVertex vtx22 = graph.addVertex("vertex22");
        SimpleVertex vtx221 = graph.addVertex("vertex221");
        SimpleVertex vtx222 = graph.addVertex("vertex222");
        SimpleVertex vtx223 = graph.addVertex("vertex223");

        SimpleVertex vtx3 = graph.addVertex("vertex3");
        SimpleVertex vtx31 = graph.addVertex("vertex31");
        SimpleVertex vtx32 = graph.addVertex("vertex32");
        SimpleVertex vtx33 = graph.addVertex("vertex33");

        graph.addEdge("root->1", vtx0, vtx1);
        graph.addEdge("root->2", vtx0, vtx2);
        graph.addEdge("root->3", vtx0, vtx3);

        graph.addEdge("1->11", vtx1, vtx11);
        graph.addEdge("1->12", vtx1, vtx12);

        graph.addEdge("2->21", vtx2, vtx21);
        graph.addEdge("2->22", vtx2, vtx22);

        graph.addEdge("21->211", vtx21, vtx211);
        graph.addEdge("21->212", vtx21, vtx212);
        graph.addEdge("211->21", vtx211, vtx21);

        graph.addEdge("22->221", vtx22, vtx221);
        graph.addEdge("22->222", vtx22, vtx222);
        graph.addEdge("22->223", vtx22, vtx223);
        graph.addEdge("223->root", vtx223, vtx0);

        graph.addEdge("3->31", vtx3, vtx31);
        graph.addEdge("3->32", vtx3, vtx32);
        graph.addEdge("3->33", vtx3, vtx33);

        List<SimpleVertex> visitedNodes = new ArrayList<>();
        TraversalStatus<SimpleVertex> status = Traversal.breadthFirst(vtx0, v -> {
            log.debug("Visiting vertex " + v);
            if (visitedNodes.contains(v)) {
                throw new RuntimeException("Double visit of vertex: " + v);
            }
            visitedNodes.add(v);
            if (v == vtx212 || v == vtx31 || v == vtx21) {
                throw new RuntimeException("Error on node: " + v);
            }
            return true;
        }, true);

        assertEquals(vtx0, visitedNodes.get(0));
        assertEquals(vtx1, visitedNodes.get(1));
        assertEquals(vtx2, visitedNodes.get(2));
        assertEquals(vtx3, visitedNodes.get(3));
        assertEquals(vtx11, visitedNodes.get(4));
        assertEquals(vtx12, visitedNodes.get(5));
        assertEquals(vtx21, visitedNodes.get(6));
        assertEquals(vtx22, visitedNodes.get(7));
        assertEquals(vtx31, visitedNodes.get(8));
        assertEquals(vtx32, visitedNodes.get(9));
        assertEquals(vtx33, visitedNodes.get(10));
        assertEquals(vtx211, visitedNodes.get(11));
        assertEquals(vtx212, visitedNodes.get(12));
        assertEquals(vtx221, visitedNodes.get(13));
        assertEquals(vtx222, visitedNodes.get(14));
        assertEquals(vtx223, visitedNodes.get(15));

        assertTrue(status.hasCycles());
        assertEquals(2, status.getCycleCount());
        assertTrue(status.hasErrors());
        assertEquals(3, status.getErrorList().size());
        assertTrue(status.getErrorList().get(0).getException().getMessage().startsWith("Error on node"));
    }

}