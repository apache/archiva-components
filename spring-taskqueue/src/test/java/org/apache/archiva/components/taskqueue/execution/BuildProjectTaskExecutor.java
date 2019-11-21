package org.apache.archiva.components.taskqueue.execution;

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

import org.apache.archiva.components.taskqueue.BuildProjectTask;
import org.apache.archiva.components.taskqueue.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:kenney@apache.org">Kenney Westerhof</a>
 */
@Service( "taskExecutor#build-project" )
public class BuildProjectTaskExecutor
    implements TaskExecutor
{

    private Logger logger = LoggerFactory.getLogger( getClass( ) );

    public void executeTask( Task task0 )
        throws TaskExecutionException
    {
        BuildProjectTask task = (BuildProjectTask) task0;

        task.start( );

        logger.info( "Task:{} cancelled: {}; done: {}", task, task.isCancelled( ), task.isDone( ) );

        long time = System.currentTimeMillis( );

        long endTime = task.getExecutionTime( ) + time;

        for ( long timeToSleep = endTime - time; timeToSleep > 0; timeToSleep = endTime - System.currentTimeMillis( ) )
        {
            try
            {
                logger.info( "Sleeping {} ms (interrupts ignored: {} )", timeToSleep, task.ignoreInterrupts( ) );
                Thread.sleep( timeToSleep );

                task.done( );

                logger.info( "Task completed normally: {} cancelled: {}; done: {}", task, task.isCancelled( ),
                    task.isDone( ) );
            }
            catch ( InterruptedException e )
            {
                if ( !task.ignoreInterrupts( ) )
                {
                    task.cancel( );

                    logger.info( "Task cancelled: {} cancelled: {} ; done: {}", task, task.isCancelled( ),
                        task.isDone( ) );

                    throw new TaskExecutionException( "Never interrupt sleeping threads! :)", e );
                }
                else
                {
                    logger.info( "Ignoring interrupt" );
                }
            }
        }

    }
}
