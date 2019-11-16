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

import org.apache.archiva.components.graph.api.Graph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BaseEdgeTest
{


    private Graph<SimpleNode> graph = new SimpleGraph( );

    @Test
    void getId( )
    {
        SimpleNode vtx1 = new SimpleNode( graph, "vtx1" );
        SimpleNode vtx2 = new SimpleNode( graph, "vtx2" );

        BaseEdge<SimpleNode> edge = new BaseEdge<>( graph, "edge01", vtx1, vtx2 );

        assertNotNull( edge.getId( ) );
        assertEquals( "edge01", edge.getId( ) );


    }

    @Test
    void getLabel( )
    {
        SimpleNode vtx1 = new SimpleNode( graph, "vtx1" );
        SimpleNode vtx2 = new SimpleNode( graph, "vtx2" );

        BaseEdge<SimpleNode> edge = new BaseEdge<>( graph, "edge01", vtx1, vtx2 );

        assertNull( edge.getLabel( ) );

        edge.setLabel( "edgelabel01" );

        assertEquals( "edgelabel01", edge.getLabel( ) );

        edge.setLabel( "Another label with exotic characters äÄö~§" );
        assertEquals( "Another label with exotic characters äÄö~§", edge.getLabel( ) );

    }

    @Test
    void setWeight( )
    {
        SimpleNode vtx1 = new SimpleNode( graph, "vtx1" );
        SimpleNode vtx2 = new SimpleNode( graph, "vtx2" );

        BaseEdge<SimpleNode> edge = new BaseEdge<>( graph, "edge01", vtx1, vtx2 );

        assertEquals( 1.0, edge.getWeight( ) );

        edge.setWeight( 1.5 );
        assertEquals( 1.5, edge.getWeight( ) );


    }

    @Test
    void getGraph( )
    {
        SimpleNode vtx1 = new SimpleNode( graph, "vtx1" );
        SimpleNode vtx2 = new SimpleNode( graph, "vtx2" );

        BaseEdge<SimpleNode> edge = new BaseEdge<>( graph, "edge01", vtx1, vtx2 );
        assertNotNull( edge.getGraph( ) );
        assertTrue( edge.getGraph( ) == graph );
    }

    @Test
    void getInVertex( )
    {
        SimpleNode vtx1 = new SimpleNode( graph, "vtx1" );
        SimpleNode vtx2 = new SimpleNode( graph, "vtx2" );

        BaseEdge<SimpleNode> edge = new BaseEdge<>( graph, "edge01", vtx1, vtx2 );

        assertEquals( vtx1, edge.getSource( ) );
    }

    @Test
    void getOutVertex( )
    {
        SimpleNode vtx1 = new SimpleNode( graph, "vtx1" );
        SimpleNode vtx2 = new SimpleNode( graph, "vtx2" );

        BaseEdge<SimpleNode> edge = new BaseEdge<>( graph, "edge01", vtx1, vtx2 );

        assertEquals( vtx2, edge.getDestination( ) );
    }

}