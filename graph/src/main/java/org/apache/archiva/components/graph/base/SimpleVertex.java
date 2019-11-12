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

import java.util.ArrayList;
import java.util.List;

/**
 * Simple vertex implementation. The hash value is based on the id.
 * Comparation is by label, if exists, otherwise by id.
 */
public class SimpleVertex implements Vertex<SimpleVertex>, Comparable<SimpleVertex>
{

    private final String id;
    private String label;
    private final Graph<SimpleVertex> graph;
    private List<Edge<SimpleVertex>> outEdges = new ArrayList<>();
    private List<Edge<SimpleVertex>> inEdges = new ArrayList<>();

    SimpleVertex(Graph<SimpleVertex> graph, String id) {
        this.id = id;
        this.graph = graph;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public Graph<SimpleVertex> getGraph() {
        return graph;
    }

    @Override
    public List<Edge<SimpleVertex>> getOutEdges() {
        return outEdges;
    }

    @Override
    public List<Edge<SimpleVertex>> getInEdges() {
        return inEdges;
    }

    protected void addEdge(Edge<SimpleVertex> edge) {
        if (edge.getSource() == this) {
            outEdges.add(edge);
        } else if (edge.getDestination() == this) {
            inEdges.add(edge);
        } else {
            throw new IllegalArgumentException("The given edge does not contain this vertex");
        }
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public String toString() {
        return this.id+"|"+this.label;
    }

    public void removeEdge(Edge<SimpleVertex> edge) {
        if (edge.getDestination()==this) {
            inEdges.remove(edge);
        } else if (edge.getSource()==this) {
            outEdges.remove(edge);
        }
    }

    @Override
    public int compareTo(SimpleVertex o) {
        if (label!=null && o.getLabel()!=null) {
            return this.label.compareTo(o.getLabel());
        } else {
            return this.id.compareTo(o.getId());
        }
    }
}
