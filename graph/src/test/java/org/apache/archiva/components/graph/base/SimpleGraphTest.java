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
import org.apache.archiva.components.graph.api.StandardRelationType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimpleGraphTest
{

    @Test
    void createNewNode( )
    {
        SimpleGraph graph = new SimpleGraph( );
        SimpleNode vtx = graph.createNewNode( );
        assertNotNull( vtx );
        assertNotNull( vtx.getId( ) );
        assertNotNull( java.util.UUID.fromString( vtx.getId( ) ) );
    }

    @Test
    void createNewEdge( )
    {
        SimpleGraph graph = new SimpleGraph( );
        SimpleNode vtx1 = graph.createNewNode( );
        SimpleNode vtx2 = graph.createNewNode( );
        Edge<SimpleNode> edge = graph.createNewEdge( StandardRelationType.DEFAULT, vtx1, vtx2 );
        assertNotNull( edge );
        assertNotNull( edge.getId( ) );
        assertNotNull( java.util.UUID.fromString( edge.getId( ) ) );
        assertEquals( vtx1, edge.getSource( ) );
        assertEquals( vtx2, edge.getDestination( ) );
    }


    @Test
    void removeEdge( )
    {
        SimpleGraph graph = new SimpleGraph( );
        SimpleNode vtx1 = graph.createNewNode( );
        SimpleNode vtx2 = graph.createNewNode( );
        Edge<SimpleNode> edge = graph.createNewEdge( StandardRelationType.DEFAULT, vtx1, vtx2 );

        graph.removeEdge( edge );

        assertEquals( 0, vtx1.getInEdges( ).size( ) );
        assertEquals( 0, vtx2.getInEdges( ).size( ) );
        assertEquals( 0, vtx1.getOutEdges( ).size( ) );
        assertEquals( 0, vtx2.getOutEdges( ).size( ) );

    }

    @Test
    void removeNode( )
    {
        String label = "root";
        SimpleGraph graph = new SimpleGraph( );
        SimpleNode node = graph.newNode( label );

        assertNotNull( node );
        assertNotNull( graph.getNode( node.getId( ) ) );

        graph.removeNode( node );
        assertNull( graph.getNode( node.getId( ) ) );

    }

    @Test
    void removeNodeWithEdges( )
    {
        String label = "root";
        SimpleGraph graph = new SimpleGraph( );
        SimpleNode node1 = graph.newNode( "1" );
        SimpleNode node2 = graph.newNode( "2" );
        Edge<SimpleNode> edge1 = graph.newEdge( "1:2", node1, node2 );
        Edge<SimpleNode> edge2 = graph.newEdge( "2:1", node2, node1 );


        assertNotNull( node1 );
        assertNotNull( graph.getNode( node1.getId( ) ) );

        graph.removeNode( node1 );

        assertNull( graph.getNode( node1.getId( ) ) );

        assertEquals( 0, node2.getInEdges( ).size( ) );
        assertEquals( 0, node2.getOutEdges( ).size( ) );


    }

    @Test
    void getNode( )
    {
        String label = "root";
        SimpleGraph graph = new SimpleGraph( );
        SimpleNode node = graph.newNode( label );

        assertNotNull( node );
        SimpleNode foundNode = graph.getNode( node.getId( ) );
        assertNotNull( foundNode );
        assertEquals( node, foundNode );
        assertEquals( label, foundNode.getLabel( ) );

    }


    @Test
    void newNode( )
    {
        String label = "root";
        SimpleGraph graph = new SimpleGraph( );
        SimpleNode node = graph.newNode( label );

        assertNotNull( node );
        assertEquals( label, node.getLabel( ) );

    }

    @Test
    void addNode( )
    {
        String id = "root";
        SimpleGraph graph = new SimpleGraph( );
        SimpleNode node = graph.addNode( id, id );

        assertNotNull( node );
        assertNotNull( graph.getNode( id ) );
        assertEquals( id, graph.getNode( id ).getLabel( ) );

    }

}