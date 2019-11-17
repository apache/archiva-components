package org.apache.archiva.components.cache.test;

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

import junit.framework.TestCase;
import org.apache.archiva.components.cache.Cache;
import org.apache.archiva.components.cache.CacheException;
import org.apache.archiva.components.cache.CacheStatistics;
import org.apache.archiva.components.cache.factory.CacheFactory;
import org.apache.archiva.components.cache.test.examples.wine.Wine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AbstractCacheTestCase
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = {"classpath*:/META-INF/spring-context.xml", "classpath*:/spring-context.xml"} )
public abstract class AbstractCacheTestCase
    extends TestCase
{
    static
    {
        Logger logger = Logger.getLogger( "org.codehaus.plexus.cache" );
        logger.setLevel( Level.ALL );
        ConsoleHandler handler = new ConsoleHandler( );
        handler.setLevel( Level.ALL );
        logger.addHandler( handler );
    }

    protected Cache<String, Integer> cache;

    @Before
    public void setUp( )
        throws Exception
    {
        super.setUp( );
        cache = getCache( );
    }


    public abstract Cache<String, Integer> getCache( );

    @Test
    public void testSimplePutGet( )
    {
        Integer fooInt = Integer.valueOf( 42 );
        cache.put( "foo", fooInt );

        Integer val = cache.get( "foo" );
        assertEquals( 42, val.intValue( ) );

        assertNull( cache.get( "bar" ) );
    }

    @Test
    public void testLargePutGet( )
    {
        EnglishNumberFormat fmt = new EnglishNumberFormat( );

        for ( int i = 4500; i <= 5000; i++ )
        {
            String key = fmt.toText( i );
            cache.put( key, Integer.valueOf( i ) );
        }

        // Put some holes into the list.
        List<String> removedKeys = new ArrayList<>( );
        removedKeys.add( fmt.toText( 4600 ) );
        removedKeys.add( fmt.toText( 4700 ) );
        removedKeys.add( fmt.toText( 4800 ) );

        Iterator<String> it = removedKeys.iterator( );
        while ( it.hasNext( ) )
        {
            cache.remove( it.next( ) );
        }

        // Some direct gets
        assertEquals( Integer.valueOf( 4590 ), cache.get( "four thousand five hundred ninety" ) );
        assertEquals( Integer.valueOf( 4912 ), cache.get( "four thousand nine hundred twelve" ) );
        int DIRECT = 2;

        // Fetch the list repeatedly
        int ITERS = 100;
        int LOW = 4590;
        int HIGH = 4810;
        for ( int iter = 0; iter < ITERS; iter++ )
        {
            for ( int num = LOW; num < HIGH; num++ )
            {
                String key = fmt.toText( num );
                Integer expected = Integer.valueOf( num );
                Integer val = cache.get( key );

                // Intentionally removed entries?
                if ( removedKeys.contains( key ) )
                {
                    assertNull( "Removed key [" + key + "] should have no value.", val );
                }
                else
                {
                    assertEquals( expected, val );
                }
            }
        }

        // Test the statistics.
        CacheStatistics stats = cache.getStatistics( );

        int expectedHits = ( ( ( HIGH - LOW - removedKeys.size( ) ) * ITERS ) + DIRECT );
        int expectedMiss = ( ITERS * removedKeys.size( ) );

        /* Due to the nature of how the various providers do their work, the expected values
         * should be viewed as minimum values, not exact values.
         */
        assertTrue( "Cache hit count should exceed [" + expectedHits + "], but was actually [" + stats.getCacheHits( )
            + "]", expectedHits <= stats.getCacheHits( ) );

        assertTrue( "Cache miss count should exceed [" + expectedMiss + "], but was actually [" + stats.getCacheMiss( )
            + "]", expectedMiss <= stats.getCacheMiss( ) );

        /* For the same reason as above, the hit rate is completely un-testable.
         * Leaving this commented so that future developers understand the reason we are not
         * testing this value.
         
         double expectedHitRate = (double) expectedHits / (double) ( expectedHits + expectedMiss );
         assertTrue( "Cache hit rate should exceed [" + expectedHitRate + "], but was actually ["
         + stats.getCacheHitRate() + "]", expectedHitRate <= stats.getCacheHitRate() );
         
         */
    }

    public abstract Cache<String, Wine> getAlwaysRefresCache( )
        throws Exception;

    @Test
    public void testAlwaysRefresh( )
        throws Exception
    {
        Wine wine = new Wine( "bordeaux", "west/south of France" );
        String key = wine.getName( );
        Cache<String, Wine> cache = this.getAlwaysRefresCache( );
        cache.put( key, wine );
        Thread.sleep( 1001 );
        assertNull( cache.get( key ) );
    }

    public abstract Cache<String, Wine> getNeverRefresCache( )
        throws Exception;

    @Test
    public void testNeverRefresh( )
        throws Exception
    {
        Cache<String, Wine> cache = this.getNeverRefresCache( );
        Wine wine = new Wine( "bordeaux", "west/south of France" );
        String key = wine.getName( );
        cache.put( key, wine );
        //Thread.sleep( 1200 );
        Wine o = cache.get( key );
        assertNotNull( o );
        assertEquals( wine.hashCode( ), o.hashCode( ) );
    }

    public abstract Cache<String, Wine> getOneSecondRefresCache( )
        throws Exception;

    @Test
    public void testOneSecondRefresh( )
        throws Exception
    {
        Cache<String, Wine> cache = this.getOneSecondRefresCache( );
        Wine wine = new Wine( "bordeaux", "west/south of France" );
        String key = wine.getName( );
        cache.put( key, wine );
        Thread.sleep( 1200 );
        assertNull( cache.get( key ) );
    }

    public abstract Cache<String, Wine> getTwoSecondRefresCache( )
        throws Exception;

    @Test
    public void testTwoSecondRefresh( )
        throws Exception
    {
        Cache<String, Wine> cache = this.getTwoSecondRefresCache( );
        Wine wine = new Wine( "bordeaux", "west/south of France" );
        String key = wine.getName( );
        cache.put( key, wine );
        Thread.sleep( 500 );
        Wine o = cache.get( key );
        assertNotNull( o );
        assertEquals( wine.hashCode( ), o.hashCode( ) );
    }

    public abstract Class getCacheClass( );

    @Test
    public void testCacheFactory( ) throws CacheException
    {
        Cache<String, Integer> cache = CacheFactory.getInstance( ).getCache( "foo-factory-test", null );

        // This test is only here to ensure that the provider implements a Creator class.
        assertNotNull( "Cache should not be null", cache );
        assertEquals( "Cache class", getCacheClass( ).getName( ), cache.getClass( ).getName( ) );
        assertTrue( "Cache should be assignable from", getCacheClass( ).isAssignableFrom( cache.getClass( ) ) );

        // Now do some basic set/get functions to test if the cache has been initialized (or not).
        assertNull( cache.get( "bad wolf" ) );

        Integer fooInt = Integer.valueOf( 42 );
        cache.put( "foo", fooInt );

        Integer val = cache.get( "foo" );
        assertEquals( 42, val.intValue( ) );

        assertNull( cache.get( "bar" ) );
    }
}
