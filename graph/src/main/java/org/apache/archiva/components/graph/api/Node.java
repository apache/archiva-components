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

import java.util.List;

/**
 * The node is a single entity in a graph structure. The node may have connections to and from
 * other nodes (represented by edges).
 *
 * @param <V> The node implementation
 */
public interface Node<V extends Node<V>>
{

    /**
     * Returns the identifier of this node. The identifier is unique for a given graph.
     *
     * @return the identifier
     */
    String getId( );

    /**
     * Returns the label of this node. The label is a name and must not be unique.
     * If the label was not set it should return a empty string.
     *
     * @return the label of the node or a empty string
     */
    String getLabel( );

    /**
     * Sets the label of the node
     *
     * @param label sets the label string, must not be <code>null</code>
     * @throws NullPointerException if the given label is <code>null</code>
     */
    void setLabel( String label ) throws NullPointerException;

    /**
     * Returns the graph where this node was created.
     *
     * @return the graph instance
     */
    Graph<V> getGraph( );

    /**
     * Returns all edges that connect to other nodes
     *
     * @return the edges where this node is the source
     */
    List<Edge<V>> getOutEdges( );

    /**
     * Returns all edges that connect other nodes to this node
     *
     * @return the edges where this node is the destination
     */
    List<Edge<V>> getInEdges( );

    /**
     * Returns the categories of the node.
     *
     * @return the list of categories of the node
     */
    List<Category> getCategories( );

    /**
     * Adds a category to the node
     *
     * @param category the category to add
     */
    void addCategory( Category category );


    /**
     * Removes a category from the node.
     *
     * @param category the category to remove
     */
    void removeCategory( Category category );
}
