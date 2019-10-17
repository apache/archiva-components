package org.apache.archiva.redback.components.apacheds;

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

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import java.io.File;
import java.util.Set;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 *
 */
public interface ApacheDs
{

    // ----------------------------------------------------------------------
    // Configuration
    // ----------------------------------------------------------------------

    void setBasedir( File basedir );

    void setPort( int port );

    int getPort();

    void setEnableNetworking( boolean enableNetworking );

    void addPartition( String name, String root, Set indexedAttributes, Attributes partitionAttributes )
        throws NamingException;

    void addPartition( Partition partition )
        throws NamingException;

    /**
     * Creates a partition usable for testing and other light usage.
     *
     * @param name             The name of the partition. Will be used as the directory name when persisted.
     * @param domainComponents E.g. "plexus", "codehaus", "org"
     * @throws NamingException
     */
    Partition addSimplePartition( String name, String[] domainComponents )
        throws NamingException;

    // ----------------------------------------------------------------------
    // Server control
    // ----------------------------------------------------------------------

    void startServer()
        throws Exception;

    void stopServer()
        throws Exception;

    void sync()
        throws Exception;

    boolean isStopped();

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    InitialDirContext getAdminContext()
        throws NamingException;

    InitialDirContext getSystemContext()
        throws NamingException;
}
