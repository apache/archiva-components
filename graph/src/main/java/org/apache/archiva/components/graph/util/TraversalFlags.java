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

/**
 * Flags that control traversal behaviour
 */
public class TraversalFlags
{
    private final boolean directed;
    private final boolean continueOnError;

    public TraversalFlags( )
    {
        this.directed = true;
        this.continueOnError = true;
    }

    public TraversalFlags( boolean directed, boolean continueOnError )
    {
        this.directed = directed;
        this.continueOnError = continueOnError;
    }

    public TraversalFlags( boolean directed, boolean continueOnError, boolean consumeParentAfterChild )
    {
        this.directed = directed;
        this.continueOnError = continueOnError;
    }

    /**
     * If the <code>directed=true</code>, traversal will only use source to destination links not reverse.
     * If the value is <code>false</code> traversal will use every edge from a given node regardless of the
     * direction.
     * Default is <code>true</code>
     *
     * @return true, if the graph is directed otherwise false
     */
    public boolean isDirected( )
    {
        return directed;
    }

    /**
     * If this flag is <code>true</code>, the traversal will continue, if the consumer function
     * throws an error. Otherwise the traversal will be stopped.
     * Default is <code>true</code>
     *
     * @return <code>true</code>, if traversal should continue on error, otherwise <code>false</code>
     */
    public boolean isContinueOnError( )
    {
        return continueOnError;
    }


}
