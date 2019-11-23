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

import java.util.ArrayList;
import java.util.List;

/**
 * This class gives information about the status of the graph traversal.
 * It holds the errors encountered and information about detected cycles.
 *
 * @param <V>
 */
public class TraversalStatus<V extends Node<V>>
{

    private int cycles = 0;
    private List<VisitError<V>> errorList;
    private List<V> cycleNodes;

    public TraversalStatus( )
    {

    }

    /**
     * Returns the list of errors
     *
     * @return a list of errors encountered while executing the consumer function on nodes
     */
    public List<VisitError<V>> getErrorList( )
    {
        return errorList;
    }

    /**
     * Returns true, if there were errors while running the consumer function.
     *
     * @return true, if errors occured on the consumer function, otherwise false
     */
    public boolean hasErrors( )
    {
        return errorList != null && errorList.size( ) > 0;
    }

    /**
     * Add the given error to the list.
     *
     * @param node the node, where the error occurred
     * @param e    the exception
     */
    public void addError( V node, Throwable e )
    {
        if ( errorList == null )
        {
            errorList = new ArrayList<>( );
        }
        errorList.add( new VisitError( node, e ) );
    }

    /**
     * Add another cycle to the counter
     *
     * @param node
     */
    public void registerCycle( V node )
    {
        cycles++;
        if ( cycleNodes == null )
        {
            cycleNodes = new ArrayList<>( );
        }
        cycleNodes.add( node );
    }

    /**
     * Returns true, if the traversal encountered a cycle.
     *
     * @return true, if cycle was encountered, otherwise false
     */
    public boolean hasCycles( )
    {
        return cycles > 0;
    }

    /**
     * Returns the number of detected cycles.
     *
     * @return the number of cycles
     */
    public int getCycleCount( )
    {
        return cycles;
    }

    /**
     * Returns the nodes, where cycles were detected. The list may contain
     * duplicated entries, if cycles where detected on different paths.
     *
     * @return the list of nodes where cycles were detected
     */
    public List<V> getCycleNodes( )
    {
        return cycleNodes;
    }
}
