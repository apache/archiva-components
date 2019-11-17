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

import org.apache.archiva.components.cache.builder.CacheBuilder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Olivier Lamy
 * @since 5 February, 2007
 */
@Service
public class DefaultWineService
    implements WineService
{
    @Inject
    private CacheBuilder<String, Wine> cacheBuilder;

    @Inject
    private WineDao wineDao;

    public Wine getWine( String name )
    {
        Wine wine = cacheBuilder.getCache( Wine.class ).get( name );
        if ( wine == null )
        {
            wine = wineDao.getWine( name );
            cacheBuilder.getCache( Wine.class ).put( name, wine );
        }
        return wine;
    }

}
