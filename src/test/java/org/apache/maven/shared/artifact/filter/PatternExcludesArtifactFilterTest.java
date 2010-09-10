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

import org.apache.maven.artifact.resolver.filter.ArtifactFilter;

import java.util.List;

import junit.framework.TestCase;

public class PatternExcludesArtifactFilterTest extends TestCase
{

    private final PatternArtifactFilterTCK tck = new PatternArtifactFilterTCK()
    {

        protected ArtifactFilter createFilter( final List patterns )
        {
            return new PatternExcludesArtifactFilter( patterns );
        }

        protected ArtifactFilter createFilter( final List patterns, final boolean actTransitively )
        {
            return new PatternExcludesArtifactFilter( patterns, actTransitively );
        }

    };

    public void testShouldTriggerBothPatternsWithNonColonWildcards()
    {
        tck.testShouldTriggerBothPatternsWithNonColonWildcards( true );
    }

    public void testIncludeWhenPatternMatchesDepTrailWithTransitivityUsingNonColonWildcard()
    {
        tck.testIncludeWhenPatternMatchesDepTrailWithTransitivityUsingNonColonWildcard( true );
    }

    public void testShouldTriggerBothPatternsWithWildcards()
    {
        tck.testShouldTriggerBothPatternsWithWildcards( true );
    }

    public void testShouldNotIncludeDirectlyMatchedArtifactByDependencyConflictId()
    {
        tck.testShouldIncludeDirectlyMatchedArtifactByDependencyConflictId( true );
    }

    public void testShouldNotIncludeDirectlyMatchedArtifactByGroupIdArtifactId()
    {
        tck.testShouldIncludeDirectlyMatchedArtifactByGroupIdArtifactId( true );
    }

    public void testShouldNotIncludeWhenPatternMatchesDependencyTrailAndTransitivityIsEnabled()
    {
        tck.testShouldIncludeWhenPatternMatchesDependencyTrailAndTransitivityIsEnabled( true );
    }

    public void testShouldIncludeWhenArtifactIdDiffers()
    {
        tck.testShouldNotIncludeWhenArtifactIdDiffers( true );
    }

    public void testShouldIncludeWhenBothIdElementsDiffer()
    {
        tck.testShouldNotIncludeWhenBothIdElementsDiffer( true );
    }

    public void testShouldIncludeWhenGroupIdDiffers()
    {
        tck.testShouldNotIncludeWhenGroupIdDiffers( true );
    }

    public void testShouldIncludeWhenNegativeMatch()
    {
        tck.testShouldNotIncludeWhenNegativeMatch( true );
    }

    public void testShouldNotIncludeWhenWildcardMatchesInsideSequence()
    {
        tck.testShouldIncludeWhenWildcardMatchesInsideSequence( true );
    }

    public void testShouldIncludeWhenWildcardMatchesOutsideSequence()
    {
        tck.testShouldIncludeWhenWildcardMatchesOutsideSequence( true );
    }

    public void testShouldIncludeTransitiveDependencyWhenWildcardMatchesButDoesntMatchParent()
    {
        tck.testShouldIncludeTransitiveDependencyWhenWildcardMatchesButDoesntMatchParent( true );
    }

    // See comment in TCK.
    // public void testShouldIncludeDirectDependencyWhenInvertedWildcardMatchesButDoesntMatchTransitiveChild()
    // {
    // tck.testShouldIncludeDirectDependencyWhenInvertedWildcardMatchesButDoesntMatchTransitiveChild( true );
    // }
}
