/*
 * Copyright 2023 i-net software
 * Copyright 2000-2014 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.vaadin.sass.testcases.scss;

import org.junit.Assert;
import org.w3c.css.sac.CSSException;

import com.vaadin.sass.internal.handler.SCSSErrorHandler;

public class AssertErrorHandler extends SCSSErrorHandler {
    @Override
    public void error( Throwable th ) throws CSSException {
        throw new AssertionError( th );
    }

    @Override
    public void error( String msg ) throws CSSException {
        Assert.fail( msg );
    }

    @Override
    public void warning( Throwable th ) {
        th.printStackTrace( System.out );
    }

    @Override
    public void warning( String msg ) {
        System.out.println( msg );
    }

    @Override
    public void debug( String msg ) {
        System.out.println( msg );
    }
}