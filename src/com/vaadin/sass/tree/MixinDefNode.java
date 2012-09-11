/*
 * Copyright 2011 Vaadin Ltd.
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

package com.vaadin.sass.tree;

import java.util.ArrayList;
import java.util.Collection;

import com.vaadin.sass.util.DeepCopy;

public class MixinDefNode extends Node implements IVariableNode {
    private static final long serialVersionUID = 5469294053247343948L;

    private String name;
    private ArrayList<VariableNode> arglist;
    private String args;
    private String body;

    public MixinDefNode(String name, Collection<VariableNode> args) {
        super();
        this.name = name;
        arglist = new ArrayList<VariableNode>();
        if (args != null && !args.isEmpty()) {
            arglist.addAll(args);
        }
    }

    public MixinDefNode(String name, String args, String body) {
        this.name = name;
        this.args = args;
        this.body = body;
    }

    @Override
    public String toString() {
        return "Mixin Definition Node: {name: " + name + ", args: " + args
                + ", body: " + body + "}";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<VariableNode> getArglist() {
        return arglist;
    }

    public void setArglist(ArrayList<VariableNode> arglist) {
        this.arglist = arglist;
    }

    @Override
    public void replaceVariables(ArrayList<VariableNode> variables) {
        for (final VariableNode var : variables) {
            for (final VariableNode arg : new ArrayList<VariableNode>(arglist)) {

                if (arg.getName().equals(var.getName())) {
                    arglist.add(arglist.indexOf(arg),
                            (VariableNode) DeepCopy.copy(var));
                    arglist.remove(arg);
                }
            }
        }
    }

}
