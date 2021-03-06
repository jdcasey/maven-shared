/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.shared.artifact.filter;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.codehaus.plexus.logging.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * TODO: include in maven-artifact in future
 * 
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @see StrictPatternIncludesArtifactFilter
 */
public class PatternIncludesArtifactFilter implements ArtifactFilter, StatisticsReportingArtifactFilter
{
    private final List positivePatterns;

    private final List negativePatterns;

    private final boolean actTransitively;

    private final Set patternsTriggered = new HashSet();

    private final List filteredArtifactIds = new ArrayList();

    public PatternIncludesArtifactFilter( final List patterns )
    {
        this( patterns, false );
    }

    public PatternIncludesArtifactFilter( final List patterns, final boolean actTransitively )
    {
        this.actTransitively = actTransitively;
        final List pos = new ArrayList();
        final List neg = new ArrayList();
        if ( ( patterns != null ) && !patterns.isEmpty() )
        {
            for ( final Iterator it = patterns.iterator(); it.hasNext(); )
            {
                final String pattern = (String) it.next();

                if ( pattern.startsWith( "!" ) )
                {
                    neg.add( pattern.substring( 1 ) );
                }
                else
                {
                    pos.add( pattern );
                }
            }
        }

        positivePatterns = pos;
        negativePatterns = neg;
    }

    public boolean include( final Artifact artifact )
    {
        final boolean shouldInclude = patternMatches( artifact );

        if ( !shouldInclude )
        {
            addFilteredArtifactId( artifact.getId() );
        }

        return shouldInclude;
    }

    protected boolean patternMatches( final Artifact artifact )
    {
        return ( positiveMatch( artifact ) == Boolean.TRUE ) || ( negativeMatch( artifact ) == Boolean.FALSE );
    }

    protected void addFilteredArtifactId( final String artifactId )
    {
        filteredArtifactIds.add( artifactId );
    }

    private Boolean negativeMatch( final Artifact artifact )
    {
        if ( ( negativePatterns == null ) || negativePatterns.isEmpty() )
        {
            return null;
        }
        else
        {
            return Boolean.valueOf( match( artifact, negativePatterns ) );
        }
    }

    protected Boolean positiveMatch( final Artifact artifact )
    {
        if ( ( positivePatterns == null ) || positivePatterns.isEmpty() )
        {
            return null;
        }
        else
        {
            return Boolean.valueOf( match( artifact, positivePatterns ) );
        }
    }

    private boolean match( final Artifact artifact, final List patterns )
    {
        final String shortId = ArtifactUtils.versionlessKey( artifact );
        final String id = artifact.getDependencyConflictId();
        final String wholeId = artifact.getId();

        if ( matchAgainst( wholeId, patterns, false ) )
        {
            return true;
        }

        if ( matchAgainst( id, patterns, false ) )
        {
            return true;
        }

        if ( matchAgainst( shortId, patterns, false ) )
        {
            return true;
        }

        if ( actTransitively )
        {
            final List depTrail = artifact.getDependencyTrail();

            if ( ( depTrail != null ) && depTrail.size() > 1 )
            {
                for ( final Iterator iterator = depTrail.iterator(); iterator.hasNext(); )
                {
                    final String trailItem = (String) iterator.next();
                    if ( matchAgainst( trailItem, patterns, true ) )
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean matchAgainst( final String value, final List patterns, final boolean regionMatch )
    {
        for ( final Iterator iterator = patterns.iterator(); iterator.hasNext(); )
        {
            final String pattern = (String) iterator.next();

            final String[] patternTokens = pattern.split( ":" );
            final String[] tokens = value.split( ":" );

            // fail immediately if pattern tokens outnumber tokens to match
            boolean matched = ( patternTokens.length <= tokens.length );

            for ( int i = 0; matched && i < patternTokens.length; i++ )
            {
                matched = matches( tokens[i], patternTokens[i] );
            }

            // // case of starting '*' like '*:jar:*'
            if ( !matched && patternTokens.length < tokens.length && patternTokens.length > 0
                            && "*".equals( patternTokens[0] ) )
            {
                matched = true;
                for ( int i = 0; matched && i < patternTokens.length; i++ )
                {
                    matched = matches( tokens[i + ( tokens.length - patternTokens.length )], patternTokens[i] );
                }
            }

            if ( matched )
            {
                patternsTriggered.add( pattern );
                return true;
            }

            if ( regionMatch && value.indexOf( pattern ) > -1 )
            {
                patternsTriggered.add( pattern );
                return true;
            }

        }
        return false;

    }

    /**
     * Gets whether the specified token matches the specified pattern segment.
     * 
     * @param token
     *            the token to check
     * @param pattern
     *            the pattern segment to match, as defined above
     * @return <code>true</code> if the specified token is matched by the specified pattern segment
     */
    private boolean matches( final String token, final String pattern )
    {
        boolean matches;

        // support full wildcard and implied wildcard
        if ( "*".equals( pattern ) || pattern.length() == 0 )
        {
            matches = true;
        }
        // support contains wildcard
        else if ( pattern.startsWith( "*" ) && pattern.endsWith( "*" ) )
        {
            final String contains = pattern.substring( 1, pattern.length() - 1 );

            matches = ( token.indexOf( contains ) != -1 );
        }
        // support leading wildcard
        else if ( pattern.startsWith( "*" ) )
        {
            final String suffix = pattern.substring( 1, pattern.length() );

            matches = token.endsWith( suffix );
        }
        // support trailing wildcard
        else if ( pattern.endsWith( "*" ) )
        {
            final String prefix = pattern.substring( 0, pattern.length() - 1 );

            matches = token.startsWith( prefix );
        }
        // support versions range
        else if ( pattern.startsWith( "[" ) || pattern.startsWith( "(" ) )
        {
            matches = isVersionIncludedInRange( token, pattern );
        }
        // support exact match
        else
        {
            matches = token.equals( pattern );
        }

        return matches;
    }

    private boolean isVersionIncludedInRange( final String version, final String range )
    {
        try
        {
            return VersionRange.createFromVersionSpec( range ).containsVersion( new DefaultArtifactVersion( version ) );
        }
        catch ( final InvalidVersionSpecificationException e )
        {
            return false;
        }
    }

    public void reportMissedCriteria( final Logger logger )
    {
        // if there are no patterns, there is nothing to report.
        if ( !positivePatterns.isEmpty() || !negativePatterns.isEmpty() )
        {
            final List missed = new ArrayList();
            missed.addAll( positivePatterns );
            missed.addAll( negativePatterns );

            missed.removeAll( patternsTriggered );

            if ( !missed.isEmpty() && logger.isWarnEnabled() )
            {
                final StringBuffer buffer = new StringBuffer();

                buffer.append( "The following patterns were never triggered in this " );
                buffer.append( getFilterDescription() );
                buffer.append( ':' );

                for ( final Iterator it = missed.iterator(); it.hasNext(); )
                {
                    final String pattern = (String) it.next();

                    buffer.append( "\no  \'" ).append( pattern ).append( "\'" );
                }

                buffer.append( "\n" );

                logger.warn( buffer.toString() );
            }
        }
    }

    public String toString()
    {
        return "Includes filter:" + getPatternsAsString();
    }

    protected String getPatternsAsString()
    {
        final StringBuffer buffer = new StringBuffer();
        for ( final Iterator it = positivePatterns.iterator(); it.hasNext(); )
        {
            final String pattern = (String) it.next();

            buffer.append( "\no \'" ).append( pattern ).append( "\'" );
        }

        return buffer.toString();
    }

    protected String getFilterDescription()
    {
        return "artifact inclusion filter";
    }

    public void reportFilteredArtifacts( final Logger logger )
    {
        if ( !filteredArtifactIds.isEmpty() && logger.isDebugEnabled() )
        {
            final StringBuffer buffer =
                new StringBuffer( "The following artifacts were removed by this " + getFilterDescription() + ": " );

            for ( final Iterator it = filteredArtifactIds.iterator(); it.hasNext(); )
            {
                final String artifactId = (String) it.next();

                buffer.append( '\n' ).append( artifactId );
            }

            logger.debug( buffer.toString() );
        }
    }

    public boolean hasMissedCriteria()
    {
        // if there are no patterns, there is nothing to report.
        if ( !positivePatterns.isEmpty() || !negativePatterns.isEmpty() )
        {
            final List missed = new ArrayList();
            missed.addAll( positivePatterns );
            missed.addAll( negativePatterns );

            missed.removeAll( patternsTriggered );

            return !missed.isEmpty();
        }

        return false;
    }

}
