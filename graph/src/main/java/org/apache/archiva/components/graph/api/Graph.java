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
import java.util.Set;

/**
 * A graph is the container for nodes. The nodes are connected by edges. Each node and
 * edge instance is coupled to the graph where it was created.
 *
 * @param <V>
 */
public interface Graph<V extends Node<V>>
{

    /**
     * Creates a node with the given label in the graph. The id of the node is generated
     * by the graph implementation.
     * The method returns always a new node instance, even if a node with the given label
     * exists already.
     *
     * @param label the label of the new node
     * @return a new node instance with the given label and a generated id
     */
    V newNode( String label );

    /**
     * Adds a new node if the id does not exist yet, otherwise returns the node with the given id
     * and the new label set.
     *
     * @param id    the id of the node
     * @param label the label of the node
     * @return a new node instance or the existing node with the given id
     */
    V addNode( String id, String label );

    void removeNode( V node );


    /**
     * Creates a new edge instance with the given label. The method returns always a new edge instance,
     * even if a edge with the given label exists already.
     * The type is set to {@link StandardRelationType#DEFAULT}
     *
     * @param label           the edge label
     * @param sourceNode      the source node
     * @param destinationNode the destination node
     * @return a new edge instance
     * @throws IllegalArgumentException if source or destination is <code>null</code>
     */
    Edge<V> newEdge( String label, V sourceNode, V destinationNode ) throws IllegalArgumentException;

    /**
     * Creates a new edge instance with the given label. The method returns always a new edge instance,
     * even if a edge with the given label exists already.
     *
     * @param type            the edge type
     * @param label           the edge label
     * @param sourceNode      the source node
     * @param destinationNode the destination node
     * @return a new edge instance
     * @throws IllegalArgumentException if the source or destination node is <code>null</code>
     */
    Edge<V> newEdge( RelationType type, String label, V sourceNode, V destinationNode ) throws IllegalArgumentException;

    /**
     * Creates a new edge instance with the given id or returns the edge instance with the given id and
     * type.
     * If a edge with the id exists already but with a different type it will throw a {@link IllegalArgumentException}
     *
     * @param type            the type of the edge
     * @param id              the id
     * @param label           the edge label
     * @param sourceNode      the source node
     * @param destinationNode the destination node
     * @return the newly created edge instance
     * @throws IllegalArgumentException if a edge instance with the given id but a different type exists already. Or if
     *                                  source or destination node is <code>null</code>
     */
    Edge<V> addEdge( RelationType type, String id, String label, V sourceNode, V destinationNode ) throws IllegalArgumentException;

    /**
     * Creates a new edge instance with the given id or returns the edge instance with the given id.
     * If the edge is newly created it will get the type {@link StandardRelationType#DEFAULT}
     *
     * @param id              the id
     * @param label           the edge label
     * @param sourceNode      the source node
     * @param destinationNode the destination node
     * @return the newly created edge instance
     * @throws IllegalArgumentException if source or destination is <code>null</code>
     */
    Edge<V> addEdge( String id, String label, V sourceNode, V destinationNode ) throws IllegalArgumentException;

    /**
     * Removes the edge from the graph and unregisters it from the source and destination
     *
     * @param edge the edge to remove
     */
    void removeEdge( Edge<V> edge );

    /**
     * Returns the node with the given id
     *
     * @param id the node id
     * @return the found node, or <code>null</code>, if it does not exist
     */
    V getNode( String id );

    /**
     * Finds nodes using a given query. The query syntax depends on the graph implementation.
     *
     * @param query the query to find nodes
     * @return the found nodes, or a empty list
     */
    List<V> findNodes( String query );

    /**
     * Returns the edge with the given id.
     *
     * @param id the edge id
     * @return the found edge instance, or <code>null</code>, if it does not exist
     */
    Edge<V> getEdge( String id );

    /**
     * Returns all nodes of the graph
     *
     * @return the set of nodes that build the graph
     */
    Set<V> getNodes( );


}
