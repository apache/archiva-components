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
import org.apache.archiva.components.graph.api.Node;
import org.apache.archiva.components.graph.api.RelationType;
import org.apache.archiva.components.graph.api.StandardRelationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * Abstract implementation of a directed graph
 *
 * @param <V> The node implementation.
 */
public abstract class DirectedGraph<V extends Node<V>> implements Graph<V>
{

    private static final Logger log = LoggerFactory.getLogger( DirectedGraph.class );

    protected TreeMap<String, V> nodes = new TreeMap<>( );
    protected TreeMap<String, Edge<V>> edges = new TreeMap<>( );

    @Override
    public V newNode( String label )
    {
        V node = createNewNode( );
        node.setLabel( label );
        this.nodes.put( node.getId( ), node );
        return node;
    }

    @Override
    public V addNode( String id, String label )
    {
        V node;
        if ( nodes.containsKey( id ) )
        {
            node = nodes.get( id );
        }
        else
        {
            node = createNewNode( id );
            this.nodes.put( id, node );
        }
        node.setLabel( label );
        return node;
    }


    protected V createNewNode( )
    {
        return createNewNode( UUID.randomUUID( ).toString( ) );
    }

    /**
     * Subclasses that implement this method must return a new node instance for
     * each call. The node should not be connected with any edges.
     *
     * @return A new node instance.
     */
    abstract V createNewNode( String id );

    /**
     * Creates a new edge instance on each method call. The id will be a generated UUID.
     *
     * @param type            the relation type
     * @param sourceNode      the source node
     * @param destinationNode the destination node
     * @return the new edge instance
     */
    protected Edge<V> createNewEdge( RelationType type, V sourceNode, V destinationNode )
    {
        return createNewEdge( type, UUID.randomUUID( ).toString( ), sourceNode, destinationNode );
    }


    /**
     * Subclasses that implement this method must return a new instance of the edge with
     * the given source and destination nodes. The nodes must be linked by the new
     * edge.
     *
     * @param type            the type of the edge
     * @param id              the id of the new edge
     * @param sourceNode      the source node
     * @param destinationNode the destination node
     * @return A new edge instance.
     */
    abstract Edge<V> createNewEdge( RelationType type, String id, V sourceNode, V destinationNode );


    @Override
    public Edge<V> newEdge( String label, V sourceNode, V destinationNode )
    {
        return newEdge( StandardRelationType.DEFAULT, label, sourceNode, destinationNode );
    }

    @Override
    public Edge<V> newEdge( RelationType type, String label, V sourceNode, V destinationNode )
    {
        if ( sourceNode == null )
        {
            throw new IllegalArgumentException( "Source node may not be null" );
        }
        if ( destinationNode == null )
        {
            throw new IllegalArgumentException( "Destination node may not be null" );
        }
        Edge<V> edge = createNewEdge( type, sourceNode, destinationNode );
        edge.setLabel( label );
        this.edges.put( edge.getId( ), edge );
        return edge;
    }

    @Override
    public Edge<V> addEdge( final RelationType type, final String id, final String label,
                            final V sourceNode, final V destinationNode ) throws IllegalArgumentException
    {
        if ( sourceNode == null )
        {
            throw new IllegalArgumentException( "Source node may not be null" );
        }
        if ( destinationNode == null )
        {
            throw new IllegalArgumentException( "Destination node may not be null" );
        }
        Edge<V> edge;
        if ( this.edges.containsKey( id ) )
        {
            log.debug( "Edge found " + id );
            edge = this.edges.get( id );
        }
        else
        {
            edge = createNewEdge( type, id, sourceNode, destinationNode );
            this.edges.put( id, edge );
        }
        edge.setLabel( label );
        log.debug( "Adding edge " + edge.toString( ) );
        return edge;
    }

    @Override
    public Edge<V> addEdge( String id, String label, V sourceNode, V destinationNode ) throws IllegalArgumentException
    {
        return addEdge( StandardRelationType.DEFAULT, id, label, sourceNode, destinationNode );
    }

    @Override
    public V getNode( String id )
    {
        return nodes.get( id );
    }

    @Override
    public Edge<V> getEdge( String id )
    {
        return edges.get( id );
    }

    @Override
    public Set<V> getNodes( )
    {
        return nodes.values( ).stream( ).collect( Collectors.toSet( ) );
    }

    @Override
    public List<V> findNodes( String query )
    {
        return Collections.EMPTY_LIST;
    }

    @Override
    public void removeNode( V node )
    {
        List<Edge<V>> rmEdges = new ArrayList<>( node.getInEdges( ) );
        for ( Edge<V> edge : rmEdges )
        {
            removeEdge( edge );
        }
        rmEdges = new ArrayList<>( node.getOutEdges( ) );
        for ( Edge<V> edge : rmEdges )
        {
            removeEdge( edge );
        }
        nodes.remove( node.getId( ) );
    }

    @Override
    public void removeEdge( Edge<V> edge )
    {
        edges.remove( edge.getId( ) );
    }

}
