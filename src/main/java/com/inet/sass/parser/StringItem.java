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
package com.inet.sass.parser;

import static com.inet.sass.parser.SCSSLexicalUnit.SCSS_STRING;

import com.inet.sass.ScssContext;
import com.inet.sass.tree.Node.BuildStringStrategy;

/**
 * StringItem is a wrapper class that allows strings to be stored in lists
 * taking SassListItems.
 * 
 * @author Vaadin
 * 
 */
public class StringItem implements SassListItem {
    String value;

    public StringItem(String s) {
        value = s;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short getItemType() {
        return SCSS_STRING;
    }

    @Override
    public int getLineNumber() {
        return 0;
    }

    @Override
    public int getColumnNumber() {
        return 0;
    }

    @Override
    public boolean containsArithmeticalOperator() {
        return false;
    }

    @Override
    public SassListItem evaluateFunctionsAndExpressions(ScssContext context,
            boolean evaluateArithmetics) {
        return this;
    }

    @Override
    public StringItem updateUrl(String prefix) {
        // nothing to update
        return this;
    }

    @Override
    public String printState() {
        return value;
    }

    @Override
    public String buildString(BuildStringStrategy strategy) {
        return value;
    }

    @Override
    public String unquotedString() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public LexicalUnitImpl getContainedValue() {
        throw new ParseException(
                "getContainedValue() is not supported by StringValue.");
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StringItem)) {
            return false;
        }
        StringItem other = (StringItem) o;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}