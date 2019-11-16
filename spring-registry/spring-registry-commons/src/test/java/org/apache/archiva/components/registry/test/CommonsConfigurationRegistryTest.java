package org.apache.archiva.components.registry.test;

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

import org.apache.archiva.components.registry.Registry;
import org.apache.archiva.components.registry.RegistryException;
import org.apache.archiva.components.registry.RegistryListener;
import org.apache.archiva.components.registry.commons.CommonsConfigurationRegistry;
import org.apache.commons.configuration.XMLConfiguration;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;

/**
 * Test the commons configuration registry.
 */
public class CommonsConfigurationRegistryTest
    extends AbstractRegistryTest
{
    private Registry registry;

    private final Logger logger = LoggerFactory.getLogger( getClass() );


    private static final int INT_TEST_VALUE = 8080;

    public String getRoleHint()
    {
        return "builder";
    }

    @Test
    public void testDefaultConfiguration()
        throws Exception
    {
        registry = getRegistry( "default" );

        assertEquals( "Check system property override", System.getProperty( "user.dir" ),
                      registry.getString( "user.dir" ) );
        assertEquals( "Check system property", System.getProperty( "user.home" ), registry.getString( "user.home" ) );
        assertNull( "Check other properties are not loaded", registry.getString( "test.value" ) );
    }

    @Test
    public void testBuilderConfiguration()
        throws Exception
    {
        registry = getRegistry( "builder" );

        assertEquals( "Check system property override", "new user dir", registry.getString( "user.dir" ) );
        assertEquals( "Check system property default", System.getProperty( "user.home" ),
                      registry.getString( "user.home" ) );
        assertEquals( "Check other properties are loaded", "foo", registry.getString( "test.value" ) );
        assertEquals( "Check other properties are loaded", 1, registry.getInt( "test.number" ) );
        assertTrue( "Check other properties are loaded", registry.getBoolean( "test.boolean" ) );
    }

    @Test
    public void testDump()
        throws Exception
    {
        registry = getRegistry( "default" );

        String dump = registry.dump();
        assertTrue( dump.startsWith( "Configuration Dump.\n\"" ) );
    }

    @Test
    public void testDefaults()
        throws Exception
    {
        registry = getRegistry( "builder" );

        assertNull( "Check getString returns null", registry.getString( "foo" ) );
        assertEquals( "Check getString returns default", "bar", registry.getString( "foo", "bar" ) );

        try
        {
            registry.getInt( "foo" );
            fail();
        }
        catch ( NoSuchElementException e )
        {
            // success
        }

        assertEquals( "Check getInt returns default", INT_TEST_VALUE, registry.getInt( "foo", INT_TEST_VALUE ) );

        try
        {
            registry.getBoolean( "foo" );
            fail();
        }
        catch ( NoSuchElementException e )
        {
            // success
        }

        assertTrue( "Check getBoolean returns default", registry.getBoolean( "foo", true ) );
    }

    @Test
    public void testInterpolation()
        throws Exception
    {
        registry = getRegistry( "builder" );

        assertEquals( "Check system property interpolation", System.getProperty( "user.home" ) + "/.m2/repository",
                      registry.getString( "repository" ) );

        assertEquals( "Check configuration value interpolation", "foo/bar",
                      registry.getString( "test.interpolation" ) );
    }

    @Test
    public void testAddConfigurationXmlFile()
        throws Exception
    {
        registry = getRegistry( "default" );

        registry.addConfigurationFromFile( new File( "./src/test/resources/org/codehaus/plexus/registry/test.xml" ) );

        assertEquals( "Check system property default", System.getProperty( "user.dir" ),
                      registry.getString( "user.dir" ) );
        assertEquals( "Check other properties are loaded", "foo", registry.getString( "test.value" ) );
    }

    @Test
    public void testAddConfigurationPropertiesFile()
        throws Exception
    {
        registry = getRegistry( "default" );

        registry.addConfigurationFromFile(
            new File( "./src/test/resources/org/codehaus/plexus/registry/test.properties" ) );

        assertEquals( "Check system property default", System.getProperty( "user.dir" ),
                      registry.getString( "user.dir" ) );
        assertEquals( "Check other properties are loaded", "baz", registry.getString( "foo.bar" ) );
        assertNull( "Check other properties are not loaded", registry.getString( "test.value" ) );
    }

    @Test
    public void testAddConfigurationXmlResource()
        throws Exception
    {
        registry = getRegistry( "default" );

        registry.addConfigurationFromResource( "org/codehaus/plexus/registry/test.xml" );

        assertEquals( "Check system property default", System.getProperty( "user.dir" ),
                      registry.getString( "user.dir" ) );
        assertEquals( "Check other properties are loaded", "foo", registry.getString( "test.value" ) );
    }

    @Test
    public void testAddConfigurationPropertiesResource()
        throws Exception
    {
        registry = getRegistry( "default" );

        registry.addConfigurationFromResource( "org/codehaus/plexus/registry/test.properties" );

        assertEquals( "Check system property default", System.getProperty( "user.dir" ),
                      registry.getString( "user.dir" ) );
        assertEquals( "Check other properties are loaded", "baz", registry.getString( "foo.bar" ) );
        assertNull( "Check other properties are not loaded", registry.getString( "test.value" ) );
    }

    @Test
    public void testAddConfigurationUnrecognisedType()
        throws Exception
    {
        registry = getRegistry( "default" );

        try
        {
            registry.addConfigurationFromResource( "org/codehaus/plexus/registry/test.foo" );
            fail();
        }
        catch ( RegistryException e )
        {
            // success
        }

        try
        {
            registry.addConfigurationFromFile(
                new File( "./src/test/resources/org/codehaus/plexus/registry/test.foo" ) );
            fail();
        }
        catch ( RegistryException e )
        {
            // success
        }
    }

    @Test
    public void testIsEmpty()
        throws Exception
    {
        registry = getRegistry( "default" );

        assertFalse( registry.isEmpty() );
        assertTrue( registry.getSubset( "foo" ).isEmpty() );
    }

    @Test
    public void testGetSubset()
        throws Exception
    {
        registry = getRegistry( "builder" );

        Registry registry = this.registry.getSubset( "test" );
        assertEquals( "Check other properties are loaded", "foo", registry.getString( "value" ) );
        assertEquals( "Check other properties are loaded", 1, registry.getInt( "number" ) );
        assertTrue( "Check other properties are loaded", registry.getBoolean( "boolean" ) );
    }

    @Test
    public void testGetSubsetList()
        throws Exception
    {
        registry = getRegistry( "builder" );

        List list = registry.getSubsetList( "objects.object" );
        assertEquals( 2, list.size() );
        Registry r = (Registry) list.get( 0 );
        assertTrue( "bar".equals( r.getString( "foo" ) ) || "baz".equals( r.getString( "foo" ) ) );
        r = (Registry) list.get( 1 );
        assertTrue( "bar".equals( r.getString( "foo" ) ) || "baz".equals( r.getString( "foo" ) ) );
    }

    @Test
    public void testGetProperties()
        throws Exception
    {
        registry = getRegistry( "builder" );

        Properties properties = registry.getProperties( "properties" );
        assertEquals( 2, properties.size() );
        assertEquals( "bar", properties.getProperty( "foo" ) );
        assertEquals( "baz", properties.getProperty( "bar" ) );
    }

    @Test
    public void testGetList()
        throws Exception
    {
        registry = getRegistry( "builder" );

        List list = registry.getList( "strings.string" );
        assertEquals( 3, list.size() );
        assertEquals( "s1", list.get( 0 ) );
        assertEquals( "s2", list.get( 1 ) );
        assertEquals( "s3", list.get( 2 ) );
    }

    @Test
    public void testGetSection()
        throws Exception
    {
        registry = getRegistry( "builder" );

        Registry registry = this.registry.getSection( "properties" );
        assertNull( registry.getString( "test.value" ) );
        assertEquals( "baz", registry.getString( "foo.bar" ) );
    }

    @Test
    public void testRemoveKey()
        throws Exception
    {
        registry = getRegistry( "builder" );

        Registry registry = this.registry.getSection( "properties" );
        assertEquals( "baz", registry.getString( "foo.bar" ) );
        registry.remove( "foo.bar" );
        assertNull( registry.getString( "foo.bar" ) );
    }

    @Test
    public void testRemoveSubset()
        throws Exception
    {
        registry = getRegistry( "builder" );

        registry.removeSubset( "strings" );
        assertEquals( Collections.EMPTY_LIST, registry.getList( "strings.string" ) );

        Registry registry = this.registry.getSection( "properties" );
        assertEquals( "baz", registry.getString( "foo.bar" ) );
        registry.remove( "foo" );
        assertEquals( "baz", registry.getString( "foo.bar" ) );
        registry.removeSubset( "foo" );
        assertNull( registry.getString( "foo.bar" ) );
    }

/* TODO: for 1.4
    public void testGetForcedCreateByName()
        throws Exception
    {
        registry = (Registry) lookup( Registry.class.getName(), "forceCreate" );

        String testFile = getTestFile( "target/foo-forced" ).getAbsolutePath();
        assertFalse( FileUtils.fileExists( testFile ) );

        assertNotNull( registry.getSection( "foo" ) );
    }
*/

    @Test
    public void testGetDontForceCreateByName()
        throws Exception
    {
        registry = getRegistry( "noForceCreate" );

        assertNull( registry.getSection( "foo" ) );
    }

    @Test
    public void testSaveSection()
        throws Exception
    {
        File src = new File( "./src/test/resources/test-save.xml" );
        File dest = new File( "./target/test-classes/test-save.xml" );
        FileCopyUtils.copy( src, dest );

        registry = getRegistry( "test-save" );

        Registry registry = this.registry.getSection( "org.codehaus.plexus.registry" );
        assertEquals( "check list elements", Arrays.asList( new String[]{ "1", "2", "3" } ),
                      registry.getList( "listElements.listElement" ) );

        registry.remove( "listElements.listElement(1)" );
        registry.save();

        XMLConfiguration configuration = new XMLConfiguration( dest );
        assertEquals( Arrays.asList( new String[]{ "1", "3" } ), configuration.getList( "listElements.listElement" ) );

        // file in ${basedir}/target/conf/shared.xml
        Registry section = this.registry.getSection( "org.apache.maven.shared.app.user" );
        section.setString( "foo", "zloug" );
        section.save();

        configuration = new XMLConfiguration( new File( "target/conf/shared.xml" ) );
        assertNotNull( configuration.getString( "foo" ) );

    }


    @Test
    public void test_listener()
        throws Exception
    {
        registry = getRegistry( "default" );

        int listenerSize = CommonsConfigurationRegistry.class.cast( registry ).getChangeListenersSize();

        MockChangeListener mockChangeListener = new MockChangeListener();

        registry.addChangeListener( mockChangeListener );

        registry.addChangeListener( new MockChangeListener() );

        assertEquals( listenerSize + 2, CommonsConfigurationRegistry.class.cast( registry ).getChangeListenersSize() );

        registry.removeChangeListener( mockChangeListener );

        assertEquals( listenerSize + 1, CommonsConfigurationRegistry.class.cast( registry ).getChangeListenersSize() );
    }

    private static class MockChangeListener
        implements RegistryListener
    {
        @Override
        public void beforeConfigurationChange( Registry registry, String propertyName, Object propertyValue )
        {
            // no op
        }

        @Override
        public void afterConfigurationChange( Registry registry, String propertyName, Object propertyValue )
        {
            // no op
        }
    }
}
