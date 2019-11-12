package org.apache.archiva.components.graph.api;
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

import org.apache.archiva.components.graph.base.SimpleVertex;

import java.util.Set;

/**
 *
 * A graph is the container for vertices. The vertices are connected by edges. Each vertex and
 * edge instance is coupled to the graph where it was created.
 *
 * @param <V>
 */
public interface Graph<V extends Vertex<V>> {

    V addVertex(String label);

    void removeVertex(V vertex);

    Edge<V> addEdge(String label, V inVertex, V outVertex);

    void removeEdge(Edge<SimpleVertex> edge);

    V getVertex(String id);

    Edge<V> getEdge(String id);

    Set<V> getVertices();



}
