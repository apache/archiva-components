package org.apache.archiva.components.cache.test.examples.wine;

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

import java.io.Serializable;

/**
 * @author Olivier Lamy
 * @since 5 February, 2007
 */
public class Wine
    implements Serializable
{
    private String name;

    private String localisation;

    public Wine( String name, String localisation )
    {
        this.name = name;
        this.localisation = localisation;
    }

    public String getLocalisation( )
    {
        return localisation;
    }

    public void setLocalisation( String localisation )
    {
        this.localisation = localisation;
    }

    public String getName( )
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    @Override
    public String toString( )
    {
        final StringBuilder sb = new StringBuilder( "Wine{" );
        sb.append( "name='" ).append( name ).append( '\'' );
        sb.append( ", localisation='" ).append( localisation ).append( '\'' );
        sb.append( '}' );
        return sb.toString( );
    }
}
