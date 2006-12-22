package org.apache.maven.user.acegi.acl.basic;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.acegisecurity.acl.basic.SimpleAclEntry;

/**
 * Extends {@link SimpleAclEntry} to allow more combinations of permissions.
 * 
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 */
public class ExtendedSimpleAclEntry
    extends SimpleAclEntry
{

    public static final int EXECUTE = CREATE;

    public static final int READ_WRITE_EXECUTE_DELETE = READ_WRITE_CREATE_DELETE;

    public static final int READ_WRITE_EXECUTE = READ_WRITE_CREATE;

    public static final int READ_EXECUTE = READ | EXECUTE;

    public static final int WRITE_EXECUTE = WRITE | EXECUTE;

    private static final int[] VALID_PERMISSIONS = new int[DELETE * 2];

    static
    {
        /* DELETE is the highest number 16, so we have 16 multiplied by 2 permissions */
        for ( int i = 0; i < VALID_PERMISSIONS.length; i++ )
        {
            VALID_PERMISSIONS[i] = i;
        }
    }

    /**
     * @return a copy of the permissions array, changes to the values won't affect this class.
     */
    public int[] getValidPermissions()
    {
        return (int[]) VALID_PERMISSIONS.clone();
    }
}