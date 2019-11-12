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

public class BaseEdge<V extends Vertex<V>> implements Edge<V>, Comparable<BaseEdge<V>> {

    private final String id;
    private String label;
    private double weight = 1.0;
    private V sourceVertex;
    private V destinationVertex;
    private final Graph<V> graph;

    public BaseEdge(Graph<V> graph, String id, V sourceVertex, V destinationVertex) {
        this.id = id;
        this.graph = graph;
        this.sourceVertex = sourceVertex;
        this.destinationVertex = destinationVertex;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public Graph<V> getGraph() {
        return graph;
    }

    @Override
    public V getSource() {
        return sourceVertex;
    }

    @Override
    public V getDestination() {
        return destinationVertex;
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public String toString() {
        return this.id+"|"+this.label+": "+ sourceVertex.toString() + " -> " + destinationVertex.toString();
    }

    @Override
    public int compareTo(BaseEdge<V> o) {
        if (this.label!=null || o.getLabel()!=null) {
            return this.label.compareTo(o.getLabel());
        } else {
            return this.id.compareTo(o.getId());
        }
    }
}
