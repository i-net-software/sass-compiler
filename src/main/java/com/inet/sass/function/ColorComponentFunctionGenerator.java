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
import com.inet.sass.parser.FormalArgumentList;
import com.inet.sass.parser.LexicalUnitImpl;
import com.inet.sass.parser.ParseException;
import com.inet.sass.parser.SassListItem;
import com.inet.sass.util.ColorUtil;

class ColorComponentFunctionGenerator extends AbstractFunctionGenerator {

    private static String[] argumentNames = { "color" };

    ColorComponentFunctionGenerator() {
        super(createArgumentList(argumentNames, false), "red", "green", "blue",
                "hue", "saturation", "lightness");
    }

    @Override
    protected SassListItem computeForArgumentList(ScssContext context,
            LexicalUnitImpl function, FormalArgumentList actualArguments) {
        boolean hslComponent = false;
        int componentNumber = 0; // the index of the wanted component in an RGB
                                 // array
        if ("red".equals(function.getFunctionName())) {
            componentNumber = 0;
        } else if ("green".equals(function.getFunctionName())) {
            componentNumber = 1;
        } else if ("blue".equals(function.getFunctionName())) {
            componentNumber = 2;
        } else if ("hue".equals(function.getFunctionName())) {
            componentNumber = 0;
            hslComponent = true;
        } else if ("saturation".equals(function.getFunctionName())) {
            componentNumber = 1;
            hslComponent = true;
        } else if ("lightness".equals(function.getFunctionName())) {
            componentNumber = 2;
            hslComponent = true;
        }
        checkParameters(function, actualArguments);
        LexicalUnitImpl color = getParam(actualArguments, "color")
                .getContainedValue();
        if (hslComponent) {
            double[] components = ColorUtil.colorToHsl(color);
            if (componentNumber == 0) {
                return LexicalUnitImpl.createDEG( color.getUri(), color.getLineNumber(), color.getColumnNumber(), components[componentNumber] );
            } else {
                return LexicalUnitImpl.createPercentage( color.getUri(), color.getLineNumber(), color.getColumnNumber(), components[componentNumber] );
            }
        } else {
            int[] components = ColorUtil.colorToRgb(color);
            return LexicalUnitImpl.createInteger( color.getUri(), color.getLineNumber(), color.getColumnNumber(), components[componentNumber] );
        }
    }

    private void checkParameters(LexicalUnitImpl function,
            FormalArgumentList args) {
        SassListItem arg = getParam(args, "color");
        if (!(arg instanceof LexicalUnitImpl)) {
            throw new ParseException( "Function " + function.getFunctionName() + " must have exactly one single value parameter: " + arg, function );
        }
        LexicalUnitImpl firstParam = (LexicalUnitImpl) arg;
        if (!ColorUtil.isColor(firstParam) && !ColorUtil.isRgba(firstParam)
                && !ColorUtil.isHsla(firstParam)) {
            throw new ParseException( "The parameter of the function " + function.getFunctionName() + " must be a valid color: " + firstParam, function );
        }
    }
}