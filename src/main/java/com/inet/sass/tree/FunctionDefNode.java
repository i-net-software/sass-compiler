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
package com.inet.sass.tree;

import java.util.Collection;
import java.util.Collections;

import com.inet.sass.ScssContext;
import com.inet.sass.parser.FormalArgumentList;

public class FunctionDefNode extends DefNode {
    public FunctionDefNode( String name, FormalArgumentList args ) {
        super( name, args );
    }

    private FunctionDefNode(FunctionDefNode nodeToCopy) {
        super(nodeToCopy);
    }

    @Override
    public FunctionDefNode copy() {
        return new FunctionDefNode(this);
    }

    @Override
    public Collection<Node> traverse(ScssContext context) {
        context.defineFunction(this);
        setDefinitionScope(context.getCurrentScope());
        return Collections.emptyList();
    }

}
