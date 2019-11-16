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
import org.apache.archiva.components.graph.api.RelationType;

import java.util.List;

/**
 * Simple directed graph implementation that uses UUIDs as unique identifiers for
 * each node and edge.
 */
public class SimpleGraph extends DirectedGraph<SimpleNode>
{


    @Override
    SimpleNode createNewNode( String id )
    {
        return new SimpleNode( this, id );
    }


    @Override
    Edge<SimpleNode> createNewEdge( RelationType type, String id, SimpleNode sourceNode, SimpleNode destinationNode )
    {
        BaseEdge edge = new BaseEdge<>( this, id, sourceNode, destinationNode );
        addEdgeToNode( sourceNode, edge );
        addEdgeToNode( destinationNode, edge );
        return edge;
    }

    private void addEdgeToNode( SimpleNode node, Edge<SimpleNode> edge )
    {
        node.addEdge( edge );
    }


    @Override
    public List<SimpleNode> findNodes( String query )
    {
        return null;
    }


    @Override
    public void removeEdge( Edge<SimpleNode> edge )
    {
        super.removeEdge( edge );
        edge.getSource( ).removeEdge( edge );
        edge.getDestination( ).removeEdge( edge );
    }
}
