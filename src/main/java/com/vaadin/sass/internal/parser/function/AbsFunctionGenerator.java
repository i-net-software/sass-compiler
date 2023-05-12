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
package com.vaadin.sass.internal.parser.function;

import com.vaadin.sass.internal.parser.LexicalUnitImpl;

class AbsFunctionGenerator extends
        AbstractSingleParameterFunctionGenerator {

    private static String[] argumentNames = { "value" };

    AbsFunctionGenerator() {
        super(createArgumentList(argumentNames, false), "abs");
    }

    @Override
    protected LexicalUnitImpl computeForParam( LexicalUnitImpl function, LexicalUnitImpl param ) {
        return param.copyWithValue( Math.abs( param.getFloatValue() ) );
    }

}
