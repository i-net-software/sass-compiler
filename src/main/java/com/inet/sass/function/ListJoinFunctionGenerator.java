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

import java.util.ArrayList;

import com.inet.sass.ScssContext;
import com.inet.sass.parser.FormalArgumentList;
import com.inet.sass.parser.LexicalUnitImpl;
import com.inet.sass.parser.SassList;
import com.inet.sass.parser.SassListItem;

class ListJoinFunctionGenerator extends ListFunctionGenerator {

    private static String[] argumentNames = { "list1", "list2", "separator" };
    private static SassListItem[] defaultValues = { null, null,
            LexicalUnitImpl.createIdent("auto") };

    ListJoinFunctionGenerator() {
        super(createArgumentList(argumentNames, defaultValues, false), "join");
    }

    @Override
    protected SassListItem computeForArgumentList(ScssContext context,
            LexicalUnitImpl function, FormalArgumentList actualArguments) {
        SassListItem firstListAsItem = getParam(actualArguments, "list1");
        SassListItem secondListAsItem = getParam(actualArguments, "list2");

        SassList firstList = asList(firstListAsItem);
        SassList secondList = asList(secondListAsItem);
        ArrayList<SassListItem> newList = new ArrayList<SassListItem>();
        for (SassListItem firstItem : firstList) {
            newList.add(firstItem);
        }
        for (SassListItem secondItem : secondList) {
            newList.add(secondItem);
        }

        SassList.Separator sep = getSeparator(getParam(actualArguments,
                "separator"));
        if (sep == null) { // determine the separator in "auto" mode
            sep = getAutoSeparator(firstList, secondList);
        }
        return new SassList(sep, newList);
    }

}
