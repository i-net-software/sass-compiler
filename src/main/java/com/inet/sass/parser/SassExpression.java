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

import static com.inet.sass.parser.SCSSLexicalUnit.SCSS_EXPRESSION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.inet.sass.ScssContext;
import com.inet.sass.expression.ArithmeticExpressionEvaluator;
import com.inet.sass.expression.BinaryOperator;
import com.inet.sass.tree.Node;
import com.inet.sass.tree.Node.BuildStringStrategy;

/**
 * SassExpressions are used for representing and evaluating arithmetic
 * expressions.
 * 
 * @author Vaadin
 * 
 */
public class SassExpression implements SassListItem {

    private List<SassListItem> items;
    private int line = 0;
    private int column = 0;

    /**
     * Constructs a SassExpression from a list of items. The list is not copied
     * but used directly.
     * 
     * @param items
     *            list of items (not copied but used directly)
     */
    private SassExpression(List<SassListItem> items) {
        if (!items.isEmpty()) {
            line = items.get(0).getLineNumber();
            column = items.get(0).getColumnNumber();
        }

        this.items = items;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short getItemType() {
        return SCSS_EXPRESSION;
    }

    /**
     * Creates a new expression containing the elements of the parameter items
     * but with trailing whitespace items eliminated. If items contains only one
     * element excluding the trailing whitespace, returns the only contained
     * element. Otherwise returns a SassExpression.
     * 
     * @param items
     *            one or more SassListItems.
     * @return A SassExpression corresponding to items. If there is only one
     *         item after the removal of whitespace, returns that item instead
     *         of a SassExpression.
     */
    public static SassListItem createExpression(SassListItem... items) {
        return createExpression(Arrays.asList(items));
    }

    /**
     * Creates a new expression containing the elements of the parameter items
     * but with trailing whitespace items eliminated. If items contains only one
     * element excluding the trailing whitespace, returns the only contained
     * element. Otherwise returns a SassExpression.
     * 
     * @param items
     *            A list of SassListItems.
     * @return A SassExpression corresponding to items. If there is only one
     *         item after the removal of whitespace, returns that item instead
     *         of a SassExpression.
     */
    public static SassListItem createExpression(List<SassListItem> items) {
        // filter out trailing whitespace
        int lastNonWhitespace = items.size() - 1;
        while (lastNonWhitespace > 0
                && items.get(lastNonWhitespace) == LexicalUnitImpl.WHITESPACE) {
            --lastNonWhitespace;
        }
        if (lastNonWhitespace < items.size() - 1) {
            // copy needed as subList() returns a non-serializable list
            items = new ArrayList<SassListItem>(items.subList(0,
                    lastNonWhitespace + 1));
        }
        if (items.size() == 1) {
            return items.get(0);
        } else {
            return new SassExpression(items);
        }
    }

    @Override
    public int getLineNumber() {
        return line;
    }

    @Override
    public int getColumnNumber() {
        return column;
    }

    public boolean containsArithmeticalOperator() {
        for (SassListItem item : items) {
            if (item.containsArithmeticalOperator()) {
                return true;
            }
        }
        int previousIndex = getNextNonspaceIndex(items, 0);
        int currentIndex = getNextNonspaceIndex(items, previousIndex + 1);
        int nextIndex = getNextNonspaceIndex(items, currentIndex + 1);
        if (nextIndex >= items.size()) {
            return false;
        }
        while (nextIndex < items.size()) {
            SassListItem previous = items.get(previousIndex);
            SassListItem current = items.get(currentIndex);
            SassListItem next = items.get(nextIndex);
            previousIndex = currentIndex;
            currentIndex = nextIndex;
            nextIndex = getNextNonspaceIndex(items, nextIndex + 1);
            short currentType = current.getItemType();
            if (currentType == BinaryOperator.DIV.type) {
                /*
                 * '/' is treated as an arithmetical operator when one of its
                 * operands is Variable, or there is another binary operator.
                 * Otherwise, '/' is treated as a CSS operator. If interpolation
                 * occurs on either side of a symbol '/', '*', '+' or ´-', the
                 * symbol is not treated as an arithmetical operator.
                 */
                if ((isVariable(previous) || isVariable(next))
                        && !containsInterpolation(previous)
                        && !containsInterpolation(next)) {
                    return true;
                }
            } else if (isOperator(currentType)
                    && !containsInterpolation(previous)
                    && !containsInterpolation(next)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOperator(short type) {
        for (BinaryOperator operator : BinaryOperator.values()) {
            if (type == operator.type) {
                return true;
            }
        }
        return false;
    }

    private boolean isVariable(SassListItem item) {
        return item.getItemType() == LexicalUnitImpl.SCSS_VARIABLE;
    }

    private boolean containsInterpolation(SassListItem item) {
        if( item.getClass() == LexicalUnitImpl.class ) {
            return ((LexicalUnitImpl)item).containsInterpolation();
        }
        return false;
    }

    /**
     * Returns the index of the next non-whitespace item in list, starting from
     * startIndex (inclusive). If there are no non-whitespace items in
     * list[startIndex...list.size() - 1], returns list.size().
     * 
     * @param list
     *            A list.
     * @param startIndex
     *            The first index included in the search.
     * @return The smallest index i such that i &lt;= startIndex &amp;&amp; list.get(i)
     *         does not represent whitespace. If no such index exists, returns
     *         list.size().
     */
    public static int getNextNonspaceIndex(List<SassListItem> list,
            int startIndex) {
        for (int i = startIndex; i < list.size(); ++i) {
            if (!(list.get(i) == LexicalUnitImpl.WHITESPACE)) {
                return i;
            }
        }
        return list.size();
    }

    @Override
    public SassListItem evaluateFunctionsAndExpressions( ScssContext context, boolean evaluateArithmetics ) {
        if( evaluateArithmetics && !items.isEmpty() ) {
            return ArithmeticExpressionEvaluator.evaluate( context, items );
        }
        List<SassListItem> list = new ArrayList<SassListItem>();
        for( SassListItem item : items ) {
            list.add( item.evaluateFunctionsAndExpressions( context, evaluateArithmetics ) );
        }
        return new SassExpression( list );
    }

    @Override
    public SassExpression updateUrl(String prefix) {
        List<SassListItem> newItems = new ArrayList<SassListItem>(items.size());
        for (SassListItem item : items) {
            newItems.add(item.updateUrl(prefix));
        }
        return new SassExpression(newItems);
    }

    @Override
    public String printState() {
        return buildString(Node.PRINT_STRATEGY);
    }

    @Override
    public String buildString(BuildStringStrategy strategy) {
        StringBuilder result = new StringBuilder();
        Iterator<SassListItem> it = items.iterator();
        while (it.hasNext()) {
            SassListItem item = it.next();
            result.append(strategy.build(item));
        }
        return result.toString();
    }

    @Override
    public String toString() {
        String result = "SassExpression[";
        result += buildString(Node.TO_STRING_STRATEGY);
        return result + "]";
    }

    @Override
    public String unquotedString() {
        if (items.size() == 1 && items.get(0) instanceof LexicalUnitImpl) {
            return ((LexicalUnitImpl) items.get(0)).unquotedString();
        }
        return printState();
    }

    @Override
    public LexicalUnitImpl getContainedValue() {
        if (items.size() != 1 || !(items.get(0) instanceof LexicalUnitImpl)) {
            throw new ParseException(
                    "getContainedValue() can only be used for an expression that contains one simple value. Actual value: "
                            + toString());
        }
        return (LexicalUnitImpl) items.get(0);
    }

    /**
     * Tests whether this and o are equal expressions. Two expressions are
     * considered to be equal only if they have equal operands and operators in
     * the same order.
     * 
     * In most cases the results of the expressions should be compared to each
     * other instead of the expressions themselves. For this the expressions can
     * be evaluated using evaluateFunctionsAndExpressions after replacing any
     * variables occurring in the expressions.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SassExpression)) {
            return false;
        }
        SassExpression other = (SassExpression) o;
        if (items.size() != other.items.size()) {
            return false;
        }
        for (int i = 0; i < items.size(); i++) {
            if (!items.get(i).equals(other.items.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (int i = 0; i < items.size(); i++) {
            int currentHash = 0;
            if (items.get(i) != null) {
                currentHash = items.get(i).hashCode();
            }
            result = 41 * result + currentHash;
        }
        return result;
    }
}
