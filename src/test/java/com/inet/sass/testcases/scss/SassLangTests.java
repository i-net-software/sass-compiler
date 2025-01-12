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
import java.net.URL;
import java.util.Collection;

import org.junit.runner.RunWith;

import com.inet.sass.ScssContext;
import com.inet.sass.function.AbstractFunctionGenerator;
import com.inet.sass.function.SCSSFunctionGenerator;
import com.inet.sass.parser.FormalArgumentList;
import com.inet.sass.parser.LexicalUnitImpl;
import com.inet.sass.parser.SassListItem;
import com.inet.sass.testcases.scss.SassTestRunner.TestFactory;

@RunWith(SassTestRunner.class)
public class SassLangTests extends AbstractDirectoryScanningSassTests {

    @Override
    protected URL getResourceURL(String path) {
        return getResourceURLInternal(path);
    }

    private static URL getResourceURLInternal(String path) {
        URL url = SassLangTests.class.getResource("/sasslang" + path);
        if (url == null) {
            throw new RuntimeException(
                    "Could not locate /sasslang using classloader");
        }
        return url;
    }

    @TestFactory
    public static Collection<String> getScssResourceNames()
            throws URISyntaxException, IOException {
        SCSSFunctionGenerator.registerCustomFunction( new FilenameFunction() );
        return getScssResourceNames(getResourceURLInternal(""));
    }

    private static class FilenameFunction extends AbstractFunctionGenerator {

        private FilenameFunction() {
            super( createArgumentList( new String[0], false ), "filename" );
        }

        @Override
        protected SassListItem computeForArgumentList( ScssContext context, LexicalUnitImpl function, FormalArgumentList actualArguments ) {
            String fileName = function.getUri();
            int idx = fileName.indexOf( "/sasslang/" );
            fileName = "/target/test-classes" + fileName.substring( idx );
            return LexicalUnitImpl.createIdent( function.getUri(), function.getLineNumber(), function.getColumnNumber(), fileName );
        }
    }
}
