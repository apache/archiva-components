package org.apache.archiva.components.cache.oscache;

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
import org.apache.commons.lang3.SystemUtils;

import java.io.File;

/**
 * OsCacheCreator
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 */
public class OsCacheCreator
    implements CacheCreator
{

    public Cache createCache( CacheHints hints )
        throws CacheException
    {
        OsCacheCache cache = new OsCacheCache( );

        cache.setCacheKey( hints.getName( ) );

        cache.setCachePersistenceOverflowOnly( hints.isOverflowToDisk( ) );
        if ( hints.isOverflowToDisk( ) )
        {
            File overflowPath = null;

            if ( hints.getDiskOverflowPath( ) != null )
            {
                overflowPath = hints.getDiskOverflowPath( );
            }
            else
            {
                File tmpDir = SystemUtils.getJavaIoTmpDir( );
                overflowPath = new File( tmpDir, "oscache/" + hints.getName( ) );
            }

            cache.setCachePath( overflowPath.getAbsolutePath( ) );
        }

        cache.setCapacity( hints.getMaxElements( ) );
        cache.setRefreshPeriod( hints.getIdleExpirationSeconds( ) );

        // Does not support:  hints.getMaxSecondsInCache()

        cache.initialize( );

        return cache;
    }

}
