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
import org.apache.archiva.components.graph.api.Graph;
import org.apache.archiva.components.graph.api.Vertex;

import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;


/**
 * Abstract implementation of a directed graph
 * @param <V> The vertex implementation.
 */
public abstract class DirectedGraph<V extends Vertex<V>> implements Graph<V> {


    protected TreeMap<String, V> vertices = new TreeMap<>();
    protected TreeMap<String, Edge<V>> edges = new TreeMap<>();

    public V addVertex(String label) {
        V vtx = createNewVertex();
        vtx.setLabel(label);
        this.vertices.put(vtx.getId(), vtx);
        return vtx;
    }

    /**
     * Subclasses that implement this method must return a new vertex instance for
     * each call. The vertex should not be connected with any edges.
     *
     * @return A new vertex instance.
     */
    abstract V createNewVertex();

    /**
     * Subclasses that implement this method must return a new instance of the edge with
     * the given source and destination vertices. The vertices must be linked by the new
     * edge.
     *
     * @param sourceVertex the source vertex
     * @param destinationVertex the destination vertex
     * @return A new edge instance.
     */
    abstract Edge<V> createNewEdge(V sourceVertex, V destinationVertex);



    @Override
    public Edge<V> addEdge(String label, V sourceVertex, V destinationVertex) {
        Edge<V> edge = createNewEdge(sourceVertex, destinationVertex);
        edge.setLabel(label);
        this.edges.put(edge.getId(), edge);
        return edge;
    }

    @Override
    public V getVertex(String id) {
        return vertices.get(id);
    }

    @Override
    public Edge<V> getEdge(String id) {
        return edges.get(id);
    }

    @Override
    public Set<V> getVertices() {
        return vertices.values().stream().collect(Collectors.toSet());
    }

}
