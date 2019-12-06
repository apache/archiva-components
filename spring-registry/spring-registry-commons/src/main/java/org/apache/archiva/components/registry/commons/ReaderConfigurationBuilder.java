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

import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.builder.BuilderParameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileBased;
import org.apache.commons.configuration2.io.FileHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.util.Map;

public class ReaderConfigurationBuilder<T extends ImmutableConfiguration> extends BasicConfigurationBuilder<T>
{

    private static final Logger log = LoggerFactory.getLogger( ReaderConfigurationBuilder.class );

    private T configuration;

    private Reader inputReader;

    public ReaderConfigurationBuilder( Class<? extends T> resCls )
    {
        super( resCls );
    }

    public ReaderConfigurationBuilder( Class<? extends T> resCls, Map<String, Object> params )
    {
        super( resCls, params );
    }

    public ReaderConfigurationBuilder( Class<? extends T> resCls, Map<String, Object> params, boolean allowFailOnInit )
    {
        super( resCls, params, allowFailOnInit );
    }

    @Override
    public T getConfiguration( ) throws ConfigurationException
    {
        if (configuration!=null) {
            return configuration;
        } else
        {
            T result = super.getConfiguration( );
            log.debug( "Retrieving configuration: {}", result.getClass( ) );
            log.debug( "Params: {}", getParameters( ) );
            inputReader = (Reader) getParameters( ).get( ReaderBuilderParameters.INPUT_READER );
            if ( result instanceof FileBasedConfiguration && inputReader!=null)
            {
                FileHandler fileHandler = new FileHandler( (FileBasedConfiguration) result );
                fileHandler.load( inputReader );
                log.debug( "Loaded from reader" );

            }
            else
            {
                log.warn( "This configuration is not file based" );
            }
            this.configuration = result;
            return result;
        }
    }

    @Override
    public ReaderConfigurationBuilder<T> configure( BuilderParameters... params )
    {
        super.configure( params );
        return this;
    }

    @Override
    public synchronized ReaderConfigurationBuilder<T> setParameters( Map<String, Object> params )
    {
        super.setParameters( params );
        return this;
    }

    @Override
    public synchronized ReaderConfigurationBuilder<T> addParameters( Map<String, Object> params )
    {
        super.addParameters( params );
        return this;
    }

    public Reader getInputReader( )
    {
        return inputReader;
    }

    public void setInputReader( Reader inputReader )
    {
        this.inputReader = inputReader;
    }
}
