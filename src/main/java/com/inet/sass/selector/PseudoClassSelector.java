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
package com.inet.sass.selector;

import com.inet.sass.ScssContext;
import com.inet.sass.parser.StringInterpolationSequence;

/**
 * Single CSS3 pseudo-class selector such as ":active" or ":nth-child(2)".
 * @see SimpleSelectorSequence
 * @see Selector
 */
public class PseudoClassSelector extends SimpleSelector {
    private StringInterpolationSequence pseudoClass;
    private StringInterpolationSequence argument;

    public PseudoClassSelector( StringInterpolationSequence pseudoClass, StringInterpolationSequence argument ) {
        this.pseudoClass = pseudoClass;
        this.argument = argument;
    }

    public StringInterpolationSequence getClassValue() {
        return pseudoClass;
    }

    @Override
    public String toString() {
        if( argument == null ) {
            return ":" + getClassValue();
        } else {
            return ":" + getClassValue() + "(" + argument + ")";
        }
    }

    @Override
    public PseudoClassSelector replaceVariables( ScssContext context ) {
        if( argument == null ) {
            if( pseudoClass.containsInterpolation() ) {
                return new PseudoClassSelector( pseudoClass.replaceVariables( context ), null );
            }
        } else {
            if( pseudoClass.containsInterpolation() || argument.containsInterpolation() ) {
                return new PseudoClassSelector( pseudoClass.replaceVariables( context ), argument.replaceVariables( context ) );
            }
        }
        return this;
    }
}
