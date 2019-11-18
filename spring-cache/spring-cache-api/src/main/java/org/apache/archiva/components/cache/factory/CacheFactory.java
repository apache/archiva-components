package org.apache.archiva.components.cache.factory;

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

import org.apache.archiva.components.cache.Cache;
import org.apache.archiva.components.cache.CacheException;
import org.apache.archiva.components.cache.CacheHints;
import org.apache.archiva.components.cache.impl.NoCacheCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * CacheFactory - dynamic cache creation (and tracking) facility for non-plexus objects to use.
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 */
public class CacheFactory
{
    public static final String PROP_FILE = "META-INF/archiva-cache.properties";

    static class CacheFactoryHolder
    {
        static CacheFactory instance = new CacheFactory( );
    }

    private static Map caches;

    private static CacheCreator creator;

    private Logger logger = LoggerFactory.getLogger( getClass( ) );

    private CacheFactory( )
    {
        caches = new HashMap( );

        try
        {
            ClassLoader classLoader = this.getClass( ).getClassLoader( );

            if ( classLoader == null )
            {
                classLoader = ClassLoader.getSystemClassLoader( );
            }

            Enumeration cachePropResources = classLoader.getResources( PROP_FILE );

            if ( cachePropResources.hasMoreElements( ) )
            {
                URL propURL = (URL) cachePropResources.nextElement( );
                Properties props = new Properties( );

                props.load( propURL.openStream( ) );
                String creatorImpl = props.getProperty( "cache.creator" );

                Class creatorClass = classLoader.loadClass( creatorImpl );
                creator = (CacheCreator) creatorClass.newInstance( );
            }

            if ( cachePropResources.hasMoreElements( ) )
            {
                logger.error( "More than 1 CacheCreator provider exists in classpath. Using first one found [{}].",
                    creator.getClass( ).getName( ) );
            }
        }
        catch ( IOException e )
        {
            throw new ExceptionInInitializerError( e );
        }
        catch ( ClassNotFoundException e )
        {
            throw new ExceptionInInitializerError( e );
        }
        catch ( InstantiationException e )
        {
            throw new ExceptionInInitializerError( e );
        }
        catch ( IllegalAccessException e )
        {
            throw new ExceptionInInitializerError( e );
        }
    }

    public static CacheFactory getInstance( )
    {
        return CacheFactoryHolder.instance;
    }

    public void setCacheCreatorFactory( CacheCreator creator )
    {
        CacheFactory.creator = creator;
    }

    public Cache getCache( String id, CacheHints hints )
        throws CacheException
    {
        if ( creator == null )
        {
            return new NoCacheCache( );
        }

        if ( caches.containsKey( id ) )
        {
            return (Cache) caches.get( id );
        }

        if ( hints == null )
        {
            // Setup some defaults.
            hints = new CacheHints( );
            hints.setName( id );
        }

        Cache cache = CacheFactory.creator.createCache( hints );

        caches.put( id, cache );
        return (Cache) cache;
    }
}
