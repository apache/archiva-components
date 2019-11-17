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

import com.opensymphony.oscache.base.CacheEntry;
import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;
import org.apache.archiva.components.cache.AbstractCache;
import org.apache.archiva.components.cache.Cache;
import org.apache.archiva.components.cache.CacheStatistics;

import javax.annotation.PostConstruct;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * For configuration see documentation : http://opensymphony.com/oscache/wiki/Configuration.html
 *
 * @author @author Olivier Lamy
 */
public class OsCacheCache<V, T>
    extends AbstractCache<V, T>
    implements Cache<V, T>
{

    private GeneralCacheAdministrator generalCacheAdministrator;

    private OsCacheStatistics osCacheStatistics;

    //---------------------------------------------
    // Configuration attributes
    //--------------------------------------------- 

    /**
     * use memory cache
     */
    private boolean cacheMemory = true;

    /**
     * maximum item numbers default value -1 means unlimited
     */
    private int capacity = -1;

    /**
     * cache algorithm
     */
    private String cacheAlgorithm;

    /**
     *
     */
    private boolean blockingCache = false;

    /**
     *
     */
    private boolean cacheUnlimitedDisk = false;

    /**
     *
     */
    private String cachePersistenceClass;

    /**
     *
     */
    private String cachePath;

    /**
     *
     */
    private boolean cachePersistenceOverflowOnly = false;

    /**
     * A default one will be added to provided CacheStatistics
     */
    private List cacheEventListeners;

    /**
     *
     */
    private String cacheKey;

    /**
     *
     */
    private boolean cacheUseHostDomainInKey = false;

    /**
     * in order to add some other osCache properties
     */
    private Map additionnalProperties;

    /**
     * default Value {@link CacheEntry#INDEFINITE_EXPIRY}
     */
    private int refreshPeriod = CacheEntry.INDEFINITE_EXPIRY;

    //---------------------------------------------
    // Component lifecycle
    //---------------------------------------------
    @PostConstruct
    public void initialize( )
    {
        Properties cacheProperties = new Properties( );
        if ( this.getAdditionnalProperties( ) != null )
        {
            cacheProperties.putAll( this.getAdditionnalProperties( ) );
        }
        if ( this.getCacheAlgorithm( ) != null )
        {
            cacheProperties.put( GeneralCacheAdministrator.CACHE_ALGORITHM_KEY, this.getCacheAlgorithm( ) );
        }
        cacheProperties.put( GeneralCacheAdministrator.CACHE_BLOCKING_KEY, Boolean.toString( this.isBlockingCache( ) ) );
        cacheProperties.put( GeneralCacheAdministrator.CACHE_CAPACITY_KEY, Integer.toString( this.getCapacity( ) ) );
        cacheProperties.put( GeneralCacheAdministrator.CACHE_DISK_UNLIMITED_KEY,
            Boolean.toString( this.isCacheUnlimitedDisk( ) ) );

        String cacheEventListenersAsString = this.getCacheEventListenersAsString( );
        if ( cacheEventListenersAsString != null )
        {
            cacheProperties.put( GeneralCacheAdministrator.CACHE_ENTRY_EVENT_LISTENERS_KEY,
                cacheEventListenersAsString );
        }
        cacheProperties.put( GeneralCacheAdministrator.CACHE_MEMORY_KEY, Boolean.toString( this.isCacheMemory( ) ) );
        cacheProperties.put( GeneralCacheAdministrator.CACHE_PERSISTENCE_OVERFLOW_KEY,
            Boolean.toString( this.isCachePersistenceOverflowOnly( ) ) );

        if ( this.getCachePersistenceClass( ) != null )
        {
            cacheProperties.put( GeneralCacheAdministrator.PERSISTENCE_CLASS_KEY, this.getCachePersistenceClass( ) );
        }

        cacheProperties.put( "cache.unlimited.disk", Boolean.toString( this.isCacheUnlimitedDisk( ) ) );
        if ( this.getCachePath( ) != null )
        {
            cacheProperties.put( "cache.path", this.getCachePath( ) );
        }
        if ( this.getCacheKey( ) != null )
        {
            cacheProperties.put( "cache.key", this.getCacheKey( ) );
        }
        cacheProperties.put( "cache.use.host.domain.in.key", Boolean.toString( this.isCacheUseHostDomainInKey( ) ) );
        this.generalCacheAdministrator = new GeneralCacheAdministrator( cacheProperties );
        this.osCacheStatistics = new OsCacheStatistics( this.generalCacheAdministrator.getCache( ) );
    }

    //---------------------------------------------
    // Interface implementation
    //---------------------------------------------

    public void clear( )
    {
        this.generalCacheAdministrator.flushAll( );
    }

    public T get( V key )
    {
        try
        {
            T object = null;
            if ( this.getRefreshPeriod( ) >= 0 )
            {
                object = (T) this.generalCacheAdministrator.getFromCache( key.toString( ), this.getRefreshPeriod( ) );
            }
            else
            {
                object = (T) this.generalCacheAdministrator.getFromCache( key.toString( ) );
            }
            if ( object != null )
            {
                this.osCacheStatistics.hit( );
            }
            else
            {
                this.osCacheStatistics.miss( );
            }
            return object;
        }
        catch ( NeedsRefreshException e )
        {
            this.generalCacheAdministrator.cancelUpdate( key.toString( ) );
            this.osCacheStatistics.miss( );
            return null;
        }
    }


    public CacheStatistics getStatistics( )
    {
        // osCacheStatistics to update ??
        return this.osCacheStatistics;
    }

    public boolean hasKey( V key )
    {
        // TODO if null increase/decrease statistics ?
        return this.get( key ) == null;
    }

    public void register( V key, T value )
    {
        this.generalCacheAdministrator.putInCache( key.toString( ), value );
    }

    public T put( V key, T value )
    {
        Object previous = null;
        try
        {
            previous = this.generalCacheAdministrator.getFromCache( key.toString( ), this.getRefreshPeriod( ) );
        }
        catch ( NeedsRefreshException e )
        {
            // ignore this because the content will be updated
        }
        this.generalCacheAdministrator.putInCache( key.toString( ), value );
        return (T) previous;
    }

    public T remove( V key )
    {
        Object previous = null;
        try
        {
            previous = this.generalCacheAdministrator.getFromCache( key.toString( ), this.getRefreshPeriod( ) );

        }
        catch ( NeedsRefreshException e )
        {
            // ignore this because the content will be updated
        }
        this.generalCacheAdministrator.cancelUpdate( key.toString( ) );
        this.generalCacheAdministrator.flushEntry( key.toString( ) );
        return (T) previous;
    }

    //---------------------------------------------
    // getters/setters
    //---------------------------------------------    

    public Map getAdditionnalProperties( )
    {
        return additionnalProperties;
    }

    public void setAdditionnalProperties( Map additionnalProperties )
    {
        this.additionnalProperties = additionnalProperties;
    }

    public boolean isBlockingCache( )
    {
        return blockingCache;
    }

    public void setBlockingCache( boolean blockingCache )
    {
        this.blockingCache = blockingCache;
    }

    public String getCacheAlgorithm( )
    {
        return cacheAlgorithm == null ? "com.opensymphony.oscache.base.algorithm.UnlimitedCache" : cacheAlgorithm;
    }

    public void setCacheAlgorithm( String cacheAlgorithm )
    {
        this.cacheAlgorithm = cacheAlgorithm;
    }

    public List getCacheEventListeners( )
    {
        return cacheEventListeners;
    }

    /**
     * @return list values in a String separated with comma
     */
    private String getCacheEventListenersAsString( )
    {
        if ( this.getCacheEventListeners( ) == null )
        {
            return null;
        }
        if ( this.getCacheEventListeners( ).isEmpty( ) )
        {
            return null;
        }
        StringBuilder stringBuffer = new StringBuilder( );
        for ( Iterator iterator = this.getCacheEventListeners( ).iterator( ); iterator.hasNext( ); )
        {
            stringBuffer.append( iterator.next( ) ).append( ',' );
        }
        return stringBuffer.toString( ).substring( 0, stringBuffer.toString( ).length( ) - 1 );
    }

    public void setCacheEventListeners( List cacheEventListeners )
    {
        this.cacheEventListeners = cacheEventListeners;
    }

    public String getCacheKey( )
    {
        return cacheKey == null ? "cacheKey" : cacheKey;
    }

    public void setCacheKey( String cacheKey )
    {
        this.cacheKey = cacheKey;
    }

    public boolean isCacheMemory( )
    {
        return cacheMemory;
    }

    public void setCacheMemory( boolean cacheMemory )
    {
        this.cacheMemory = cacheMemory;
    }

    public String getCachePath( )
    {
        return cachePath;
    }

    public void setCachePath( String cachePath )
    {
        this.cachePath = cachePath;
    }

    public String getCachePersistenceClass( )
    {
        return cachePersistenceClass;
    }

    public void setCachePersistenceClass( String cachePersistenceClass )
    {
        this.cachePersistenceClass = cachePersistenceClass;
    }

    public boolean isCachePersistenceOverflowOnly( )
    {
        return cachePersistenceOverflowOnly;
    }

    public void setCachePersistenceOverflowOnly( boolean cachePersistenceOverflowOnly )
    {
        this.cachePersistenceOverflowOnly = cachePersistenceOverflowOnly;
    }

    public boolean isCacheUnlimitedDisk( )
    {
        return cacheUnlimitedDisk;
    }

    public void setCacheUnlimitedDisk( boolean cacheUnlimitedDisk )
    {
        this.cacheUnlimitedDisk = cacheUnlimitedDisk;
    }

    public boolean isCacheUseHostDomainInKey( )
    {
        return cacheUseHostDomainInKey;
    }

    public void setCacheUseHostDomainInKey( boolean cacheUseHostDomainInKey )
    {
        this.cacheUseHostDomainInKey = cacheUseHostDomainInKey;
    }

    public int getCapacity( )
    {
        return capacity;
    }

    public void setCapacity( int capacity )
    {
        this.capacity = capacity;
    }

    public GeneralCacheAdministrator getGeneralCacheAdministrator( )
    {
        return generalCacheAdministrator;
    }

    public void setGeneralCacheAdministrator( GeneralCacheAdministrator generalCacheAdministrator )
    {
        this.generalCacheAdministrator = generalCacheAdministrator;
    }

    public int getRefreshPeriod( )
    {
        return refreshPeriod;
    }

    public void setRefreshPeriod( int refreshPeriod )
    {
        this.refreshPeriod = refreshPeriod;
    }

}
