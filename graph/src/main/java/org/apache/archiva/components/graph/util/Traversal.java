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

import org.apache.archiva.components.graph.api.Edge;
import org.apache.archiva.components.graph.api.Node;
import org.apache.archiva.components.graph.api.TraversalStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.function.BiFunction;

/**
 * Utility class for graph traversal.
 */
public class Traversal
{

    private static final Logger log = LoggerFactory.getLogger( Traversal.class );

    /**
     * Traverses the graph starting at the start node and using a depth first algorithm.
     * Each node will only be consumed once.
     * The consumer function is applied for each visited node and must return True, if the
     * traversal should continue.
     * If the directed flag is set to true, only outgoing edges are used for traversal from one one
     * to the other, otherwise, incoming edges are used too.
     *
     * @param start    the start node
     * @param consumer The consumer function. The function must return <code>true</code>, if the traversal should
     *                 continue, otherwise <code>false</code>
     * @param flags    Sets some flags for traversal behaviour
     * @param <V>
     * @return The traversal status
     */
    public static <V extends Node<V>> TraversalStatus<V> depthFirst( final V start, final BiFunction<V, TraversalStatus<V>, Boolean> consumer,
                                                                     final BiFunction<V, TraversalStatus<V>, Boolean> afterChildConsumer,
                                                                     final TraversalFlags flags )
    {
        TraversalStatus<V> status = new TraversalStatus<>( );
        Set<V> visited = new LinkedHashSet<>( );
        Stack<V> stack = new Stack<>( );
        stack.push( start );
        while ( !stack.isEmpty( ) )
        {
            V node = stack.peek( );
            if ( !visited.contains( node ) )
            {
                Boolean continueTraversal = Boolean.TRUE;
                try
                {
                    continueTraversal = consumer.apply( node, status );
                }
                catch ( Throwable e )
                {
                    log.debug( "Error during visit. Node: {}, Message: {}", node, e.getMessage( ) );
                    status.addError( node, e );
                    if ( !flags.isContinueOnError( ) )
                    {
                        break;
                    }
                }
                visited.add( node );
                log.debug( "Visited: " + node );
                if ( !continueTraversal )
                {
                    log.debug( "Aborting from consumer on node {}", node.getId( ) );
                    break;
                }
                // We traverse using the order of the edges. This is a stack, so the elements that
                // should be visited first are pushed last on the stack.
                final List<Edge<V>> outEdges = node.getOutEdges( );
                final int outEdgeMaxIdx = node.getOutEdges( ).size( ) - 1;
                for ( int i = outEdgeMaxIdx; i >= 0; i-- )
                {
                    try
                    {
                        Edge<V> v = outEdges.get( i );
                        V dest = v.getDestination( );
                        log.debug( "Directed destination: {}", dest.getId( ) );
                        if ( !visited.contains( dest ) )
                        {
                            log.debug( "Adding to stack {}", dest.getId( ) );
                            stack.push( dest );
                        }
                        else if ( stack.contains( dest ) )
                        {
                            log.debug( "Cycle detected {}", dest.getId( ) );
                            status.registerCycle( dest );
                        }
                    }
                    catch ( IndexOutOfBoundsException e )
                    {
                        log.warn( "Modification of graph during traversal of output edges: " + node + " Index: " + i );
                    }
                }
                if ( !flags.isDirected( ) )
                {
                    final List<Edge<V>> inEdges = node.getInEdges( );
                    final int inEdgeMaxIdx = node.getOutEdges( ).size( ) - 1;
                    for ( int i = inEdgeMaxIdx; i >= 0; i-- )
                    {
                        try
                        {
                            Edge<V> v = inEdges.get( i );
                            V source = v.getSource( );
                            log.debug( "Undirected source: " + v.getSource( ) );
                            if ( !visited.contains( source ) )
                            {
                                stack.push( source );
                            }
                            else if ( stack.contains( source ) )
                            {
                                status.registerCycle( source );
                            }
                        }
                        catch ( IndexOutOfBoundsException e )
                        {
                            log.warn( "Modification of graph during traversal of input edges: " + node + " Index: " + i );
                        }
                    }
                }
            }
            else
            {
                node = stack.pop( );
                if ( !afterChildConsumer.apply( node, status ) )
                {
                    log.debug( "Aborting from after child consumer on node {}", node.getId( ) );
                    break;
                }
            }
        }
        return status;
    }

    public static <V extends Node<V>> TraversalStatus<V> depthFirst( final V start, final BiFunction<V, TraversalStatus<V>, Boolean> consumer,
                                                                     final TraversalFlags flags )
    {
        return depthFirst( start, consumer, ( v, s ) -> true, flags );
    }

    /**
     * Same as {@link #depthFirst(Node, BiFunction, TraversalFlags)} but sets the <code>continueOnError</code>
     * parameter to <code>true</code> and <code>directed</code> parameter to <code>true</code>.
     */
    public static <V extends Node<V>> TraversalStatus<V> depthFirst( V start, BiFunction<V, TraversalStatus<V>, Boolean> consumer )
    {
        return depthFirst( start, consumer, new TraversalFlags( ) );
    }

    /**
     * Traverses the graph starting at the start node and using a breadth first algorithm.
     * Each node will only be consumed once.
     * The consumer function is applied for each visited node and must return True, if the
     * traversal should continue.
     * If the directed flag is set to true, only outgoing edges are used for traversal from one one
     * to the other, otherwise, incoming edges are used too.
     * This breadth first algorithm is not able to detect cycles, in a directed graph. Only in undirected.
     *
     * @param start    the start node
     * @param consumer The consumer function. The function must return <code>true</code>, if the traversal should
     *                 continue, otherwise <code>false</code>
     * @param flags    flags that control traversal behaviour
     * @param <V>
     * @return The traversal status
     */
    @SuppressWarnings( "Duplicates" )
    public static <V extends Node<V>> TraversalStatus<V> breadthFirst( final V start,
                                                                       final BiFunction<V, TraversalStatus<V>, Boolean> consumer,
                                                                       final TraversalFlags flags )
    {
        final TraversalStatus<V> status = new TraversalStatus<>( );
        final Set<V> visited = new LinkedHashSet<>( );
        final Queue<V> queue = new LinkedList<>( );
        queue.add( start );
        while ( !queue.isEmpty( ) )
        {
            V node = queue.poll( );
            if ( !visited.contains( node ) )
            {
                Boolean continueTraversal = Boolean.TRUE;
                try
                {
                    continueTraversal = consumer.apply( node, status );
                }
                catch ( Throwable e )
                {
                    log.debug( "Error during visit. Node: {}, Message: {}", node, e.getMessage( ) );
                    status.addError( node, e );
                    if ( !flags.isContinueOnError( ) )
                    {
                        break;
                    }
                }
                visited.add( node );
                log.debug( "Visited: " + node );
                if ( !continueTraversal )
                {
                    break;
                }
                for ( Edge<V> v : node.getOutEdges( ) )
                {
                    queue.add( v.getDestination( ) );
                }
                if ( !flags.isDirected( ) )
                {
                    for ( Edge<V> v : node.getInEdges( ) )
                    {
                        queue.add( v.getSource( ) );
                    }
                }
            }
            else if ( !flags.isDirected( ) )
            {
                status.registerCycle( node );
            }
        }
        return status;
    }

    /**
     * Same as {@link #breadthFirst(Node, BiFunction, TraversalFlags)} but sets <code>continueOnError</code> to <code>true</code>
     * and <code>directed</code> to <code>true</code>.
     */
    public static <V extends Node<V>> TraversalStatus<V> breadthFirst( final V start, final BiFunction<V, TraversalStatus<V>, Boolean> consumer )
    {
        return breadthFirst( start, consumer, new TraversalFlags( ) );
    }

    /**
     * Traverses the graph, stops and returns <code>true</code> if it founds a cycle, otherwise returns
     * <code>false</code>
     *
     * @param startNode the start node where the traversal starts
     * @param <V>
     * @return <code>true</code>, if a cycle was found, otherwise <code>false</code>
     */
    public static <V extends Node<V>> boolean hasCycle( final V startNode )
    {
        TraversalStatus<V> status = depthFirst( startNode, ( n, s ) -> !s.hasCycles( ) );
        return status.hasCycles( );
    }

    /**
     * Traverses the graph and if a cycle was detected returns the node where the cycle was detected.
     * Otherwise returns <code>null</code>
     *
     * @param startNode the start node, where the traversal starts
     * @param <V>
     * @return the node, where the cycle was detected, otherwise <code>null</code>
     */
    public static <V extends Node<V>> V findFirstCycleNode( final V startNode )
    {
        TraversalStatus<V> status = depthFirst( startNode, ( n, s ) -> !s.hasCycles( ) );
        if ( status.hasCycles( ) )
        {
            return status.getCycleNodes( ).get( 0 );
        }
        else
        {
            return null;
        }
    }

    /**
     * Traverses the graph and if a cycle was detected returns the node where the cycle was detected.
     * Otherwise returns <code>null</code>
     *
     * @param startNode the start node, where the traversal starts
     * @param <V>
     * @return the node, where the cycle was detected, otherwise <code>null</code>
     */
    public static <V extends Node<V>> List<V> findAllCycleNodes( final V startNode )
    {
        TraversalStatus<V> status = depthFirst( startNode, ( n, s ) -> true );
        if ( status.hasCycles( ) )
        {
            return status.getCycleNodes( );
        }
        else
        {
            return Collections.emptyList( );
        }
    }

    /**
     * Sorts the graph starting at the <code>startNode</code> in topological order. That means
     * for a given path the deepest nodes are before their ancestors.
     * For nodes of the same level the order is the order of the edges on the source node.
     *
     * @param startNode the node where the traversal will start
     * @param <V>
     * @return A list of sorted nodes
     */
    public static <V extends Node<V>> List<V> topologialSort( final V startNode )
    {
        List<V> nodeList = new ArrayList<>( );
        TraversalStatus<V> status = depthFirst( startNode, ( n, s ) -> true,
            ( n, s ) -> {
                nodeList.add( n );
                return true;
            }, new TraversalFlags( ) );
        return nodeList;
    }

}
