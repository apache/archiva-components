package org.apache.archiva.components.cache.ehcache;

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
import org.apache.archiva.components.cache.factory.CacheCreator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * EhcacheCreator - runtime creation of an ehcache.
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 */
public class EhcacheCreator
    implements CacheCreator
{

    @Override
    public Cache createCache( CacheHints hints )
        throws CacheException
    {
        EhcacheCache cache = new EhcacheCache( );

        cache.setName( hints.getName( ) );

        cache.setDiskPersistent( hints.isOverflowToDisk( ) );
        if ( hints.isOverflowToDisk( ) )
        {
            Path overflowPath;

            if ( hints.getDiskOverflowPath( ) != null )
            {
                overflowPath = hints.getDiskOverflowPath( ).toPath( );
            }
            else
            {
                try
                {
                    overflowPath = Files.createTempDirectory( "ehcache-archiva-" + hints.getName( ) );
                }
                catch ( IOException e )
                {
                    throw new CacheException( e );
                }
            }

            cache.setDiskStorePath( overflowPath.toAbsolutePath( ).toString( ) );
        }

        cache.setMaxElementsInMemory( hints.getMaxElements( ) );
        cache.setTimeToLiveSeconds( hints.getMaxSecondsInCache( ) );
        cache.setTimeToIdleSeconds( hints.getIdleExpirationSeconds( ) );

        cache.initialize( );

        return cache;
    }
}
