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

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;
import net.sf.ehcache.config.DiskStoreConfiguration;
import net.sf.ehcache.config.MemoryUnit;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.statistics.StatisticsGateway;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import org.apache.archiva.components.cache.CacheStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.nio.file.Files;
import java.nio.file.Path;

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

    private Logger log = LoggerFactory.getLogger( EhcacheCache.class );

    class Stats
        implements CacheStatistics
    {
        private boolean useBaseLine = false;
        private long hitCountBL = 0;
        private long missCountBL = 0;
        private long sizeBL = 0;
        private long localHeapSizeInBytesBL = 0;

        // No API for cache clear since 2.10. We use a baseline, if the cache is cleared.
        @Override
        public void clear( )
        {
            useBaseLine = true;
            final StatisticsGateway cStats = ehcache.getStatistics( );
            hitCountBL = cStats.cacheHitCount( );
            missCountBL = cStats.cacheMissCount( );
            sizeBL = cStats.getSize( );
            localHeapSizeInBytesBL = cStats.getLocalHeapSizeInBytes( );
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

        @Override
        public long getCacheHits( )
        {
            return useBaseLine ? ehcache.getStatistics( ).cacheHitCount( ) - hitCountBL : ehcache.getStatistics( ).cacheHitCount( );
        }

        @Override
        public long getCacheMiss( )
        {
            return useBaseLine ? ehcache.getStatistics( ).cacheMissCount( ) - missCountBL : ehcache.getStatistics( ).cacheMissCount( );
        }

        @Override
        public long getSize( )
        {
            return useBaseLine ? ehcache.getStatistics( ).getSize( ) - sizeBL : ehcache.getStatistics( ).getSize( );
        }

        @Override
        public long getInMemorySize( )
        {
            return useBaseLine ? ehcache.getStatistics( ).getLocalHeapSizeInBytes( ) - localHeapSizeInBytesBL : ehcache.getStatistics( ).getLocalHeapSizeInBytes( );
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
    private String diskStorePath = System.getProperties( ).containsKey( EHCACHE_DISK_STORE_PROPERTY ) ?
        System.getProperty( EHCACHE_DISK_STORE_PROPERTY ) :
        System.getProperty( "java.io.tmpdir" ) + "/ehcache-archiva";

    private boolean eternal = false;

    private int maxElementsInMemory = 0;

    private String memoryEvictionPolicy = "LRU";

    private String name = "cache";

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

    private CacheManager cacheManager = null;//CacheManager.getInstance();

    private net.sf.ehcache.Cache ehcache;

    private Stats stats;

    @Override
    public void clear( )
    {
        ehcache.removeAll( );
        stats.clear( );
    }

    @PostConstruct
    public void initialize( )
    {
        // We are skipping the update check if not set explicitly
        if ( !System.getProperties( ).containsKey( "net.sf.ehcache.skipUpdateCheck" ) )
        {
            System.setProperty( "net.sf.ehcache.skipUpdateCheck", "true" );
        }

        stats = new Stats( );

        boolean cacheManagerExists = CacheManager.getCacheManager( getName( ) ) != null;

        if ( cacheManagerExists )
        {
            if ( failOnDuplicateCache )
            {
                throw new RuntimeException( "A previous cacheManager with name [" + getName( ) + "] exists." );
            }
            else
            {
                log.warn( "skip duplicate cache {}", getName( ) );
                cacheManager = CacheManager.getCacheManager( getName( ) );
            }
        }
        else
        {
            Configuration configuration;
            if ( configurationFile != null && Files.exists( configurationFile ) && Files.isReadable( configurationFile ) )
            {
                configuration = ConfigurationFactory.parseConfiguration( configurationFile.toFile( ) );
            }
            else
            {
                configuration = new Configuration( );
            }
            this.cacheManager = new CacheManager( configuration.name( getName( ) ).diskStore(
                new DiskStoreConfiguration( ).path( getDiskStorePath( ) ) ) );
        }

        boolean cacheExists = cacheManager.cacheExists( getName( ) );

        if ( cacheExists )
        {
            if ( failOnDuplicateCache )
            {
                throw new RuntimeException( "A previous cache with name [" + getName( ) + "] exists." );
            }
            else
            {
                log.warn( "skip duplicate cache {}", getName( ) );
                ehcache = cacheManager.getCache( getName( ) );
            }
        }
        else
        {
            CacheConfiguration cacheConfiguration =
                new CacheConfiguration( ).name( getName( ) ).memoryStoreEvictionPolicy(
                    getMemoryStoreEvictionPolicy( ) ).eternal( isEternal( ) ).timeToLiveSeconds(
                    getTimeToLiveSeconds( ) ).timeToIdleSeconds(
                    getTimeToIdleSeconds( ) ).diskExpiryThreadIntervalSeconds(
                    getDiskExpiryThreadIntervalSeconds( ) ).overflowToOffHeap(
                    isOverflowToOffHeap( ) ).maxEntriesLocalDisk( getMaxElementsOnDisk( ) );

            cacheConfiguration.addPersistence( new PersistenceConfiguration( ) );
            if ( isDiskPersistent( ) )
            {
                cacheConfiguration.getPersistenceConfiguration( ).setStrategy( PersistenceConfiguration.Strategy.LOCALTEMPSWAP.name( ) );
            }
            else
            {
                cacheConfiguration.getPersistenceConfiguration( ).setStrategy( PersistenceConfiguration.Strategy.NONE.name( ) );
            }

            if ( getMaxElementsInMemory( ) > 0 )
            {
                cacheConfiguration = cacheConfiguration.maxEntriesLocalHeap( getMaxElementsInMemory( ) );
            }

            if ( getMaxBytesLocalHeap( ) > 0 )
            {
                cacheConfiguration = cacheConfiguration.maxBytesLocalHeap( getMaxBytesLocalHeap( ), MemoryUnit.BYTES );
            }
            if ( getMaxBytesLocalOffHeap( ) > 0 )
            {
                cacheConfiguration =
                    cacheConfiguration.maxBytesLocalOffHeap( getMaxBytesLocalOffHeap( ), MemoryUnit.BYTES );
            }

            ehcache = new Cache( cacheConfiguration );
            cacheManager.addCache( ehcache );
        }
    }

    @PreDestroy
    public void dispose( )
    {
        if ( this.cacheManager.getStatus( ).equals( Status.STATUS_ALIVE ) )
        {
            log.info( "Disposing cache: {}", ehcache );
            if ( this.ehcache != null )
            {
                this.cacheManager.removeCache( this.ehcache.getName( ) );
                this.ehcache = null;
            }
        }
        else
        {
            log.debug( "Not disposing cache, because cacheManager is not alive: {}", ehcache );
        }
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public T get( V key )
    {
        if ( key == null )
        {
            return null;
        }

        Element elem = ehcache.get( key );
        if ( elem == null )
        {
            return null;
        }
        return (T) elem.getObjectValue( );
    }

    public long getDiskExpiryThreadIntervalSeconds( )
    {
        return diskExpiryThreadIntervalSeconds;
    }

    public String getDiskStorePath( )
    {
        return diskStorePath;
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

    public MemoryStoreEvictionPolicy getMemoryStoreEvictionPolicy( )
    {
        return MemoryStoreEvictionPolicy.fromString( memoryEvictionPolicy );
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
        return ehcache.isKeyInCache( key );
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
        ehcache.put( new Element( key, value ) );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public T put( V key, T value )
    {
        // Multiple steps done to satisfy Cache API requirement for Previous object return.
        Element elem;
        Object previous = null;
        elem = ehcache.get( key );
        if ( elem != null )
        {
            previous = elem.getObjectValue( );
        }
        elem = new Element( key, value );
        ehcache.put( elem );
        return (T) previous;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public T remove( V key )
    {
        Element elem;
        Object previous = null;
        elem = ehcache.get( key );
        if ( elem != null )
        {
            previous = elem.getObjectValue( );
            ehcache.remove( key );
        }

        return (T) previous;
    }

    public void setDiskExpiryThreadIntervalSeconds( long diskExpiryThreadIntervalSeconds )
    {
        this.diskExpiryThreadIntervalSeconds = diskExpiryThreadIntervalSeconds;
    }

    public void setDiskPersistent( boolean diskPersistent )
    {
        this.diskPersistent = diskPersistent;
    }

    public void setDiskStorePath( String diskStorePath )
    {
        this.diskStorePath = diskStorePath;
    }

    public void setEternal( boolean eternal )
    {
        this.eternal = eternal;
    }


    @Override
    public void setMaxElementsInMemory( int maxElementsInMemory )
    {
        this.maxElementsInMemory = maxElementsInMemory;
        if ( this.ehcache != null )
        {
            this.ehcache.getCacheConfiguration( ).setMaxEntriesLocalHeap( this.maxElementsInMemory );

        }
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
        if ( this.ehcache != null )
        {
            this.ehcache.getCacheConfiguration( ).setTimeToIdleSeconds( timeToIdleSeconds );
        }
        this.timeToIdleSeconds = timeToIdleSeconds;
    }

    @Override
    public void setTimeToLiveSeconds( int timeToLiveSeconds )
    {
        if ( this.ehcache != null )
        {
            this.ehcache.getCacheConfiguration( ).setTimeToLiveSeconds( timeToIdleSeconds );
        }
        this.timeToLiveSeconds = timeToLiveSeconds;
    }

    public boolean isStatisticsEnabled( )
    {
        return statisticsEnabled;
    }

    public void setStatisticsEnabled( boolean statisticsEnabled )
    {
        this.statisticsEnabled = statisticsEnabled;
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
        if ( this.ehcache != null )
        {
            this.ehcache.getCacheConfiguration( ).setMaxEntriesInCache( this.maxElementsOnDisk );
            this.ehcache.getCacheConfiguration( ).maxEntriesLocalDisk( this.maxElementsOnDisk );
        }
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
