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

package com.inet.sass.testcases.scss;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;

import com.inet.sass.AbstractTestBase;
import com.inet.sass.ScssStylesheet;
import com.inet.sass.parser.LexicalUnitImpl;
import com.inet.sass.parser.SCSSLexicalUnit;
import com.inet.sass.tree.BlockNode;
import com.inet.sass.tree.RuleNode;
import com.inet.sass.tree.VariableNode;

public class Variables extends AbstractTestBase {

    String scss = "/scss/variables.scss";
    String css  = "/css/variables.css";

    @Test
    public void testParser() throws IOException, URISyntaxException {
        ScssStylesheet root = getStyleSheet( scss );
        Assert.assertEquals( 8, root.getChildren().size() );

        VariableNode varNode1 = (VariableNode)root.getChildren().get( 0 );
        Assert.assertEquals( "blue", varNode1.getName() );

        VariableNode varNode3 = (VariableNode)root.getChildren().get( 2 );
        Assert.assertEquals( "chameleon-font-family", varNode3.getName() );

        VariableNode varNode2 = (VariableNode)root.getChildren().get( 1 );
        Assert.assertEquals( "margin", varNode2.getName() );
        Assert.assertEquals( 8f, varNode2.getExpr().getContainedValue().getDoubleValue(), 0 );
        Assert.assertEquals( "px", varNode2.getExpr().getContainedValue().getDimensionUnitText() );

        BlockNode blockNode1 = (BlockNode)root.getChildren().get( 6 );
        Assert.assertEquals( 3, blockNode1.getChildren().size() );
        RuleNode ruleNode1Block1 = (RuleNode)blockNode1.getChildren().get( 2 );
        Assert.assertEquals( "border-color", ruleNode1Block1.getVariable().toString() );
        Assert.assertEquals( SCSSLexicalUnit.SCSS_VARIABLE, ruleNode1Block1.getValue().getContainedValue().getItemType() );
        Assert.assertEquals( "blue", ruleNode1Block1.getValue().getContainedValue().getStringValue() );

        RuleNode ruleNode2Block1 = (RuleNode)blockNode1.getChildren().get( 2 );
        Assert.assertEquals( "border-color", ruleNode2Block1.getVariable().toString() );
        Assert.assertEquals( SCSSLexicalUnit.SCSS_VARIABLE, ruleNode2Block1.getValue().getContainedValue().getItemType() );
        Assert.assertEquals( "blue", ruleNode2Block1.getValue().getContainedValue().getStringValue() );

        BlockNode blockNode2 = (BlockNode)root.getChildren().get( 6 );
        RuleNode ruleNode1Block2 = (RuleNode)blockNode2.getChildren().get( 0 );
        Assert.assertEquals( "padding", ruleNode1Block2.getVariable().toString() );
        Assert.assertEquals( SCSSLexicalUnit.SCSS_VARIABLE, ruleNode1Block2.getValue().getContainedValue().getItemType() );
        Assert.assertEquals( "margin", ruleNode1Block2.getValue().getContainedValue().getStringValue() );

        RuleNode ruleNode2Block2 = (RuleNode)blockNode2.getChildren().get( 1 );
        Assert.assertEquals( "margin", ruleNode2Block2.getVariable().toString() );
        Assert.assertEquals( SCSSLexicalUnit.SCSS_VARIABLE, ruleNode2Block2.getValue().getContainedValue().getItemType() );
        Assert.assertEquals( "margin", ruleNode2Block2.getValue().getContainedValue().getStringValue() );

        RuleNode ruleNode3Block2 = (RuleNode)blockNode2.getChildren().get( 2 );
        Assert.assertEquals( "border-color", ruleNode3Block2.getVariable().toString() );
        Assert.assertEquals( SCSSLexicalUnit.SCSS_VARIABLE, ruleNode1Block2.getValue().getContainedValue().getItemType() );
        Assert.assertEquals( "blue", ruleNode3Block2.getValue().getContainedValue().getStringValue() );

        BlockNode blockNode3 = (BlockNode)root.getChildren().get( 7 );
        RuleNode ruleNode1Block3 = (RuleNode)blockNode3.getChildren().get( 0 );
        Assert.assertEquals( "background-image", ruleNode1Block3.getVariable().toString() );
        Assert.assertEquals( SCSSLexicalUnit.SAC_FUNCTION, ruleNode1Block3.getValue().getContainedValue().getItemType() );
        Assert.assertEquals( "url", ((LexicalUnitImpl)ruleNode1Block3.getValue()).getFunctionName() );
    }

    @Test
    public void testCompiler() throws Exception {
        testCompiler( scss, css );
    }
}
