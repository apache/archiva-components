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
import org.apache.archiva.components.graph.api.TraversalStatus;
import org.apache.archiva.components.graph.api.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Utility class for graph traversal.
 */
public class Traversal {

    private static final Logger log = LoggerFactory.getLogger(Traversal.class);

    /**
     * Traverses the graph starting at the start node and using a depth first algorithm.
     * Each node will only be consumed once.
     * The consumer function is applied for each visited node and must return True, if the
     * traversal should continue.
     * If the directed flag is set to true, only outgoing edges are used for traversal from one one
     * to the other, otherwise, incoming edges are used too.
     *
     * @param start           the start node
     * @param consumer        The consumer function. The function must return <code>true</code>, if the traversal should
     *                        continue, otherwise <code>false</code>
     * @param directed        If true, only outgoing edges are used to navigate to neighbours, otherwise incoming and outgoing
     *                        edges are used.
     * @param continueOnError If true, the traversal continues, even if the consumer function threw an error
     * @param <V>
     * @return The traversal status
     */
    @SuppressWarnings("Duplicates")
    public static <V extends Vertex<V>> TraversalStatus<V> depthFirst(final V start, final Function<V, Boolean> consumer,
                                                                      final boolean directed, final boolean continueOnError) {
        TraversalStatus<V> status = new TraversalStatus<>();
        Set<V> visited = new LinkedHashSet<>();
        Stack<V> stack = new Stack<>();
        stack.push(start);
        while (!stack.isEmpty()) {
            V vertex = stack.pop();
            if (!visited.contains(vertex)) {
                Boolean continueTraversal = Boolean.TRUE;
                try {
                    continueTraversal = consumer.apply(vertex);
                } catch (Throwable e) {
                    log.debug("Error during visit. Vertex: {}, Message: {}", vertex, e.getMessage());
                    status.addError(vertex, e);
                    if (!continueOnError) {
                        break;
                    }
                }
                visited.add(vertex);
                log.debug("Visited: " + vertex);
                if (!continueTraversal) {
                    break;
                }
                // We traverse using the order of the edges. This is a stack, so the elements that
                // should be visited first are pushed last on the stack.
                final List<Edge<V>> outEdges = vertex.getOutEdges();
                final int outEdgeMaxIdx = vertex.getOutEdges().size() - 1;
                for (int i = outEdgeMaxIdx; i >= 0; i--) {
                    try {
                        Edge<V> v = outEdges.get(i);
                        log.debug("Directed destination: " + v.getDestination());
                        stack.push(v.getDestination());
                    } catch (IndexOutOfBoundsException e) {
                        log.warn("Modification of graph during traversal of output edges: " + vertex + " Index: " + i);
                    }
                }
                if (!directed) {
                    final List<Edge<V>> inEdges = vertex.getInEdges();
                    final int inEdgeMaxIdx = vertex.getOutEdges().size() - 1;
                    for (int i = inEdgeMaxIdx; i >= 0; i--) {
                        try {
                            Edge<V> v = inEdges.get(i);
                            log.debug("Undirected source: " + v.getSource());
                            stack.push(v.getSource());
                        } catch (IndexOutOfBoundsException e) {
                            log.warn("Modification of graph during traversal of input edges: " + vertex + " Index: " + i);
                        }
                    }
                }
            } else {
                status.registerCycle(vertex);
            }
        }
        return status;
    }

    /**
     * Same as {@link #depthFirst(Vertex, Function, boolean, boolean)} but sets the <code>continueOnError</code> parameter to <code>true</code>.
     */
    public static <V extends Vertex<V>> TraversalStatus<V> depthFirst(V start, Function<V, Boolean> consumer,
                                                                      boolean directed) {
        return depthFirst(start, consumer, directed, true);
    }

    /**
     * Same as {@link #depthFirst(Vertex, Function, boolean, boolean)} but sets the <code>continueOnError</code>
     * parameter to <code>true</code> and <code>directed</code> parameter to <code>true</code>.
     */
    public static <V extends Vertex<V>> TraversalStatus<V> depthFirst(V start, Function<V, Boolean> consumer) {
        return depthFirst(start, consumer, true, true);
    }

    /**
     * Traverses the graph starting at the start node and using a breadth first algorithm.
     * Each node will only be consumed once.
     * The consumer function is applied for each visited node and must return True, if the
     * traversal should continue.
     * If the directed flag is set to true, only outgoing edges are used for traversal from one one
     * to the other, otherwise, incoming edges are used too.
     *
     * @param start           the start node
     * @param consumer        The consumer function. The function must return <code>true</code>, if the traversal should
     *                        continue, otherwise <code>false</code>
     * @param directed        If true, only outgoing edges are used to navigate to neighbours, otherwise incoming and outgoing
     *                        edges are used.
     * @param continueOnError If true, the traversal continues, even if the consumer function threw an error
     * @param <V>
     * @return The traversal status
     */
    @SuppressWarnings("Duplicates")
    public static <V extends Vertex<V>> TraversalStatus<V> breadthFirst(final V start, final Function<V, Boolean> consumer,
                                                                        final boolean directed, final boolean continueOnError) {
        final TraversalStatus<V> status = new TraversalStatus<>();
        final Set<V> visited = new LinkedHashSet<>();
        final Queue<V> queue = new LinkedList<>();
        queue.add(start);
        while (!queue.isEmpty()) {
            V vertex = queue.poll();
            if (!visited.contains(vertex)) {
                Boolean continueTraversal = Boolean.TRUE;
                try {
                    continueTraversal = consumer.apply(vertex);
                } catch (Throwable e) {
                    log.debug("Error during visit. Vertex: {}, Message: {}", vertex, e.getMessage());
                    status.addError(vertex, e);
                    if (!continueOnError) {
                        break;
                    }
                }
                visited.add(vertex);
                log.debug("Visited: " + vertex);
                if (!continueTraversal) {
                    break;
                }
                for (Edge<V> v : vertex.getOutEdges()) {
                    queue.add(v.getDestination());
                }
                if (!directed) {
                    for (Edge<V> v : vertex.getInEdges()) {
                        queue.add(v.getSource());
                    }
                }
            } else {
                status.registerCycle(vertex);
            }
        }
        return status;
    }

    /**
     * Same as {@link #breadthFirst(Vertex, Function, boolean, boolean)} but sets <code>continueOnError</code>  to <code>true</code>.
     */
    public static <V extends Vertex<V>> TraversalStatus<V> breadthFirst(final V start, final Function<V, Boolean> consumer,
                                                                        final boolean directed) {
        return breadthFirst(start, consumer, directed, true);
    }

    /**
     * Same as {@link #breadthFirst(Vertex, Function, boolean, boolean)} but sets <code>continueOnError</code> to <code>true</code>
     * and <code>directed</code> to <code>true</code>.
     */
    public static <V extends Vertex<V>> TraversalStatus<V> breadthFirst(final V start, final Function<V, Boolean> consumer) {
        return breadthFirst(start, consumer, true, true);
    }

}
