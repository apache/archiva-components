package org.apache.archiva.components.registry.commons;

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
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.SystemConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.ConfigurationBuilder;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.combined.CombinedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.event.Event;
import org.apache.commons.configuration2.event.EventSource;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.DefaultExpressionEngine;
import org.apache.commons.configuration2.tree.DefaultExpressionEngineSymbols;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookupFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Implementation of the registry component using
 * <a href="http://commons.apache.org/commons/configuration">Commons Configuration</a>. The use of Commons Configuration
 * enables a variety of sources to be used, including XML files, properties, JNDI, JDBC, etc.
 * <p/>
 * The component can be configured using the {@link #configurationDefinition} configuration item, the content of which should take
 * the format of an input to the Commons Configuration
 * <a href="http://commons.apache.org/commons/configuration/howto_configurationbuilder.html">configuration
 * builder</a>.
 *
 * If you initialize a <code>CombinedConfiguration</code>, which is the default, if you do not give a builder in the
 * constructor or use the {@link #setInitialConfiguration(String)} or {@link #setInitialConfigurationFile(Path)} methods,
 * then you should be careful with modifications and the {@link #save()} method.
 * You should always change your configuration with a given section, which represents a concrete configuration source
 * in the <code>CombinedConfiguration</code>. The section names correspond to the config-name attributes in the
 * configuration definition.
 *
 *
 */
@Service( "commons-configuration" )
public class CommonsConfigurationRegistry
    implements Registry
{
    /**
     * The combined configuration instance that houses the registry.
     */
    private Configuration configuration;
    private ConfigurationBuilder<? extends Configuration> configurationBuilder;

    private Logger logger = LoggerFactory.getLogger( getClass( ) );

    private String propertyDelimiter = ".";

    /**
     * The configuration properties for the registry. This should take the format of an input to the Commons
     * Configuration
     * <a href="http://commons.apache.org/configuration/howto_configurationbuilder.html">configuration
     * builder</a>.
     */
    private String configurationDefinition;
    private Path configurationDefinitionFile;

    private FileBasedConfiguration saveConfiguration = null;
    private FileBasedConfigurationBuilder saveConfigurationBuilder = null;
    private String saveConfigurationName = null;

    private boolean isPersistent = true;


    public CommonsConfigurationRegistry( )
    {
        // default constructor
        logger.debug( "empty constructor" );
        this.configurationBuilder = new CombinedConfigurationBuilder( );
        try
        {
            this.configuration = configurationBuilder.getConfiguration();
        }
        catch ( ConfigurationException e )
        {
            logger.debug( "Could not initialize configuration: {}", e.getMessage( ) );
        }
    }

    public CommonsConfigurationRegistry( ConfigurationBuilder<? extends Configuration> configurationBuilder )
    {
        if ( configurationBuilder == null )
        {
            throw new NullPointerException( "configurationbuilder can not be null" );
        }

        this.configurationBuilder = configurationBuilder;
        try
        {
            this.configuration = configurationBuilder.getConfiguration( );
        }
        catch ( ConfigurationException e )
        {
            logger.error( "Could not retrieve configuration" );
        }
    }

    public CommonsConfigurationRegistry( ConfigurationBuilder<? extends Configuration> configurationBuilder,
                                         Configuration configuration) {
        if ( configurationBuilder == null )
        {
            throw new NullPointerException( "configurationbuilder can not be null" );
        }
        if ( configuration == null )
        {
            throw new NullPointerException( "configuration can not be null" );
        }
        this.configurationBuilder = configurationBuilder;
        this.configuration = configuration;

    }

    @Override
    public String dump( )
    {
        StringBuilder buffer = new StringBuilder( );
        buffer.append( "Configuration Dump." );
        for ( Iterator i = configuration.getKeys( ); i.hasNext( ); )
        {
            String key = (String) i.next( );
            Object value = configuration.getProperty( key );
            buffer.append( "\n\"" ).append( key ).append( "\" = \"" ).append( value ).append( "\"" );
        }
        return buffer.toString( );
    }

    @Override
    public boolean isEmpty( )
    {
        return configuration.isEmpty( );
    }

    @Override
    public Registry getSubset( String key )
    {
        return new CommonsConfigurationRegistry( configurationBuilder, configuration.subset( key ) );
    }

    @Override
    public List getList( String key )
    {
        return configuration.getList( key );
    }

    @Override
    public List<Registry> getSubsetList( String key )
    {
        List<Registry> subsets = new ArrayList<>( );

        boolean done = false;
        do
        {
            Registry registry = getSubset( key + "(" + subsets.size( ) + ")" );
            if ( !registry.isEmpty( ) )
            {
                subsets.add( registry );
            }
            else
            {
                done = true;
            }
        }
        while ( !done );

        return subsets;
    }

    @Override
    public Properties getProperties( String key )
    {
        Configuration configuration = this.configuration.subset( key );

        Properties properties = new Properties( );
        for ( Iterator i = configuration.getKeys( ); i.hasNext( ); )
        {
            String property = (String) i.next( );
            List l = configuration.getList( property );
            StringBuilder sb = new StringBuilder( );
            for ( Object element : l )
            {
                sb.append( (String) element );
                sb.append( "," );
            }
            if ( sb.length( ) > 0 )
            {
                sb.deleteCharAt( sb.length( ) - 1 );
            }
            properties.setProperty( property, sb.toString( ) );
        }
        return properties;
    }

    @Override
    public void save( )
        throws RegistryException
    {

        if (isPersistent)
        {
            if ( configurationBuilder instanceof FileBasedConfigurationBuilder )
            {
                FileBasedConfigurationBuilder fileConfigurationBuilder = (FileBasedConfigurationBuilder) configurationBuilder;
                try
                {
                    fileConfigurationBuilder.save( );
                }
                catch ( ConfigurationException e )
                {
                    throw new RegistryException( "Could not save to file based configuration: "+e.getMessage( ), e );
                }
            }
            else if ( configurationBuilder instanceof CombinedConfigurationBuilder && saveConfigurationBuilder != null)
            {
                try
                {
                    saveConfigurationBuilder.save();
                }
                catch ( ConfigurationException e )
                {
                    throw new RegistryException( "Could not save to combined configuration: " + e.getMessage( ), e );
                }
            }
            else
            {
                throw new RegistryException( "Can only save file-based configurations" );
            }
        }
    }

    @Override
    public void addChangeListener( RegistryListener listener )
    {
        EventSource eventSource = (EventSource) this.configuration;

        eventSource.addEventListener( Event.ANY, new ConfigurationListenerDelegate( listener, this ) );
    }

    @Override
    public boolean removeChangeListener( RegistryListener listener )
    {
        EventSource eventSource = (EventSource) this.configuration;

        return eventSource.removeEventListener( Event.ANY, new ConfigurationListenerDelegate( listener, this ) );
    }



    @Override
    public Collection<String> getKeys( )
    {
        Set<String> keys = new HashSet<>( );

        for ( Iterator<String> i = configuration.getKeys( ); i.hasNext( ); )
        {
            String key = i.next( );

            int index = key.indexOf( '.' );
            if ( index < 0 )
            {
                keys.add( key );
            }
            else
            {
                keys.add( key.substring( 0, index ) );
            }
        }

        return keys;
    }

    @Override
    public Collection getFullKeys( )
    {
        Set<String> keys = new HashSet<String>( );

        for ( Iterator<String> i = configuration.getKeys( ); i.hasNext( ); )
        {
            keys.add( i.next( ) );
        }

        return keys;
    }

    /**
     * Removes a given configuration node from the configuration. For a combined configuration, this
     * may be only in memory and can not be persisted. The method runs the <code>clearProperty()</code>
     * on the configuration instance.
     *
     * @param key the key to remove
     */
    @Override
    public void remove( String key )
    {
        configuration.clearProperty( key );
    }

    @Override
    public void removeSubset( String key )
    {
        // create temporary list since removing a key will modify the iterator from configuration
        List keys = new ArrayList( );
        for ( Iterator i = configuration.getKeys( key ); i.hasNext( ); )
        {
            keys.add( i.next( ) );
        }

        for ( Iterator i = keys.iterator( ); i.hasNext( ); )
        {
            configuration.clearProperty( (String) i.next( ) );
        }
    }

    @Override
    public String getString( String key )
    {
        return configuration.getString( key );
    }

    @Override
    public String getString( String key, String defaultValue )
    {
        return configuration.getString( key, defaultValue );
    }

    @Override
    public void setString( String key, String value )
    {
        configuration.setProperty( key, value );
    }

    @Override
    public int getInt( String key )
    {
        return configuration.getInt( key );
    }

    @Override
    public int getInt( String key, int defaultValue )
    {
        return configuration.getInt( key, defaultValue );
    }

    @Override
    public void setInt( String key, int value )
    {
        configuration.setProperty( key, Integer.valueOf( value ) );
    }

    @Override
    public boolean getBoolean( String key )
    {
        return configuration.getBoolean( key );
    }

    @Override
    public boolean getBoolean( String key, boolean defaultValue )
    {
        return configuration.getBoolean( key, defaultValue );
    }

    @Override
    public void setBoolean( String key, boolean value )
    {
        configuration.setProperty( key, Boolean.valueOf( value ) );
    }

    /**
     *
     * Adds a new configuration source to the combined configuration.
     *
     * This is only possible with a combined configuration (which is the default).
     *
     * @param resource the location to load the configuration from
     * @throws RegistryException if the configuration is not a <code>CombinedConfiguration</code>
     */
    @Override
    public void addConfigurationFromResource( String resource )
        throws RegistryException
    {
        addConfigurationFromResource( resource, null );
    }

    /**
     *
     * Adds a new configuration source to the combined configuration.
     *
     * This is only possible with a combined configuration (which is the default).
     *
     * @param resource the location to load the configuration from
     * @param prefix the prefix where the root of the given configuration is placed
     * @throws RegistryException if the configuration is not a <code>CombinedConfiguration</code>
     */
    @Override
    public void addConfigurationFromResource( String resource, String prefix )
        throws RegistryException
    {
        if ( !( this.configuration instanceof CombinedConfiguration ) )
        {
            throw new RegistryException( "This is not a combined configuration so cannot add resource" );
        }
        CombinedConfiguration configuration = (CombinedConfiguration) this.configuration;
        if ( resource.endsWith( ".properties" ) )
        {
            try
            {
                logger.debug( "Loading properties configuration from classloader resource: {}", resource );
                Configurations configurations = new Configurations( );
                configuration.addConfiguration( configurations.properties( resource ), null, prefix );
            }
            catch ( ConfigurationException e )
            {
                throw new RegistryException(
                    "Unable to add configuration from resource '" + resource + "': " + e.getMessage( ), e );
            }
        }
        else if ( resource.endsWith( ".xml" ) )
        {
            try
            {
                logger.debug( "Loading XML configuration from classloader resource: {}", resource );
                Configurations configurations = new Configurations( );
                configuration.addConfiguration( configurations.xml( resource ), null, prefix );
            }
            catch ( ConfigurationException e )
            {
                throw new RegistryException(
                    "Unable to add configuration from resource '" + resource + "': " + e.getMessage( ), e );
            }
        }
        else
        {
            throw new RegistryException(
                "Unable to add configuration from resource '" + resource + "': unrecognised type" );
        }
    }

    /**
     *
     * Adds a new configuration source to the combined configuration.
     *
     * This is only possible with a combined configuration (which is the default).
     *
     * @param file  the path where the configuration can be loaded
     * @throws RegistryException if the configuration is not a <code>CombinedConfiguration</code>
     */
    @Override
    public void addConfigurationFromFile( Path file )
        throws RegistryException
    {
        addConfigurationFromFile( file, null );
    }

    /**
     *
     * Adds a new configuration source to the combined configuration.
     *
     * This is only possible with a combined configuration (which is the default).
     *
     * @param file the path, where the configuration can be loaded
     * @param prefix the prefix where the root of the given configuration is placed
     * @throws RegistryException if the configuration is not a <code>CombinedConfiguration</code>
     */
    @Override
    public void addConfigurationFromFile( Path file, String name, String prefix )
        throws RegistryException
    {
        CombinedConfiguration configuration = (CombinedConfiguration) this.configuration;
        if ( file.getFileName( ).toString().endsWith( ".properties" ) )
        {
            try
            {
                logger.debug( "Loading properties configuration from file: {}", file );
                if (configuration.getConfigurationNames( ).contains( name )) {
                    configuration.removeConfiguration( prefix );
                }
                Configurations configurations = new Configurations( );
                configuration.addConfiguration( configurations.properties( file.toFile() ), name, prefix );
            }
            catch ( ConfigurationException e )
            {
                throw new RegistryException(
                    "Unable to add configuration from file '" + file.getFileName( ).toString( ) + "': " + e.getMessage( ), e );
            }
        }
        else if ( file.getFileName( ).toString( ).endsWith( ".xml" ) )
        {
            try
            {
                logger.debug( "Loading XML configuration from file: {}", file );
                if (configuration.getConfigurationNames( ).contains( name )) {
                    configuration.removeConfiguration( prefix );
                }
                Configurations configurations = new Configurations( );
                configuration.addConfiguration( configurations.xml( file.toFile() ), name, prefix );
            }
            catch ( ConfigurationException e )
            {
                throw new RegistryException(
                    "Unable to add configuration from file '" + file.getFileName( ).toString( ) + "': " + e.getMessage( ), e );
            }
        }
        else
        {
            throw new RegistryException(
                "Unable to add configuration from file '" + file.getFileName( ).toString( ) + "': unrecognised type" );
        }
    }

    @Override
    public void addConfigurationFromFile( Path file, String prefix ) throws RegistryException
    {
        addConfigurationFromFile( file, prefix, prefix );
    }


    /**
     * This method tries to read a combined configuration definition either from the string given
     * by {@link #setInitialConfiguration(String)} or from the file given by {@link #setInitialConfigurationFile(Path)}
     *
     * The initialization assumes that override combiner is used and tries to find the first writable configuration
     * source as save target.
     *
     * @throws RegistryException if the configuration initialization failed
     */
    @Override
    @PostConstruct
    public void initialize( )
        throws RegistryException
    {


        synchronized (this)
        {
            try
            {
                CombinedConfiguration configuration;
                CombinedConfigurationBuilder builder;

                if ( configurationDefinitionFile != null && Files.exists( configurationDefinitionFile ) )
                {
                    DefaultExpressionEngine expressionEngine = new DefaultExpressionEngine( DefaultExpressionEngineSymbols.DEFAULT_SYMBOLS );
                    Parameters params = new Parameters( );
                    builder = new CombinedConfigurationBuilder( )
                        .configure( params.fileBased( ).setFile( configurationDefinitionFile.toFile( ) ) );
                    configuration = builder.getConfiguration( );
                    configuration.setExpressionEngine( expressionEngine );
                }
                else if ( StringUtils.isNotBlank( configurationDefinition ) )
                {
                    builder = new CombinedConfigurationBuilder( );
                    DefaultExpressionEngine expressionEngine = new DefaultExpressionEngine( DefaultExpressionEngineSymbols.DEFAULT_SYMBOLS );
                    StringSubstitutor substitutor = new StringSubstitutor( StringLookupFactory.INSTANCE.systemPropertyStringLookup( ) );
                    String interpolatedProps = substitutor.replace( configurationDefinition );
                    Parameters p = new Parameters( );
                    ReaderBuilderParameters param = new ReaderBuilderParameters( );
                    param.setReader( new StringReader( interpolatedProps ) );
                    ReaderConfigurationBuilder<XMLConfiguration> defBuilder = new ReaderConfigurationBuilder<>( XMLConfiguration.class ).configure( param );
                    logger.debug( "Loading configuration into commons-configuration, xml {}", interpolatedProps );
                    builder.configure( new Parameters( ).combined( ).setDefinitionBuilder( defBuilder ) );
                    configuration = builder.getConfiguration( );
                    configuration.setExpressionEngine( expressionEngine );
                    //configuration.set
                    configuration.addConfiguration( new SystemConfiguration( ) );
                }
                else
                {
                    logger.debug( "Creating a default configuration - no configuration was provided" );
                    this.isPersistent = false;
                    Parameters params = new Parameters( );
                    builder = new CombinedConfigurationBuilder( )
                        .configure( params.fileBased( ).setURL(
                            getClass( ).getClassLoader( ).getResource( "org/apache/commons/registry/default-config-definition.xml" )
                        ) );
                    configuration = builder.getConfiguration( );

                }

                HierarchicalConfiguration<?> defConfig = builder.getDefinitionBuilder( ).getConfiguration( );
                logger.debug( "Node def children: {}", defConfig.getNodeModel( ).getInMemoryRepresentation( ).getChildren( ) );
                for ( ImmutableNode child : defConfig.getNodeModel().getInMemoryRepresentation().getChildren()) {
                    logger.debug( "Child: {}, Attributes: {}" , child.getNodeName( ), child.getAttributes( ));

                }



                this.configuration = configuration;
                this.configurationBuilder = builder;
                if ( isPersistent )
                {
                    List<String> nameList;
                    if (StringUtils.isEmpty(saveConfigurationName)) {
                        nameList = configuration.getConfigurationNameList( );
                    } else {
                        nameList = Arrays.asList( saveConfigurationName );

                    }
                    logger.debug( "Config: {}", String.join( ",", configuration.getConfigurationNameList( ) ) );
                    logger.debug( "Builder: {}", String.join( ",", builder.builderNames( ) ) );
                    for ( String name : nameList )
                    {
                        if (name != null)
                        {
                            Configuration configInstance = configuration.getConfiguration( name );
                            ConfigurationBuilder builderInstance = builder.getNamedBuilder( name );
                            logger.debug( "Config: {}, Builder: {}", configInstance, builderInstance );
                            if ( configInstance != null && builderInstance != null &&
                                builderInstance instanceof FileBasedConfigurationBuilder &&
                                configInstance instanceof FileBasedConfiguration )
                            {
                                this.saveConfiguration = (FileBasedConfiguration) configInstance;
                                this.saveConfigurationBuilder = (FileBasedConfigurationBuilder) builderInstance;
                                break;
                            }
                        }
                    }
                    if ( saveConfiguration == null )
                    {
                        this.isPersistent = false;
                        logger.warn( "No writable configuration found. That means configuration cannot be saved." );
                    }
                }
            }
            catch ( ConfigurationException e )
            {
                throw new RuntimeException( e.getMessage( ), e );
            }
        }
    }

    @Override
    public void setInitialConfiguration( String configurationDefinition )
    {
        this.configurationDefinition = configurationDefinition;
    }

    @Override
    public void setInitialConfigurationFile( Path configurationDefinitionFile )
    {
        this.configurationDefinitionFile = configurationDefinitionFile;
    }

    @Override
    public Registry getSection( String name )
    {
        if (!StringUtils.isEmpty( name ) && configuration instanceof CombinedConfiguration)
        {
            CombinedConfiguration combinedConfiguration = (CombinedConfiguration) configuration;
            Configuration configuration = combinedConfiguration.getConfiguration( name );
            try
            {
                ConfigurationBuilder<? extends Configuration> builder = ( (CombinedConfigurationBuilder) configurationBuilder ).getNamedBuilder( name );
                return configuration == null ? null : new CommonsConfigurationRegistry( builder, configuration );
            }
            catch ( ConfigurationException e )
            {
                logger.error( "Unable to retrieve builder for configuration {}", name );
                return null;
            }
        } else {
            return null;
        }
    }

    public String getPropertyDelimiter( )
    {
        return propertyDelimiter;
    }

    public void setPropertyDelimiter( String propertyDelimiter )
    {
        this.propertyDelimiter = propertyDelimiter;
    }

    public void setSaveConfigurationName(String name) {
        synchronized (this)
        {
            this.saveConfigurationName = name;
        }
    }

    public String getSaveConfigurationName() {
        return this.saveConfigurationName;
    }

    public FileBasedConfiguration getSaveConfiguration( )
    {
        return saveConfiguration;
    }

    @Override
    public boolean isPersistent( )
    {
        return isPersistent;
    }

    @Override
    public void setPersistent( boolean persistent )
    {
        synchronized (this)
        {
            isPersistent = persistent;
        }
    }
}
