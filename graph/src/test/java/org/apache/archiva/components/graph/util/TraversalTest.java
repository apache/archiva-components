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

import org.apache.archiva.components.graph.api.TraversalStatus;
import org.apache.archiva.components.graph.base.SimpleGraph;
import org.apache.archiva.components.graph.base.SimpleNode;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TraversalTest
{

    private static final Logger log = LoggerFactory.getLogger( TraversalTest.class );

    @Test
    void depthFirstWithoutCycleAndWithoutError( )
    {
        SimpleGraph graph = new SimpleGraph( );
        SimpleNode vtx0 = graph.newNode( "root" );
        SimpleNode vtx1 = graph.newNode( "vertex1" );
        SimpleNode vtx11 = graph.newNode( "vertex11" );
        SimpleNode vtx12 = graph.newNode( "vertex12" );
        SimpleNode vtx2 = graph.newNode( "vertex2" );
        SimpleNode vtx21 = graph.newNode( "vertex21" );
        SimpleNode vtx211 = graph.newNode( "vertex211" );
        SimpleNode vtx212 = graph.newNode( "vertex212" );

        SimpleNode vtx22 = graph.newNode( "vertex22" );
        SimpleNode vtx221 = graph.newNode( "vertex221" );
        SimpleNode vtx222 = graph.newNode( "vertex222" );
        SimpleNode vtx223 = graph.newNode( "vertex223" );

        SimpleNode vtx3 = graph.newNode( "vertex3" );
        SimpleNode vtx31 = graph.newNode( "vertex31" );
        SimpleNode vtx32 = graph.newNode( "vertex32" );
        SimpleNode vtx33 = graph.newNode( "vertex33" );

        graph.newEdge( "root->1", vtx0, vtx1 );
        graph.newEdge( "root->2", vtx0, vtx2 );
        graph.newEdge( "root->3", vtx0, vtx3 );

        graph.newEdge( "1->11", vtx1, vtx11 );
        graph.newEdge( "1->12", vtx1, vtx12 );

        graph.newEdge( "2->21", vtx2, vtx21 );
        graph.newEdge( "2->22", vtx2, vtx22 );

        graph.newEdge( "21->211", vtx21, vtx211 );
        graph.newEdge( "21->212", vtx21, vtx212 );

        graph.newEdge( "22->221", vtx22, vtx221 );
        graph.newEdge( "22->222", vtx22, vtx222 );
        graph.newEdge( "22->223", vtx22, vtx223 );

        graph.newEdge( "3->31", vtx3, vtx31 );
        graph.newEdge( "3->32", vtx3, vtx32 );
        graph.newEdge( "3->33", vtx3, vtx33 );

        List<SimpleNode> visitedNodes = new ArrayList<>( );
        TraversalStatus<SimpleNode> status = Traversal.depthFirst( vtx0, ( v, s ) -> {
            log.debug( "Visiting vertex " + v );
            if ( visitedNodes.contains( v ) )
            {
                throw new RuntimeException( "Double visit of vertex: " + v );
            }
            visitedNodes.add( v );
            return true;
        } );

        assertEquals( vtx0, visitedNodes.get( 0 ) );
        assertEquals( vtx1, visitedNodes.get( 1 ) );
        assertEquals( vtx11, visitedNodes.get( 2 ) );
        assertEquals( vtx12, visitedNodes.get( 3 ) );
        assertEquals( vtx2, visitedNodes.get( 4 ) );
        assertEquals( vtx21, visitedNodes.get( 5 ) );
        assertEquals( vtx211, visitedNodes.get( 6 ) );
        assertEquals( vtx212, visitedNodes.get( 7 ) );
        assertEquals( vtx22, visitedNodes.get( 8 ) );
        assertEquals( vtx221, visitedNodes.get( 9 ) );
        assertEquals( vtx222, visitedNodes.get( 10 ) );
        assertEquals( vtx223, visitedNodes.get( 11 ) );
        assertEquals( vtx3, visitedNodes.get( 12 ) );
        assertEquals( vtx31, visitedNodes.get( 13 ) );
        assertEquals( vtx32, visitedNodes.get( 14 ) );
        assertEquals( vtx33, visitedNodes.get( 15 ) );

        assertFalse( status.hasCycles( ) );
        assertFalse( status.hasErrors( ) );
    }

    @Test
    void depthFirstWithCycleAndWithoutError( )
    {
        SimpleGraph graph = new SimpleGraph( );
        SimpleNode vtx0 = graph.newNode( "root" );
        SimpleNode vtx1 = graph.newNode( "vertex1" );
        SimpleNode vtx11 = graph.newNode( "vertex11" );
        SimpleNode vtx12 = graph.newNode( "vertex12" );
        SimpleNode vtx2 = graph.newNode( "vertex2" );
        SimpleNode vtx21 = graph.newNode( "vertex21" );
        SimpleNode vtx211 = graph.newNode( "vertex211" );
        SimpleNode vtx212 = graph.newNode( "vertex212" );

        SimpleNode vtx22 = graph.newNode( "vertex22" );
        SimpleNode vtx221 = graph.newNode( "vertex221" );
        SimpleNode vtx222 = graph.newNode( "vertex222" );
        SimpleNode vtx223 = graph.newNode( "vertex223" );

        SimpleNode vtx3 = graph.newNode( "vertex3" );
        SimpleNode vtx31 = graph.newNode( "vertex31" );
        SimpleNode vtx32 = graph.newNode( "vertex32" );
        SimpleNode vtx33 = graph.newNode( "vertex33" );

        graph.newEdge( "root->1", vtx0, vtx1 );
        graph.newEdge( "root->2", vtx0, vtx2 );
        graph.newEdge( "root->3", vtx0, vtx3 );

        graph.newEdge( "1->11", vtx1, vtx11 );
        graph.newEdge( "1->12", vtx1, vtx12 );

        graph.newEdge( "2->21", vtx2, vtx21 );
        graph.newEdge( "2->22", vtx2, vtx22 );
        graph.newEdge( "22->2", vtx22, vtx2 );

        graph.newEdge( "21->211", vtx21, vtx211 );
        graph.newEdge( "21->212", vtx21, vtx212 );

        graph.newEdge( "22->221", vtx22, vtx221 );
        graph.newEdge( "22->222", vtx22, vtx222 );
        graph.newEdge( "22->223", vtx22, vtx223 );
        graph.newEdge( "223->root", vtx223, vtx0 );

        graph.newEdge( "3->31", vtx3, vtx31 );
        graph.newEdge( "3->32", vtx3, vtx32 );
        graph.newEdge( "3->33", vtx3, vtx33 );

        List<SimpleNode> visitedNodes = new ArrayList<>( );
        TraversalStatus<SimpleNode> status = Traversal.depthFirst( vtx0, ( v, s ) -> {
            log.debug( "Visiting vertex " + v );
            if ( visitedNodes.contains( v ) )
            {
                throw new RuntimeException( "Double visit of vertex: " + v );
            }
            visitedNodes.add( v );
            return true;
        } );

        assertEquals( vtx0, visitedNodes.get( 0 ) );
        assertEquals( vtx1, visitedNodes.get( 1 ) );
        assertEquals( vtx11, visitedNodes.get( 2 ) );
        assertEquals( vtx12, visitedNodes.get( 3 ) );
        assertEquals( vtx2, visitedNodes.get( 4 ) );
        assertEquals( vtx21, visitedNodes.get( 5 ) );
        assertEquals( vtx211, visitedNodes.get( 6 ) );
        assertEquals( vtx212, visitedNodes.get( 7 ) );
        assertEquals( vtx22, visitedNodes.get( 8 ) );
        assertEquals( vtx221, visitedNodes.get( 9 ) );
        assertEquals( vtx222, visitedNodes.get( 10 ) );
        assertEquals( vtx223, visitedNodes.get( 11 ) );
        assertEquals( vtx3, visitedNodes.get( 12 ) );
        assertEquals( vtx31, visitedNodes.get( 13 ) );
        assertEquals( vtx32, visitedNodes.get( 14 ) );
        assertEquals( vtx33, visitedNodes.get( 15 ) );

        assertTrue( status.hasCycles( ) );
        assertEquals( 2, status.getCycleCount( ) );
        assertFalse( status.hasErrors( ) );

    }

    @Test
    void depthFirstWithCycleAndWithError( )
    {
        SimpleGraph graph = new SimpleGraph( );
        SimpleNode vtx0 = graph.newNode( "root" );
        SimpleNode vtx1 = graph.newNode( "vertex1" );
        SimpleNode vtx11 = graph.newNode( "vertex11" );
        SimpleNode vtx12 = graph.newNode( "vertex12" );
        SimpleNode vtx2 = graph.newNode( "vertex2" );
        SimpleNode vtx21 = graph.newNode( "vertex21" );
        SimpleNode vtx211 = graph.newNode( "vertex211" );
        SimpleNode vtx212 = graph.newNode( "vertex212" );

        SimpleNode vtx22 = graph.newNode( "vertex22" );
        SimpleNode vtx221 = graph.newNode( "vertex221" );
        SimpleNode vtx222 = graph.newNode( "vertex222" );
        SimpleNode vtx223 = graph.newNode( "vertex223" );

        SimpleNode vtx3 = graph.newNode( "vertex3" );
        SimpleNode vtx31 = graph.newNode( "vertex31" );
        SimpleNode vtx32 = graph.newNode( "vertex32" );
        SimpleNode vtx33 = graph.newNode( "vertex33" );

        graph.newEdge( "root->1", vtx0, vtx1 );
        graph.newEdge( "root->2", vtx0, vtx2 );
        graph.newEdge( "root->3", vtx0, vtx3 );

        graph.newEdge( "1->11", vtx1, vtx11 );
        graph.newEdge( "1->12", vtx1, vtx12 );

        graph.newEdge( "2->21", vtx2, vtx21 );
        graph.newEdge( "2->22", vtx2, vtx22 );
        graph.newEdge( "22->2", vtx22, vtx2 );

        graph.newEdge( "21->211", vtx21, vtx211 );
        graph.newEdge( "21->212", vtx21, vtx212 );

        graph.newEdge( "22->221", vtx22, vtx221 );
        graph.newEdge( "22->222", vtx22, vtx222 );
        graph.newEdge( "22->223", vtx22, vtx223 );
        graph.newEdge( "223->root", vtx223, vtx0 );

        graph.newEdge( "3->31", vtx3, vtx31 );
        graph.newEdge( "3->32", vtx3, vtx32 );
        graph.newEdge( "3->33", vtx3, vtx33 );

        List<SimpleNode> visitedNodes = new ArrayList<>( );
        TraversalStatus<SimpleNode> status = Traversal.depthFirst( vtx0, ( v, s ) -> {
            log.debug( "Visiting vertex " + v );
            if ( visitedNodes.contains( v ) )
            {
                throw new RuntimeException( "Double visit of vertex: " + v );
            }
            visitedNodes.add( v );
            if ( v == vtx223 || v == vtx33 || v == vtx212 )
            {
                throw new RuntimeException( "Error for node " + v );
            }
            return true;
        } );

        assertEquals( vtx0, visitedNodes.get( 0 ) );
        assertEquals( vtx1, visitedNodes.get( 1 ) );
        assertEquals( vtx11, visitedNodes.get( 2 ) );
        assertEquals( vtx12, visitedNodes.get( 3 ) );
        assertEquals( vtx2, visitedNodes.get( 4 ) );
        assertEquals( vtx21, visitedNodes.get( 5 ) );
        assertEquals( vtx211, visitedNodes.get( 6 ) );
        assertEquals( vtx212, visitedNodes.get( 7 ) );
        assertEquals( vtx22, visitedNodes.get( 8 ) );
        assertEquals( vtx221, visitedNodes.get( 9 ) );
        assertEquals( vtx222, visitedNodes.get( 10 ) );
        assertEquals( vtx223, visitedNodes.get( 11 ) );
        assertEquals( vtx3, visitedNodes.get( 12 ) );
        assertEquals( vtx31, visitedNodes.get( 13 ) );
        assertEquals( vtx32, visitedNodes.get( 14 ) );
        assertEquals( vtx33, visitedNodes.get( 15 ) );

        assertTrue( status.hasCycles( ) );
        assertEquals( 2, status.getCycleCount( ) );
        assertTrue( status.hasErrors( ) );
        assertEquals( 3, status.getErrorList( ).size( ) );
        assertTrue( status.getErrorList( ).get( 0 ).getException( ).getMessage( ).startsWith( "Error for node" ) );

    }

    @Test
    void hasCycle( )
    {
        SimpleGraph graph = new SimpleGraph( );
        SimpleNode vtx0 = graph.newNode( "root" );
        SimpleNode vtx1 = graph.newNode( "vertex1" );
        SimpleNode vtx11 = graph.newNode( "vertex11" );
        SimpleNode vtx12 = graph.newNode( "vertex12" );
        SimpleNode vtx2 = graph.newNode( "vertex2" );
        SimpleNode vtx21 = graph.newNode( "vertex21" );
        SimpleNode vtx211 = graph.newNode( "vertex211" );
        SimpleNode vtx212 = graph.newNode( "vertex212" );

        SimpleNode vtx22 = graph.newNode( "vertex22" );
        SimpleNode vtx221 = graph.newNode( "vertex221" );
        SimpleNode vtx222 = graph.newNode( "vertex222" );
        SimpleNode vtx223 = graph.newNode( "vertex223" );

        SimpleNode vtx3 = graph.newNode( "vertex3" );
        SimpleNode vtx31 = graph.newNode( "vertex31" );
        SimpleNode vtx32 = graph.newNode( "vertex32" );
        SimpleNode vtx33 = graph.newNode( "vertex33" );

        graph.newEdge( "root->1", vtx0, vtx1 );
        graph.newEdge( "root->2", vtx0, vtx2 );
        graph.newEdge( "root->3", vtx0, vtx3 );

        graph.newEdge( "1->11", vtx1, vtx11 );
        graph.newEdge( "1->12", vtx1, vtx12 );

        graph.newEdge( "2->21", vtx2, vtx21 );
        graph.newEdge( "2->22", vtx2, vtx22 );
        graph.newEdge( "22->2", vtx22, vtx2 );

        graph.newEdge( "21->211", vtx21, vtx211 );
        graph.newEdge( "21->212", vtx21, vtx212 );

        graph.newEdge( "22->221", vtx22, vtx221 );
        graph.newEdge( "22->222", vtx22, vtx222 );
        graph.newEdge( "22->223", vtx22, vtx223 );
        graph.newEdge( "223->root", vtx223, vtx0 );

        graph.newEdge( "3->31", vtx3, vtx31 );
        graph.newEdge( "3->32", vtx3, vtx32 );
        graph.newEdge( "3->33", vtx3, vtx33 );

        assertTrue( Traversal.hasCycle( vtx0 ) );
        assertFalse( Traversal.hasCycle( vtx1 ) );
    }

    @Test
    void breadthFirstWithoutCyclesAndWithoutError( )
    {
        SimpleGraph graph = new SimpleGraph( );
        SimpleNode vtx0 = graph.newNode( "root" );
        SimpleNode vtx1 = graph.newNode( "vertex1" );
        SimpleNode vtx11 = graph.newNode( "vertex11" );
        SimpleNode vtx12 = graph.newNode( "vertex12" );
        SimpleNode vtx2 = graph.newNode( "vertex2" );
        SimpleNode vtx21 = graph.newNode( "vertex21" );
        SimpleNode vtx211 = graph.newNode( "vertex211" );
        SimpleNode vtx212 = graph.newNode( "vertex212" );

        SimpleNode vtx22 = graph.newNode( "vertex22" );
        SimpleNode vtx221 = graph.newNode( "vertex221" );
        SimpleNode vtx222 = graph.newNode( "vertex222" );
        SimpleNode vtx223 = graph.newNode( "vertex223" );

        SimpleNode vtx3 = graph.newNode( "vertex3" );
        SimpleNode vtx31 = graph.newNode( "vertex31" );
        SimpleNode vtx32 = graph.newNode( "vertex32" );
        SimpleNode vtx33 = graph.newNode( "vertex33" );

        graph.newEdge( "root->1", vtx0, vtx1 );
        graph.newEdge( "root->2", vtx0, vtx2 );
        graph.newEdge( "root->3", vtx0, vtx3 );

        graph.newEdge( "1->11", vtx1, vtx11 );
        graph.newEdge( "1->12", vtx1, vtx12 );

        graph.newEdge( "2->21", vtx2, vtx21 );
        graph.newEdge( "2->22", vtx2, vtx22 );

        graph.newEdge( "21->211", vtx21, vtx211 );
        graph.newEdge( "21->212", vtx21, vtx212 );

        graph.newEdge( "22->221", vtx22, vtx221 );
        graph.newEdge( "22->222", vtx22, vtx222 );
        graph.newEdge( "22->223", vtx22, vtx223 );

        graph.newEdge( "3->31", vtx3, vtx31 );
        graph.newEdge( "3->32", vtx3, vtx32 );
        graph.newEdge( "3->33", vtx3, vtx33 );

        List<SimpleNode> visitedNodes = new ArrayList<>( );
        TraversalStatus<SimpleNode> status = Traversal.breadthFirst( vtx0, ( v, s ) -> {
            log.debug( "Visiting vertex " + v );
            if ( visitedNodes.contains( v ) )
            {
                throw new RuntimeException( "Double visit of vertex: " + v );
            }
            visitedNodes.add( v );
            return true;
        } );

        assertEquals( vtx0, visitedNodes.get( 0 ) );
        assertEquals( vtx1, visitedNodes.get( 1 ) );
        assertEquals( vtx2, visitedNodes.get( 2 ) );
        assertEquals( vtx3, visitedNodes.get( 3 ) );
        assertEquals( vtx11, visitedNodes.get( 4 ) );
        assertEquals( vtx12, visitedNodes.get( 5 ) );
        assertEquals( vtx21, visitedNodes.get( 6 ) );
        assertEquals( vtx22, visitedNodes.get( 7 ) );
        assertEquals( vtx31, visitedNodes.get( 8 ) );
        assertEquals( vtx32, visitedNodes.get( 9 ) );
        assertEquals( vtx33, visitedNodes.get( 10 ) );
        assertEquals( vtx211, visitedNodes.get( 11 ) );
        assertEquals( vtx212, visitedNodes.get( 12 ) );
        assertEquals( vtx221, visitedNodes.get( 13 ) );
        assertEquals( vtx222, visitedNodes.get( 14 ) );
        assertEquals( vtx223, visitedNodes.get( 15 ) );

        assertFalse( status.hasCycles( ) );
        assertFalse( status.hasErrors( ) );
    }

    @Test
    void breadthFirstWithCyclesAndWithoutError( )
    {
        SimpleGraph graph = new SimpleGraph( );
        SimpleNode vtx0 = graph.newNode( "root" );
        SimpleNode vtx1 = graph.newNode( "vertex1" );
        SimpleNode vtx11 = graph.newNode( "vertex11" );
        SimpleNode vtx12 = graph.newNode( "vertex12" );
        SimpleNode vtx2 = graph.newNode( "vertex2" );
        SimpleNode vtx21 = graph.newNode( "vertex21" );
        SimpleNode vtx211 = graph.newNode( "vertex211" );
        SimpleNode vtx212 = graph.newNode( "vertex212" );

        SimpleNode vtx22 = graph.newNode( "vertex22" );
        SimpleNode vtx221 = graph.newNode( "vertex221" );
        SimpleNode vtx222 = graph.newNode( "vertex222" );
        SimpleNode vtx223 = graph.newNode( "vertex223" );

        SimpleNode vtx3 = graph.newNode( "vertex3" );
        SimpleNode vtx31 = graph.newNode( "vertex31" );
        SimpleNode vtx32 = graph.newNode( "vertex32" );
        SimpleNode vtx33 = graph.newNode( "vertex33" );

        graph.newEdge( "root->1", vtx0, vtx1 );
        graph.newEdge( "root->2", vtx0, vtx2 );
        graph.newEdge( "root->3", vtx0, vtx3 );

        graph.newEdge( "1->11", vtx1, vtx11 );
        graph.newEdge( "1->12", vtx1, vtx12 );

        graph.newEdge( "2->21", vtx2, vtx21 );
        graph.newEdge( "2->22", vtx2, vtx22 );

        graph.newEdge( "21->211", vtx21, vtx211 );
        graph.newEdge( "21->212", vtx21, vtx212 );
        graph.newEdge( "211->21", vtx211, vtx21 );

        graph.newEdge( "22->221", vtx22, vtx221 );
        graph.newEdge( "22->222", vtx22, vtx222 );
        graph.newEdge( "22->223", vtx22, vtx223 );
        graph.newEdge( "223->root", vtx223, vtx0 );

        graph.newEdge( "3->31", vtx3, vtx31 );
        graph.newEdge( "3->32", vtx3, vtx32 );
        graph.newEdge( "3->33", vtx3, vtx33 );

        List<SimpleNode> visitedNodes = new ArrayList<>( );
        TraversalStatus<SimpleNode> status = Traversal.breadthFirst( vtx0, ( v, s ) -> {
            log.debug( "Visiting vertex " + v );
            if ( visitedNodes.contains( v ) )
            {
                throw new RuntimeException( "Double visit of vertex: " + v );
            }
            visitedNodes.add( v );
            return true;
        }, new TraversalFlags( false, true ) );

        assertEquals( vtx0, visitedNodes.get( 0 ) );
        assertEquals( vtx1, visitedNodes.get( 1 ) );
        assertEquals( vtx2, visitedNodes.get( 2 ) );
        assertEquals( vtx3, visitedNodes.get( 3 ) );
        assertEquals( vtx223, visitedNodes.get( 4 ) );
        assertEquals( vtx11, visitedNodes.get( 5 ) );
        assertEquals( vtx12, visitedNodes.get( 6 ) );
        assertEquals( vtx21, visitedNodes.get( 7 ) );
        assertEquals( vtx22, visitedNodes.get( 8 ) );
        assertEquals( vtx31, visitedNodes.get( 9 ) );
        assertEquals( vtx32, visitedNodes.get( 10 ) );
        assertEquals( vtx33, visitedNodes.get( 11 ) );
        assertEquals( vtx211, visitedNodes.get( 12 ) );
        assertEquals( vtx212, visitedNodes.get( 13 ) );
        assertEquals( vtx221, visitedNodes.get( 14 ) );
        assertEquals( vtx222, visitedNodes.get( 15 ) );


        assertTrue( status.hasCycles( ) );
        assertEquals( 19, status.getCycleCount( ) );
        assertFalse( status.hasErrors( ) );
    }

    @Test
    void breadthFirstWithCyclesAndWithError( )
    {
        SimpleGraph graph = new SimpleGraph( );
        SimpleNode vtx0 = graph.newNode( "root" );
        SimpleNode vtx1 = graph.newNode( "vertex1" );
        SimpleNode vtx11 = graph.newNode( "vertex11" );
        SimpleNode vtx12 = graph.newNode( "vertex12" );
        SimpleNode vtx2 = graph.newNode( "vertex2" );
        SimpleNode vtx21 = graph.newNode( "vertex21" );
        SimpleNode vtx211 = graph.newNode( "vertex211" );
        SimpleNode vtx212 = graph.newNode( "vertex212" );

        SimpleNode vtx22 = graph.newNode( "vertex22" );
        SimpleNode vtx221 = graph.newNode( "vertex221" );
        SimpleNode vtx222 = graph.newNode( "vertex222" );
        SimpleNode vtx223 = graph.newNode( "vertex223" );

        SimpleNode vtx3 = graph.newNode( "vertex3" );
        SimpleNode vtx31 = graph.newNode( "vertex31" );
        SimpleNode vtx32 = graph.newNode( "vertex32" );
        SimpleNode vtx33 = graph.newNode( "vertex33" );

        graph.newEdge( "root->1", vtx0, vtx1 );
        graph.newEdge( "root->2", vtx0, vtx2 );
        graph.newEdge( "root->3", vtx0, vtx3 );

        graph.newEdge( "1->11", vtx1, vtx11 );
        graph.newEdge( "1->12", vtx1, vtx12 );

        graph.newEdge( "2->21", vtx2, vtx21 );
        graph.newEdge( "2->22", vtx2, vtx22 );

        graph.newEdge( "21->211", vtx21, vtx211 );
        graph.newEdge( "21->212", vtx21, vtx212 );
        graph.newEdge( "211->21", vtx211, vtx21 );

        graph.newEdge( "22->221", vtx22, vtx221 );
        graph.newEdge( "22->222", vtx22, vtx222 );
        graph.newEdge( "22->223", vtx22, vtx223 );
        graph.newEdge( "223->root", vtx223, vtx0 );

        graph.newEdge( "3->31", vtx3, vtx31 );
        graph.newEdge( "3->32", vtx3, vtx32 );
        graph.newEdge( "3->33", vtx3, vtx33 );

        List<SimpleNode> visitedNodes = new ArrayList<>( );
        TraversalStatus<SimpleNode> status = Traversal.breadthFirst( vtx0, ( v, s ) -> {
            log.debug( "Visiting vertex " + v );
            if ( visitedNodes.contains( v ) )
            {
                throw new RuntimeException( "Double visit of vertex: " + v );
            }
            visitedNodes.add( v );
            if ( v == vtx212 || v == vtx31 || v == vtx21 )
            {
                throw new RuntimeException( "Error on node: " + v );
            }
            return true;
        }, new TraversalFlags( false, true ) );

        assertEquals( vtx0, visitedNodes.get( 0 ) );
        assertEquals( vtx1, visitedNodes.get( 1 ) );
        assertEquals( vtx2, visitedNodes.get( 2 ) );
        assertEquals( vtx3, visitedNodes.get( 3 ) );
        assertEquals( vtx223, visitedNodes.get( 4 ) );
        assertEquals( vtx11, visitedNodes.get( 5 ) );
        assertEquals( vtx12, visitedNodes.get( 6 ) );
        assertEquals( vtx21, visitedNodes.get( 7 ) );
        assertEquals( vtx22, visitedNodes.get( 8 ) );
        assertEquals( vtx31, visitedNodes.get( 9 ) );
        assertEquals( vtx32, visitedNodes.get( 10 ) );
        assertEquals( vtx33, visitedNodes.get( 11 ) );
        assertEquals( vtx211, visitedNodes.get( 12 ) );
        assertEquals( vtx212, visitedNodes.get( 13 ) );
        assertEquals( vtx221, visitedNodes.get( 14 ) );
        assertEquals( vtx222, visitedNodes.get( 15 ) );

        assertTrue( status.hasCycles( ) );
        // Undirected graph
        assertEquals( 19, status.getCycleCount( ) );
        assertTrue( status.hasErrors( ) );
        assertEquals( 3, status.getErrorList( ).size( ) );
        assertTrue( status.getErrorList( ).get( 0 ).getException( ).getMessage( ).startsWith( "Error on node" ) );
    }


    @Test
    void topologicalSort( )
    {
        SimpleGraph graph = new SimpleGraph( );
        SimpleNode vtx0 = graph.newNode( "root" );
        SimpleNode vtx1 = graph.newNode( "vertex1" );
        SimpleNode vtx11 = graph.newNode( "vertex11" );
        SimpleNode vtx12 = graph.newNode( "vertex12" );
        SimpleNode vtx2 = graph.newNode( "vertex2" );
        SimpleNode vtx21 = graph.newNode( "vertex21" );
        SimpleNode vtx211 = graph.newNode( "vertex211" );
        SimpleNode vtx212 = graph.newNode( "vertex212" );

        SimpleNode vtx22 = graph.newNode( "vertex22" );
        SimpleNode vtx221 = graph.newNode( "vertex221" );
        SimpleNode vtx222 = graph.newNode( "vertex222" );
        SimpleNode vtx223 = graph.newNode( "vertex223" );

        SimpleNode vtx3 = graph.newNode( "vertex3" );
        SimpleNode vtx31 = graph.newNode( "vertex31" );
        SimpleNode vtx32 = graph.newNode( "vertex32" );
        SimpleNode vtx33 = graph.newNode( "vertex33" );
        SimpleNode vtx331 = graph.newNode( "vertex331" );
        SimpleNode vtx3311 = graph.newNode( "vertex3311" );


        graph.newEdge( "root->1", vtx0, vtx1 );
        graph.newEdge( "root->2", vtx0, vtx2 );
        graph.newEdge( "root->3", vtx0, vtx3 );

        graph.newEdge( "1->11", vtx1, vtx11 );
        graph.newEdge( "1->12", vtx1, vtx12 );

        graph.newEdge( "2->21", vtx2, vtx21 );
        graph.newEdge( "2->22", vtx2, vtx22 );
        graph.newEdge( "22->2", vtx22, vtx2 );

        graph.newEdge( "21->211", vtx21, vtx211 );
        graph.newEdge( "21->212", vtx21, vtx212 );

        graph.newEdge( "22->221", vtx22, vtx221 );
        graph.newEdge( "22->222", vtx22, vtx222 );
        graph.newEdge( "22->223", vtx22, vtx223 );
        graph.newEdge( "223->root", vtx223, vtx0 );

        graph.newEdge( "3->31", vtx3, vtx31 );
        graph.newEdge( "3->32", vtx3, vtx32 );
        graph.newEdge( "3->33", vtx3, vtx33 );
        graph.newEdge( "33->331", vtx33, vtx331 );
        graph.newEdge( "331->3311", vtx331, vtx3311 );
        graph.newEdge( "22->33", vtx22, vtx33 );
        // graph.newEdge("33->22", vtx33, vtx22);


        List<SimpleNode> result = Traversal.topologialSort( vtx0 );

        assertEquals( vtx11, result.get( 0 ) );
        assertEquals( vtx12, result.get( 1 ) );
        assertEquals( vtx1, result.get( 2 ) );
        assertEquals( vtx211, result.get( 3 ) );
        assertEquals( vtx212, result.get( 4 ) );
        assertEquals( vtx21, result.get( 5 ) );
        assertEquals( vtx221, result.get( 6 ) );
        assertEquals( vtx222, result.get( 7 ) );
        assertEquals( vtx223, result.get( 8 ) );
        assertEquals( vtx3311, result.get( 9 ) );
        assertEquals( vtx331, result.get( 10 ) );
        assertEquals( vtx33, result.get( 11 ) );
        assertEquals( vtx22, result.get( 12 ) );
        assertEquals( vtx2, result.get( 13 ) );
        assertEquals( vtx31, result.get( 14 ) );
        assertEquals( vtx32, result.get( 15 ) );
        assertEquals( vtx3, result.get( 16 ) );
        assertEquals( vtx0, result.get( 17 ) );


    }


}