package org.apache.archiva.components.registry;

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

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * The Plexus registry is a single source of external configuration for Plexus components and applications.
 * It can be used by components to source configuration, knowing that it can be used from within applications
 * without the information being hard coded into the component.
 */
public interface Registry
{

    /**
     * Dump the entire registry to a string, for debugging purposes.
     *
     * @return the registry contents
     */
    String dump( );

    /**
     * Get a string value from the registry. If not found, <code>null</code> is returned.
     *
     * @param key the key in the registry
     * @return the value
     */
    String getString( String key );

    /**
     * Get a string value from the registry. If not found, the default value is used.
     *
     * @param key          the key in the registry
     * @param defaultValue the default value
     * @return the value
     */
    String getString( String key, String defaultValue );

    /**
     * Set a string value in the registry.
     *
     * @param key   the key in the registry
     * @param value the value to set
     */
    void setString( String key, String value );

    /**
     * Get an integer value from the registry. If not found, an exception is thrown.
     *
     * @param key the key in the registry
     * @return the value
     * @throws java.util.NoSuchElementException if the key is not found
     */
    int getInt( String key );

    /**
     * Get an integer value from the registry. If not found, the default value is used.
     *
     * @param key          the key in the registry
     * @param defaultValue the default value
     * @return the value
     */
    int getInt( String key, int defaultValue );

    /**
     * Set an integer value in the registry.
     *
     * @param key   the key in the registry
     * @param value the value to set
     */
    void setInt( String key, int value );

    /**
     * Get a boolean value from the registry. If not found, an exception is thrown.
     *
     * @param key the key in the registry
     * @return the value
     * @throws java.util.NoSuchElementException if the key is not found
     */
    boolean getBoolean( String key );

    /**
     * Get a boolean value from the registry. If not found, the default value is used.
     *
     * @param key          the key in the registry
     * @param defaultValue the default value
     * @return the value
     */
    boolean getBoolean( String key, boolean defaultValue );

    /**
     * Set a boolean value in the registry.
     *
     * @param key   the key in the registry
     * @param value the value to set
     */
    void setBoolean( String key, boolean value );

    /**
     * Load configuration from the given classloader resource.
     *
     * @param resource the location to load the configuration from
     * @throws RegistryException if a problem occurred reading the resource to add to the registry
     */
    void addConfigurationFromResource( String resource )
        throws RegistryException;

    /**
     * Load configuration from the given classloader resource.
     *
     * @param resource the location to load the configuration from
     * @param prefix   the location to add the configuration at in the registry
     * @throws RegistryException if a problem occurred reading the resource to add to the registry
     */
    void addConfigurationFromResource( String resource, String prefix )
        throws RegistryException;

    /**
     * Load configuration from the given file.
     *
     * @param file the location to load the configuration from
     * @throws RegistryException if a problem occurred reading the resource to add to the registry
     */
    void addConfigurationFromFile( Path file )
        throws RegistryException;

    /**
     * Load configuration from the given file.
     *
     * @param file   the location to load the configuration from
     * @param prefix the location to add the configuration at in the registry
     * @throws RegistryException if a problem occurred reading the resource to add to the registry
     */
    void addConfigurationFromFile( Path file, String prefix )
        throws RegistryException;

    /**
     * Determine if the registry contains any elements.
     *
     * @return whether the registry contains any elements
     */
    boolean isEmpty( );

    /**
     * Get a list of strings at the given key in the registry.
     *
     * @param key the key to lookup
     * @return the list of strings
     */
    List getList( String key );

    /**
     * TODO move to Map which is more generic ?
     * Get the properties at the given key in the registry.
     *
     * @param key the key to lookup
     * @return the properties
     */
    Properties getProperties( String key );

    /**
     * Get a subset of the registry, for all keys descended from the given key.
     *
     * @param key the key to take the subset from
     * @return the registry subset
     */
    Registry getSubset( String key );

    /**
     * Get a list of subsets of the registry, for all keys descended from the given key.
     *
     * @param key the key to take the subsets from
     * @return the registry subsets
     */
    List getSubsetList( String key );

    /**
     * Sets the configuration for this registry. The format of the configuration string is dependent
     * on the implementation. The standard implementation uses the xml format of a commons configuration 2
     * combined configuration definition.
     *
     * @param configurationDefinition the string with the configuration definition
     */
    void setInitialConfiguration( String configurationDefinition );

    /**
     * Sets the file that contains the configuration for this registry. The format of the configuration file is dependent
     * on the implementation. The standard implementation uses the xml format of a commons configuration 2
     * combined configuration definition.
     * If this parameter is set and points to a valid file, the configurationDefinition string from {@link #setInitialConfiguration(String)} method is ignored.
     *
     * @param configurationDefinitionFile
     */
    void setInitialConfigurationFile( Path configurationDefinitionFile );

    /**
     * Get a subsection of the registry, identified by the given name. If it doesn't exist, <code>null</code> will be
     * returned.
     *
     * @param name registry section name
     * @return the registry
     */
    Registry getSection( String name );

    /**
     * Save any changes to the registry since it was loaded.
     *
     * @throws RegistryException             if there was a problem saving the registry
     * @throws UnsupportedOperationException if the registry is not writable
     */
    void save( )
        throws RegistryException, UnsupportedOperationException;

    /**
     * Add a change listener. Note that settings this on the base registry will only detect 'invalidation' events, not
     * individual changes. You should retrieve the named sub-registry to listen for changes.
     *
     * TODO: this isn't ideal, so maybe fix combined configuration to re-fire it's events to it's own listeners in the c-c implementation
     *
     * @param listener the listener
     */
    void addChangeListener( RegistryListener listener );

    /**
     * @param listener
     * @return <code>true</code> if has been removed
     * @since 2.3
     */
    boolean removeChangeListener( RegistryListener listener );

    /**
     * Get all the keys in this registry. Keys are only retrieved at a depth of 1.
     *
     * @return the set of keys
     */
    Collection<String> getKeys( );

    /**
     * Get all the keys in this registry.
     *
     * @return the set of keys
     * @since 2.1
     */
    Collection<String> getFullKeys( );

    /**
     * Remove a keyed element from the registry.
     *
     * @param key the key to remove
     */
    void remove( String key );

    /**
     * Remove a keyed subset of the registry.
     *
     * @param key the subset to remove
     */
    void removeSubset( String key );

    /**
     * Initializes the registry. The registry must be initialized before the registry is used or modified.
     * There are some parameters that can only be changed before initialization.
     *
     * @throws RegistryException if the initialization failed
     */
    void initialize( ) throws RegistryException;

    /**
     * Returns true, if this registry should write write changes persistently.
     * @return true, if changes can be persisted, otherwise false.
     */
    boolean isPersistent( );

    /**
     * If true, changes can be written persistently by the save method, otherwise, changes to the configuration
     * will be only in memory.
     *
     * This method must be called before the {@link #initialize()} method. After calling {@link #initialize()}, it will have no effect.
     *
     * @param isPersistent true, if changes can be written by the save method
     */
    void setPersistent(boolean isPersistent);

}
