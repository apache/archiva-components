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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimpleNodeTest
{

    private Graph<SimpleNode> graph = new SimpleGraph( );

    @Test
    void getId( )
    {
        SimpleNode vtx = new SimpleNode( graph, "testid001" );
        assertNotNull( vtx.getId( ) );
        assertEquals( "testid001", vtx.getId( ) );
    }

    @Test
    void getLabel( )
    {
        SimpleNode vtx = new SimpleNode( graph, "testid001" );
        assertNull( vtx.getLabel( ) );
        vtx.setLabel( "test" );
        assertEquals( "test", vtx.getLabel( ) );

        vtx = new SimpleNode( graph, "testid001" );
        vtx.setLabel( "Another label with more characters üy@~ ---" );
        assertNotNull( vtx.getLabel( ) );
        assertEquals( "Another label with more characters üy@~ ---", vtx.getLabel( ) );

    }


    @Test
    void getGraph( )
    {
        SimpleNode vtx = new SimpleNode( graph, "test" );
        assertNotNull( vtx.getGraph( ) );
        assertTrue( vtx.getGraph( ) == graph );
    }

    @Test
    void getInOutEdges( )
    {
        SimpleNode vtx1 = new SimpleNode( graph, "test1" );
        SimpleNode vtx2 = new SimpleNode( graph, "test2" );
        vtx1.addEdge( new BaseEdge<>( graph, "edge11_1to2", vtx1, vtx2 ) );
        vtx1.addEdge( new BaseEdge<>( graph, "edge12_1to2", vtx1, vtx2 ) );
        vtx1.addEdge( new BaseEdge<>( graph, "edge13_1to2", vtx1, vtx2 ) );
        vtx1.addEdge( new BaseEdge<>( graph, "edge14_1to2", vtx1, vtx2 ) );
        vtx2.addEdge( new BaseEdge<>( graph, "edge21_1to2", vtx1, vtx2 ) );
        vtx2.addEdge( new BaseEdge<>( graph, "edge22_1to2", vtx1, vtx2 ) );
        vtx1.addEdge( new BaseEdge<>( graph, "edge11_2to1", vtx2, vtx1 ) );
        vtx1.addEdge( new BaseEdge<>( graph, "edge12_2to1", vtx2, vtx1 ) );
        vtx1.addEdge( new BaseEdge<>( graph, "edge13_2to1", vtx2, vtx1 ) );
        vtx1.addEdge( new BaseEdge<>( graph, "edge14_2to1", vtx2, vtx1 ) );
        vtx1.addEdge( new BaseEdge<>( graph, "edge15_2to1", vtx2, vtx1 ) );
        vtx1.addEdge( new BaseEdge<>( graph, "edge16_2to1", vtx2, vtx1 ) );
        vtx2.addEdge( new BaseEdge<>( graph, "edge21_2to1", vtx2, vtx1 ) );
        assertEquals( 6, vtx1.getInEdges( ).size( ) );
        assertEquals( 2, vtx2.getInEdges( ).size( ) );
        assertEquals( 4, vtx1.getOutEdges( ).size( ) );
        assertEquals( 1, vtx2.getOutEdges( ).size( ) );
    }


    @Test
    void removeEdge( )
    {
        SimpleNode vtx1 = new SimpleNode( graph, "test1" );
        SimpleNode vtx2 = new SimpleNode( graph, "test2" );
        vtx1.addEdge( new BaseEdge<>( graph, "edge11_1to2", vtx1, vtx2 ) );
        vtx1.addEdge( new BaseEdge<>( graph, "edge12_1to2", vtx1, vtx2 ) );
        vtx1.addEdge( new BaseEdge<>( graph, "edge13_1to2", vtx1, vtx2 ) );
        vtx1.addEdge( new BaseEdge<>( graph, "edge14_1to2", vtx1, vtx2 ) );
        vtx2.addEdge( new BaseEdge<>( graph, "edge21_1to2", vtx1, vtx2 ) );
        vtx2.addEdge( new BaseEdge<>( graph, "edge22_1to2", vtx1, vtx2 ) );
        vtx1.addEdge( new BaseEdge<>( graph, "edge11_2to1", vtx2, vtx1 ) );
        vtx1.addEdge( new BaseEdge<>( graph, "edge12_2to1", vtx2, vtx1 ) );
        vtx1.addEdge( new BaseEdge<>( graph, "edge13_2to1", vtx2, vtx1 ) );
        vtx1.addEdge( new BaseEdge<>( graph, "edge14_2to1", vtx2, vtx1 ) );
        vtx1.addEdge( new BaseEdge<>( graph, "edge15_2to1", vtx2, vtx1 ) );
        vtx1.addEdge( new BaseEdge<>( graph, "edge16_2to1", vtx2, vtx1 ) );
        vtx2.addEdge( new BaseEdge<>( graph, "edge21_2to1", vtx2, vtx1 ) );
        Edge<SimpleNode> edge = vtx1.getOutEdges( ).get( 0 );
        vtx1.removeEdge( edge );
        edge = vtx1.getInEdges( ).get( 0 );
        vtx1.removeEdge( edge );
        assertEquals( 5, vtx1.getInEdges( ).size( ) );
        assertEquals( 2, vtx2.getInEdges( ).size( ) );
        assertEquals( 3, vtx1.getOutEdges( ).size( ) );
        assertEquals( 1, vtx2.getOutEdges( ).size( ) );
    }
}