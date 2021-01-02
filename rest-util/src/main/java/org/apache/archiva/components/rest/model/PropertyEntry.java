package org.apache.archiva.components.rest.model;/*
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

import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author Martin Stockhammer <martin_s@apache.org>
 */
@XmlRootElement(name="propertyEntry")
@Schema(name="PropertyEntry", description = "Key/Value-Pair")
public class PropertyEntry implements Serializable
{
    private static final long serialVersionUID = -4486042628710420898L;
    private String key;
    private String value;

    public PropertyEntry() {

    }

    public PropertyEntry(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey( )
    {
        return key;
    }

    public void setKey( String key )
    {
        this.key = key;
    }

    public String getValue( )
    {
        return value;
    }

    public void setValue( String value )
    {
        this.value = value;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o ) return true;
        if ( o == null || getClass( ) != o.getClass( ) ) return false;

        PropertyEntry that = (PropertyEntry) o;

        if ( !Objects.equals( key, that.key ) ) return false;
        return Objects.equals( value, that.value );
    }

    @Override
    public int hashCode( )
    {
        int result = key != null ? key.hashCode( ) : 0;
        result = 31 * result + ( value != null ? value.hashCode( ) : 0 );
        return result;
    }

    @Override
    public String toString( )
    {
        final StringBuilder sb = new StringBuilder( "PropertyEntry{" );
        sb.append( "key='" ).append( key ).append( '\'' );
        sb.append( ", value='" ).append( value ).append( '\'' );
        sb.append( '}' );
        return sb.toString( );
    }
}
