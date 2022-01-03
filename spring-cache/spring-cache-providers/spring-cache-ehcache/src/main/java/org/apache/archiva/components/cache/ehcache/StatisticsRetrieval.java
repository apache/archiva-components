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
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import org.ehcache.core.spi.service.StatisticsService;
import org.ehcache.spi.service.Service;
import org.ehcache.spi.service.ServiceDependencies;
import org.ehcache.spi.service.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

/**
 *
 * EhCache Service class that retrieves the Statistics instance.
 *
 * @author Martin Schreier <martin_s@apache.org>
 */
@ServiceDependencies( StatisticsService.class )
public class StatisticsRetrieval implements Service
{
    private static final Logger log = LoggerFactory.getLogger( StatisticsRetrieval.class );

    private StatisticsService statisticsService;

    @Override
    public void start( ServiceProvider<Service> serviceProvider) {
        log.info( "Starting Statistics Retrieval" );
        this.statisticsService = serviceProvider.getService(StatisticsService.class);
    }

    @Override
    public void stop() {
        this.statisticsService = null;
    }

    public StatisticsService getStatisticsService() {
        return requireNonNull(statisticsService);
    }
}
