package org.apache.archiva.components.rest.util;/*
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

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 *
 * Helper class that returns combined filter and comparison objects for ordering.
 *
 * The query term may be consist of simple query terms separated by whitespace or attribute queries
 * in the form <code>attribute:query</code>, which means only the attribute is searched for the query string.
 * <br />
 * Example:
 * <dl>
 *     <dt>`user1 test`</dt>
 *     <dd>
 * searches for the tokens user1 and test in the default attributes.
 * </dd>
 * <dt>`user1 name:test`</dt>
 * <dd>searches for the token user1 in the default attributes and for the token test in the attribute name.</dd>
 * </dl>
 *
 *
 * @since 3.0
 * @author Martin Stockhammer <martin_s@apache.org>
 */
public class QueryHelper<T>
{

    private final Map<String, BiPredicate<String, T>> filterMap;
    private final Map<String, Comparator<T>> orderMap;
    private final String[] defaultSearchAttributes;
    private final Predicate<T> DEFAULT_FILTER = ( T att ) -> false;


    /**
     * Initializes a helper with the given default search attributes.
     * @param defaultSearchAttributes the attribute names to use for searching
     */
    public QueryHelper(String[] defaultSearchAttributes) {
        this.filterMap = new HashMap<>( );
        this.orderMap = new HashMap<>( );
        this.defaultSearchAttributes = defaultSearchAttributes;
    }

    /**
     * Creates a new query helper with the given filters and comparators.
     *
     * @param filterMap a map of filters, where the key is the attribute name and the value is a predicate that matches
     *                  the filter value and the object instance.
     * @param orderMap a map of comparators, where key is the attribute name and the value is a comparator for the given
     *                 object instance
     * @param defaultSearchFields A array of attribute names, that are used as default search fields.
     */
    public QueryHelper(Map<String, BiPredicate<String, T>> filterMap, Map<String, Comparator<T>> orderMap,
                       String[] defaultSearchFields)
    {
        this.filterMap = filterMap;
        this.defaultSearchAttributes = defaultSearchFields;
        this.orderMap = new HashMap<>( orderMap );
    }

    /**
     * This adds a null safe comparator, that compares the field values in natural order. Null values are sorted after
     * any other values.
     * @param attributeName the name of the attribute
     * @param keyExtractor the extractor to use for getting the attribute value
     * @param <U> the type that is to be compared by the comparator
     */
    public <U extends Comparable<? super U>> void addNullsafeFieldComparator( String attributeName, Function<? super T, U> keyExtractor) {
        orderMap.put( attributeName, Comparator.comparing( keyExtractor, Comparator.nullsLast( Comparator.naturalOrder( ) ) ) );
    }

    /**
     * Adds a filter for a string attribute.
     * @param attributeName the name of the attribute, this is the name used in the query parameters
     * @param keyExtractor the extractor to use for getting the attribute value, e.g. PropertyEntry::getKey
     */
    public void addStringFilter(String attributeName, Function<? super T, String> keyExtractor) {
        this.filterMap.put( attributeName, ( String q, T r ) -> StringUtils.containsIgnoreCase( keyExtractor.apply( r ), q ) );
    }

    /**
     * Adds a filter for a boolean attribute. The boolean is extracted by Boolean.valueOf()
     * @param attributeName the attribute name
     * @param keyExtractor the extractor to use for getting the attribute value
     */
    public void addBooleanFilter(String attributeName, Function<? super T, Boolean> keyExtractor) {
        this.filterMap.put( attributeName, ( String q, T r ) -> Boolean.valueOf( q ) == keyExtractor.apply( r ) );
    }

    /**
     * Get the comparator for a specific attribute.
     * @param attributeName the name of the attribute.
     * @return the comparator for the attribute, if defined, or otherwise <code>null</code>
     */
    public Comparator<T> getAttributeComparator( String attributeName )
    {
        return orderMap.get( attributeName );
    }

    /**
     * Get the combined order for the given attributes in the given order.
     *
     * @param orderBy the attributes to compare. The first attribute in the list will be used first for comparing.
     * @param ascending <code>true</code>, if the ordering should be ascending, otherwise <code>false</code>
     * @return the comparator for the given order definition.
     * @throws IllegalArgumentException if there is no comparator defined for one of the given orderBy values
     */
    public Comparator<T> getComparator( List<String> orderBy, boolean ascending )
    {
        if ( ascending )
        {
            return orderBy.stream( ).map( this::getAttributeComparator ).filter( Objects::nonNull )
                .reduce( Comparator::thenComparing )
                .orElseThrow( () -> new IllegalArgumentException( "No attribute ordering found" ) );
        }

        else
        {
            return orderBy.stream( ).map( ( String name ) -> getAttributeComparator( name ) == null ? null : getAttributeComparator( name )
                .reversed( ) ).filter( Objects::nonNull ).reduce( Comparator::thenComparing )
                .orElseThrow( () -> new IllegalArgumentException( "No attribute ordering found" ) );
        }
    }

    /**
     * Returns the ordering for the given attributes and the given order string.
     * The order is considered as ascending if the string is not equal to 'desc'
     * @param orderBy the list of attributes to order by
     * @param order the order string, either 'asc' for ascending or 'desc' for descending
     * @return the combined comparator
     */
    public Comparator<T> getComparator(List<String> orderBy, String order) {
        return getComparator( orderBy, this.isAscending( order ) );
    }

    /**
     * Returns a query filter for a specific attribute and query token.
     * @param attributeName the attribute name to filter for.
     * @param queryToken the search token.
     * @return The predicate used to filter the token. If there exists no filter definition for the attribute, it will use a filter,
     * that always returns <code>false</code>
     */
    public Predicate<T> getAttributeQueryFilter( final String attributeName, final String queryToken )
    {
        if ( filterMap.containsKey( attributeName ) )
        {
            return ( T u ) -> filterMap.get( attributeName ).test( queryToken, u );
        }
        else
        {
            return DEFAULT_FILTER;
        }
    }

    /**
     * Returns the combined query filter for the given query terms.
     * The query terms may be either simple strings separated by whitespace or use the
     * <code>attribute:query</code> syntax, that searches only the attribute for the query term.
     * @param queryTerms the query string
     * @return the combined query filter
     */
    public Predicate<T> getQueryFilter( String queryTerms )
    {
        return Arrays.stream( queryTerms.split( "\\s+" ) )
            .map( s -> {
                    if ( s.contains( ":" ) )
                    {
                        String attr = StringUtils.substringBefore( s, ":" );
                        String term = StringUtils.substringAfter( s, ":" );
                        return getAttributeQueryFilter( attr, term );
                    }
                    else
                    {
                        return Arrays.stream( defaultSearchAttributes )
                            .map( att -> getAttributeQueryFilter( att, s ) ).reduce( Predicate::or ).orElseThrow(  () -> new RuntimeException( "Fatal error. No filter predicate found." ));
                    }
                }
            ).reduce( Predicate::or ).orElseThrow( () -> new RuntimeException( "Fatal error. No filter predicate found." ) );
    }

    /**
     * Returns <code>false</code>, if the given order string equals to "desc", otherwise <code>true</code>
     * @param order the string for ordering (asc, desc)
     * @return <code>false</code>, if the string equals to 'desc', otherwise <code>true</code>
     */
    public boolean isAscending(String order) {
        return !"desc".equals( order );
    }
}
