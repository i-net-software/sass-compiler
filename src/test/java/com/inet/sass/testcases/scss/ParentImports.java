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
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.inet.sass.AbstractTestBase;
import com.inet.sass.ScssStylesheet;
import com.inet.sass.tree.ImportNode;

public class ParentImports extends AbstractTestBase {

    String scss = "/scss/folder-test/parent-import.scss";
    String css  = "/css/parent-import.css";

    @Test
    public void testParser() throws IOException, URISyntaxException {
        ScssStylesheet root = getStyleSheet( scss );
        ImportNode importVariableNode = (ImportNode)root.getChildren().get( 0 );
        Assert.assertEquals( "../folder-test2/variables.scss", importVariableNode.getUri() );
        Assert.assertFalse( importVariableNode.isPureCssImport() );

        ImportNode importURLNode = (ImportNode)root.getChildren().get( 1 );
        Assert.assertEquals( "../folder-test2/url", importURLNode.getUri() );
        Assert.assertFalse( importURLNode.isPureCssImport() );

        ImportNode importImportNode = (ImportNode)root.getChildren().get( 2 );
        Assert.assertEquals( "../folder-test2/base-imported.scss", importImportNode.getUri() );
        Assert.assertFalse( importImportNode.isPureCssImport() );
    }

    @Test
    public void testCompiler() throws Exception {
        ScssStylesheet sheet = testCompiler( scss, css );
        List<String> importedSheets = sheet.getSourceUris();
        Assert.assertEquals( 5, importedSheets.size() );
        Assert.assertTrue( importedSheets.get( 0 ).replace( '\\', '/' ).endsWith( "/scss/folder-test/parent-import.scss" ) );
        Assert.assertTrue( importedSheets.get( 1 ).replace( '\\', '/' ).endsWith( "/scss/folder-test2/variables.scss" ) );
        Assert.assertTrue( importedSheets.get( 2 ).replace( '\\', '/' ).endsWith( "/scss/folder-test2/url.scss" ) );
        Assert.assertTrue( importedSheets.get( 3 ).replace( '\\', '/' ).endsWith( "/scss/folder-test2/base-imported.scss" ) );
        Assert.assertTrue( importedSheets.get( 4 ).replace( '\\', '/' ).endsWith( "/scss/folder-test2/base.scss" ) );
    }
}
