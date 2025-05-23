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

package com.inet.sass.function;

import com.inet.sass.ScssContext;
import com.inet.sass.parser.ArgumentList;
import com.inet.sass.parser.FormalArgumentList;
import com.inet.sass.parser.LexicalUnitImpl;
import com.inet.sass.parser.ParseException;
import com.inet.sass.parser.SassListItem;

class MinMaxFunctionGenerator extends AbstractFunctionGenerator {

    private static String[] argumentNames = { "numbers" };

    MinMaxFunctionGenerator() {
        super(createArgumentList(argumentNames, true), "min", "max");
    }

    @Override
    protected SassListItem computeForArgumentList(ScssContext context,
            LexicalUnitImpl function, FormalArgumentList actualArguments) {
        ArgumentList params = (ArgumentList) getParam(actualArguments, 0);
        if (params.size() == 0) {
            throw new ParseException("The function "
                    + function.getFunctionName()
                    + " requires at least one parameter", function);
        }
        // keep the unit of the result like sass-lang does
        LexicalUnitImpl result = getParam(function, params.get(0));
        for (int i = 1; i < params.size(); ++i) {
            LexicalUnitImpl value = getParam(function, params.get(i));
            if ("min".equals(function.getFunctionName())) {
                if (value.getDoubleValue() < result.getDoubleValue()) {
                    result = value;
                }
            } else {
                if (value.getDoubleValue() > result.getDoubleValue()) {
                    result = value;
                }
            }
        }
        return result;
    }

    private LexicalUnitImpl getParam(LexicalUnitImpl function,
            SassListItem param) {
        if (!(param instanceof LexicalUnitImpl)
                || !((LexicalUnitImpl) param).isNumber()) {
            throw new ParseException("The parameters to the function "
                    + function.getFunctionName() + " must be numerical", param);
        }
        return (LexicalUnitImpl) param;
    }
}
