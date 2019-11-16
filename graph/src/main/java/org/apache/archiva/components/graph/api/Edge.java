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

/**
 * The edge links a source node to a destination node.
 * A edge instance is always part of a certain graph instance.
 *
 * @param <V> The node implementation.
 */
public interface Edge<V extends Node<V>>
{

    /**
     * Returns the identifier of this edge. The id must be unique for given graph.
     *
     * @return the identifier of this edge
     */
    String getId( );

    /**
     * Returns the label of this edge. The label must not be unique.
     * If the label was not set, it should return an empty string.
     *
     * @return the label of this edge, or a empty string
     */
    String getLabel( );

    /**
     * Sets the label of this edge to the given string.
     *
     * @param label the label string, that must not be <code>null</code>
     * @throws NullPointerException if the label parameter is <code>null</code>
     */
    void setLabel( String label ) throws NullPointerException;

    /**
     * Returns the graph where this edge was created.
     *
     * @return the graph instance
     */
    Graph<V> getGraph( );

    /**
     * Returns the node, that is on the source end of this edge.
     *
     * @return the source node
     */
    V getSource( );

    /**
     * Returns the node, that is on the destination end of this edge.
     *
     * @return the destination node
     */
    V getDestination( );

    /**
     * Returns the weight of this edge. For standard graph implementations the default should be 1.0, but
     * graph implementations may decide to set another default.
     *
     * @return the weight of this edge
     */
    double getWeight( );

    /**
     * Returns the RelationType of the edge. If nothing is set, the {@link StandardRelationType#DEFAULT} should
     * be returned.
     *
     * @return the type of the edge
     */
    RelationType getType( );


}
