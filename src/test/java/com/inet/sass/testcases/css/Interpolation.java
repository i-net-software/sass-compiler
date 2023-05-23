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

package com.inet.sass.testcases.css;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.css.sac.CSSException;

import com.inet.sass.AbstractTestBase;
import com.inet.sass.ScssStylesheet;
import com.inet.sass.handler.SCSSDocumentHandler;
import com.inet.sass.parser.Parser;
import com.inet.sass.tree.BlockNode;

public class Interpolation extends AbstractTestBase {
    String scss = "/scss/interpolation.scss";

    @Test
    public void testParser() throws CSSException, URISyntaxException,
            IOException {
        Parser parser = new Parser();
        SCSSDocumentHandler handler = new SCSSDocumentHandler();
        parser.setDocumentHandler(handler);
        parser.parseStyleSheet(getClass().getResource(scss).getPath());
        ScssStylesheet root = handler.getStyleSheet();

        Assert.assertEquals(3, root.getChildren().size());
        BlockNode blockNodeWithInterpolation = (BlockNode) root.getChildren()
                .get(2);
    }
}