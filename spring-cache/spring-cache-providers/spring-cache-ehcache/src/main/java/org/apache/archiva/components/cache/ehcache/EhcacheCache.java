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

import org.apache.archiva.components.cache.CacheStatistics;
import org.ehcache.Cache;
import org.ehcache.PersistentCacheManager;
import org.ehcache.StateTransitionException;
import org.ehcache.Status;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.core.spi.service.StatisticsService;
import org.ehcache.expiry.ExpiryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EhcacheCache
 * configuration document available <a href="http://www.ehcache.org/documentation/configuration/index">EhcacheUserGuide</a>
 * <p>
 * You can use the system property <code>org.apache.archiva.ehcache.diskStore</code> to set the default disk store path.
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 */
public class EhcacheCache<V, T>
    implements org.apache.archiva.components.cache.Cache<V, T>
{

    private static final String EHCACHE_DISK_STORE_PROPERTY = "org.apache.archiva.ehcache.diskStore";

    private static final Logger log = LoggerFactory.getLogger( EhcacheCache.class );

    private final Class<V> keyType;
    private final Class<T> valueType;

    public EhcacheCache( Class<V> keyType, Class<T> valueType )
    {
        this.keyType = keyType;
        this.valueType = valueType;
    }

    static class Stats
        implements CacheStatistics
    {
        private boolean useBaseLine = false;
        private long hitCountBL = 0;
        private long missCountBL = 0;
        private long sizeBL = 0;
        private long localHeapSizeInBytesBL = 0;
        private final String cacheName;
        private final StatisticsRetrieval svc;

        public Stats( StatisticsRetrieval svc, String cacheName )
        {
            this.cacheName = cacheName;
            this.svc = svc;
        }

        // No API for cache clear since 2.10. We use a baseline, if the cache is cleared.
        @Override
        public void clear( )
        {
            useBaseLine = true;
            org.ehcache.core.statistics.CacheStatistics cStats = getStats( );
            hitCountBL = cStats.getCacheHits( );
            missCountBL = cStats.getCacheMisses( );
            sizeBL = cStats.getTierStatistics( ).size( );
            localHeapSizeInBytesBL = cStats.getTierStatistics( ).get( "OnHeap" ).getAllocatedByteSize( );
        }

        @Override
        public double getCacheHitRate( )
        {
            final double hits = getCacheHits( );
            final double miss = getCacheMiss( );

            if ( ( hits < 0.1 ) && ( miss < 0.1 ) )
            {
                return 0.0;
            }

            return hits / ( hits + miss );
        }

        private org.ehcache.core.statistics.CacheStatistics getStats( )
        {
            return svc.getStatisticsService( ).getCacheStatistics( this.cacheName );
        }

        @Override
        public long getCacheHits( )
        {
            long hits = getStats( ).getCacheHits( );
            return useBaseLine ? hits - hitCountBL : hits;
        }

        @Override
        public long getCacheMiss( )
        {
            long misses = getStats( ).getCacheMisses( );
            return useBaseLine ? misses - missCountBL : misses;
        }

        @Override
        public long getSize( )
        {
            long size = getStats( ).getTierStatistics( ).size( );
            return useBaseLine ? size - sizeBL : size;
        }

        @Override
        public long getInMemorySize( )
        {
            long memSize = getStats( ).getTierStatistics( ).get( "OnHeap" ).getAllocatedByteSize( );
            return useBaseLine ? memSize - localHeapSizeInBytesBL : memSize;
        }

        public StatisticsService getService( )
        {
            return svc.getStatisticsService( );
        }
    }

    private static class ManagerData
    {
        final PersistentCacheManager cacheManager;
        final StatisticsRetrieval statisticsRetrieval;
        final HashSet<String> cacheNames = new HashSet<>( );

        ManagerData( PersistentCacheManager cacheManager, StatisticsRetrieval statisticsRetrieval )
        {
            this.cacheManager = cacheManager;
            this.statisticsRetrieval = statisticsRetrieval;
        }

    }

    /**
     * how often to run the disk store expiry thread. A large number of 120 seconds plus is recommended
     */
    private long diskExpiryThreadIntervalSeconds = 600;

    /**
     * Whether to persist the cache to disk between JVM restarts.
     */
    private boolean diskPersistent = true;

    /**
     * Location on disk for the ehcache store.
     */
    private Path diskStorePath = Paths.get( System.getProperties( ).containsKey( EHCACHE_DISK_STORE_PROPERTY ) ?
        System.getProperty( EHCACHE_DISK_STORE_PROPERTY ) :
        System.getProperty( "java.io.tmpdir" ) + "/ehcache-archiva" ).toAbsolutePath( );

    private boolean eternal = false;

    private int maxElementsInMemory = 0;

    private String memoryEvictionPolicy = "LRU";

    private String name = "cache";

    private String registeredName;
    private Path registeredPath;

    /**
     * Flag indicating when to use the disk store.
     */
    private boolean overflowToDisk = false;

    private int timeToIdleSeconds = 600;

    private int timeToLiveSeconds = 300;

    /**
     * @since 2.0
     */
    private boolean overflowToOffHeap = false;

    /**
     * @since 2.0
     */
    private long maxBytesLocalHeap;

    /**
     * @since 2.0
     */
    private long maxBytesLocalOffHeap;

    private boolean failOnDuplicateCache = false;

    /**
     * @since 2.1
     */
    private int maxElementsOnDisk;

    private boolean statisticsEnabled = true;

    private Path configurationFile = null;

    private Cache<V, T> ehcache;

    private Stats stats;

    private static final ConcurrentHashMap<Path, ManagerData> cacheManagerRefs = new ConcurrentHashMap<>( );


    @Override
    public void clear( )
    {
        if ( ehcache != null )
        {
            ehcache.clear( );
        }
        if ( stats != null )
        {
            stats.clear( );
        }
    }

    @PostConstruct
    public void initialize( )
    {
        // We are skipping the update check if not set explicitly
        if ( !System.getProperties( ).containsKey( "net.sf.ehcache.skipUpdateCheck" ) )
        {
            System.setProperty( "net.sf.ehcache.skipUpdateCheck", "true" );
        }
        this.registeredName = getName( );
        Path storePath = getDiskStorePath( );
        this.registeredPath = storePath;
        ManagerData md = cacheManagerRefs.computeIfAbsent( this.registeredPath, ( key ) -> {
            StatisticsRetrieval retrieval = new StatisticsRetrieval( );
            return new ManagerData( initCacheManager( retrieval, this.registeredPath ), retrieval );
        } );

        this.stats = new Stats( md.statisticsRetrieval, this.registeredName );

        final PersistentCacheManager cacheManager = md.cacheManager;

        Cache<V, T> cCache = cacheManager.getCache( registeredName, keyType, valueType );

        if ( cCache != null )
        {
            if ( failOnDuplicateCache )
            {
                throw new RuntimeException( "A previous cache with name [" + registeredName + "] exists." );
            }
            else
            {
                log.warn( "skip duplicate cache {}", registeredName );
                this.ehcache = cCache;
            }
        }
        else
        {
            int diskSize = getMaxElementsOnDisk( ) > 0 ? getMaxElementsOnDisk( ) : 100;
            int memElements = getMaxElementsInMemory( ) > 0 ? getMaxElementsInMemory( ) : 1;
            ResourcePoolsBuilder rpBuilder = ResourcePoolsBuilder
                .heap( memElements ).disk( diskSize, MemoryUnit.MB, isDiskPersistent( ) );
            if ( isOverflowToOffHeap( ) )
            {
                rpBuilder.offheap( getMaxBytesLocalOffHeap( ), MemoryUnit.B );
            }
            log.info( "Creating cache {}", registeredName );
            this.ehcache = cacheManager.createCache( this.registeredName, CacheConfigurationBuilder.newCacheConfigurationBuilder( keyType, valueType, rpBuilder )
                .withExpiry( getExpiry( ) )
                .build( ) );

            md.cacheNames.add( this.registeredName );
        }
    }

    ExpiryPolicy getExpiry( )
    {
        int ttl = getTimeToLiveSeconds( );
        int tti = getTimeToIdleSeconds( );
        if ( ttl <= 0 && tti <= 0 )
        {
            return ExpiryPolicy.NO_EXPIRY;
        }
        if ( ttl <= 0 && tti > 0 )
        {
            return ExpiryPolicyBuilder.timeToIdleExpiration( Duration.ofSeconds( tti ) );
        }
        if ( ttl > 0 && tti <= 0 )
        {
            return ExpiryPolicyBuilder.timeToLiveExpiration( Duration.ofSeconds( ttl ) );
        }
        return ExpiryPolicyBuilder.expiry( ).create( Duration.ofSeconds( ttl ) ).access( Duration.ofSeconds( tti ) ).update( Duration.ofSeconds( tti ) ).build( );
    }

    private Duration getDurationFromSeconds( int seconds )
    {
        return seconds <= 0 ? ChronoUnit.FOREVER.getDuration( ) : Duration.ofSeconds( seconds );
    }

    private synchronized PersistentCacheManager initCacheManager( StatisticsRetrieval svc, Path diskStorePath )
    {
        log.info( "Initializing Cache Manager {}, {}", isStatisticsEnabled( ), diskStorePath );
        if ( !Files.exists( diskStorePath ) )
        {
            try
            {
                Files.createDirectories( diskStorePath );
            }
            catch ( IOException e )
            {
                log.error( "Could not create cache path: {}", e.getMessage( ) );
            }
        }

        try
        {
            CacheManagerBuilder<PersistentCacheManager> builder = CacheManagerBuilder.newCacheManagerBuilder( )
                .with( CacheManagerBuilder.persistence( diskStorePath.toFile( ) ) );
            if ( isStatisticsEnabled( ) )
            {
                builder = builder.using( svc );
            }
            return builder.build( true );
        } catch ( StateTransitionException ex ) {
            // One try to use fallback path, if the cache exists already
            Path fallBackPath = diskStorePath.getParent( ).resolve( diskStorePath.getFileName( ).toString( ) + "-" + ( new Random( ).nextLong( ) % 1000 ) );
            try
            {
                Files.createDirectories( fallBackPath );
            }
            catch ( IOException e )
            {
                log.error( "Could not create fallback cache path: {} ", e.getMessage( ) );
            }
            CacheManagerBuilder<PersistentCacheManager> builder = CacheManagerBuilder.newCacheManagerBuilder( )
                .with( CacheManagerBuilder.persistence( fallBackPath.toFile( ) ) );
            if ( isStatisticsEnabled( ) )
            {
                builder = builder.using( svc );
            }
            return builder.build( true );
        }
    }

    @PreDestroy
    public void dispose( )
    {
        ManagerData data = cacheManagerRefs.get( registeredPath );
        PersistentCacheManager cacheManager = data.cacheManager;
        HashSet names = data.cacheNames;
        if ( cacheManager != null && cacheManager.getStatus( ).equals( Status.AVAILABLE ) )
        {
            log.info( "Disposing cache: {}, {}", ehcache, registeredName );
            if ( this.ehcache != null )
            {
                try
                {
                    cacheManager.destroyCache( this.registeredName );

                }
                catch ( Throwable e )
                {
                    log.error( "Cache removal failed: {}", e.getMessage( ), e );
                }
                finally
                {
                    names.remove( this.registeredName );
                    this.ehcache = null;
                }
            }
            if ( names.size( ) == 0 )
            {
                try
                {
                    cacheManager.close( );
                    cacheManager.destroy( );
                }
                catch ( Throwable e )
                {
                    log.error( "Cache manager removal failed: {}", e.getMessage( ), e );
                }
                finally
                {
                    cacheManagerRefs.remove( registeredPath );
                }
            }
        }
        else
        {
            log.debug( "Not disposing cache, because cacheManager is not alive: {}", ehcache );
        }
    }

    @Override
    public T get( V key )
    {
        if ( key == null )
        {
            return null;
        }

        return ehcache.get( key );
    }

    public long getDiskExpiryThreadIntervalSeconds( )
    {
        return diskExpiryThreadIntervalSeconds;
    }

    public Path getDiskStorePath( )
    {
        return diskStorePath;
    }

    public void setDiskStorePath( Path path )
    {
        this.diskStorePath = path.toAbsolutePath( );
    }

    @Override
    public int getMaxElementsInMemory( )
    {
        return maxElementsInMemory;
    }

    public String getMemoryEvictionPolicy( )
    {
        return memoryEvictionPolicy;
    }


    public String getName( )
    {
        return name;
    }

    @Override
    public CacheStatistics getStatistics( )
    {
        return stats;
    }

    @Override
    public int getTimeToIdleSeconds( )
    {
        return timeToIdleSeconds;
    }

    @Override
    public int getTimeToLiveSeconds( )
    {
        return timeToLiveSeconds;
    }

    @Override
    public boolean hasKey( V key )
    {
        return ehcache.containsKey( key );
    }

    public boolean isDiskPersistent( )
    {
        return diskPersistent;
    }

    public boolean isEternal( )
    {
        return eternal;
    }

    /**
     * @return true, or false
     * @deprecated This flag is ignored. The persistence strategy is always overflow to disk, if on.
     */
    public boolean isOverflowToDisk( )
    {
        return overflowToDisk;
    }

    @Override
    public void register( V key, T value )
    {
        ehcache.put( key, value );
    }

    @Override
    public T put( V key, T value )
    {
        // Multiple steps done to satisfy Cache API requirement for Previous object return.
        T previous;
        previous = ehcache.get( key );
        ehcache.put( key, value );
        return previous;
    }

    @Override
    public T remove( V key )
    {
        T previous = ehcache.get( key );
        ehcache.remove( key );
        return previous;
    }

    public void setDiskExpiryThreadIntervalSeconds( long diskExpiryThreadIntervalSeconds )
    {
        this.diskExpiryThreadIntervalSeconds = diskExpiryThreadIntervalSeconds;
    }

    public void setDiskPersistent( boolean diskPersistent )
    {
        this.diskPersistent = diskPersistent;
    }

    public void setEternal( boolean eternal )
    {
        this.eternal = eternal;
    }


    @Override
    public void setMaxElementsInMemory( int maxElementsInMemory )
    {
        this.maxElementsInMemory = maxElementsInMemory;
    }

    public void setMemoryEvictionPolicy( String memoryEvictionPolicy )
    {
        this.memoryEvictionPolicy = memoryEvictionPolicy;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    /**
     * @param overflowToDisk true, or false
     * @deprecated This flag is ignored. The persistence strategy is always overflow to disk, if on.
     */
    public void setOverflowToDisk( boolean overflowToDisk )
    {
        this.overflowToDisk = overflowToDisk;
    }

    @Override
    public void setTimeToIdleSeconds( int timeToIdleSeconds )
    {
        this.timeToIdleSeconds = timeToIdleSeconds;
    }

    @Override
    public void setTimeToLiveSeconds( int timeToLiveSeconds )
    {
        this.timeToLiveSeconds = timeToLiveSeconds;
    }

    public boolean isStatisticsEnabled( )
    {
        return statisticsEnabled;
    }

    public void setStatisticsEnabled( boolean statisticsEnabled )
    {
        // ignored for ehache
    }

    public boolean isFailOnDuplicateCache( )
    {
        return failOnDuplicateCache;
    }

    public void setFailOnDuplicateCache( boolean failOnDuplicateCache )
    {
        this.failOnDuplicateCache = failOnDuplicateCache;
    }

    public boolean isOverflowToOffHeap( )
    {
        return overflowToOffHeap;
    }

    public void setOverflowToOffHeap( boolean overflowToOffHeap )
    {
        this.overflowToOffHeap = overflowToOffHeap;
    }

    public long getMaxBytesLocalHeap( )
    {
        return maxBytesLocalHeap;
    }

    public void setMaxBytesLocalHeap( long maxBytesLocalHeap )
    {
        this.maxBytesLocalHeap = maxBytesLocalHeap;
    }

    public long getMaxBytesLocalOffHeap( )
    {
        return maxBytesLocalOffHeap;
    }

    public void setMaxBytesLocalOffHeap( long maxBytesLocalOffHeap )
    {
        this.maxBytesLocalOffHeap = maxBytesLocalOffHeap;
    }

    @Override
    public int getMaxElementsOnDisk( )
    {
        return maxElementsOnDisk;
    }

    @Override
    public void setMaxElementsOnDisk( int maxElementsOnDisk )
    {
        this.maxElementsOnDisk = maxElementsOnDisk;
    }

    /**
     * Sets the path to the configuration file. If this value is set to a valid file path,
     * the configuration will be loaded from the given file. The cache defined in this file must
     * match the cache name of this instance.
     *
     * @param configurationFile a valid path to a ehcache xml configuration file
     */
    public void setConfigurationFile( Path configurationFile )
    {
        this.configurationFile = configurationFile;
    }

    /**
     * Returns the path to the configuration file or <code>null</code>, if not set.
     *
     * @return the path of the configuration file or <code>null</code>
     */
    public Path getConfigurationFile( )
    {
        return configurationFile;
    }
}
