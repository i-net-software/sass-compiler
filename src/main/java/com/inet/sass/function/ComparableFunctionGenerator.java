/*
 * Copyright 2023 i-net software
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

package com.inet.sass.function;

import com.inet.sass.ScssContext;
import com.inet.sass.parser.FormalArgumentList;
import com.inet.sass.parser.LexicalUnitImpl;
import com.inet.sass.parser.SassListItem;

class ComparableFunctionGenerator extends AbstractFunctionGenerator {

    ComparableFunctionGenerator() {
        super( createArgumentList( new String[] { "x", "y" }, false ), "comparable" );
    }

    @Override
    protected SassListItem computeForArgumentList( ScssContext context, LexicalUnitImpl function, FormalArgumentList actualArguments ) {
        LexicalUnitImpl x = getParam( actualArguments, "x" ).getContainedValue();
        LexicalUnitImpl y = getParam( actualArguments, "y" ).getContainedValue();
        String value;
        try {
            x.checkAndGetUnit(y);
            value = "true";
        } catch( Exception e ) {
            value = "false";
        }
        return LexicalUnitImpl.createIdent( x.getUri(), x.getLineNumber(), x.getColumnNumber(), value );
    }
}