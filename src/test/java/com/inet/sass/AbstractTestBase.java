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

package com.inet.sass;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;

import com.inet.sass.resolver.FilesystemResolver;
import com.inet.sass.resolver.ScssStylesheetResolver;
import com.inet.sass.testcases.scss.AssertErrorHandler;

public abstract class AbstractTestBase {

    public static final String CR = "\r";

    protected ScssStylesheet stylesheet;
    protected String originalScss;
    protected String parsedScss;
    protected String comparisonCss;

    protected ScssStylesheetResolver getResolver( String filename ) {
        return new FilesystemResolver( StandardCharsets.UTF_8 );
    }

    public ScssStylesheet getStyleSheet(String filename)
            throws URISyntaxException, IOException {
        File file = getFile(filename);
        stylesheet = ScssStylesheet.get( file.getAbsolutePath(), new AssertErrorHandler(), getResolver( filename ) );
        return stylesheet;
    }

    public File getFile(String filename) throws URISyntaxException, IOException {
        return new File(getClass().getResource(filename).toURI());
    }

    public String getFileContent(String filename) throws IOException, URISyntaxException {
        File file = getFile(filename);
        return getFileContent(file);
    }

    /**
     * Read in the full content of a file into a string.
     * 
     * @param file
     *            the file to be read
     * @return a String with the content of the
     * @throws IOException
     *             when file reading fails
     */
    public String getFileContent(File file) throws IOException {
        return IOUtils.toString( new FileInputStream( file ), "UTF-8" );
    }

    public ScssStylesheet testParser(String file) throws IOException, URISyntaxException {
        originalScss = getFileContent(file);
        originalScss = originalScss.replaceAll(CR, "");
        ScssStylesheet sheet = getStyleSheet(file);
        parsedScss = sheet.printState();
        parsedScss = parsedScss.replace(CR, "");
        Assert.assertEquals("Original CSS and parsed CSS do not match",
                originalScss, parsedScss);
        return sheet;
    }

    public ScssStylesheet testCompiler(String scss, String css)
            throws Exception {
        comparisonCss = getFileContent(css);
        comparisonCss = normalize(comparisonCss);
        ScssStylesheet sheet = getStyleSheet(scss);
        sheet.compile();
        parsedScss = sheet.printState();
        parsedScss = normalize(parsedScss);
        Assert.assertEquals("Original CSS and parsed CSS do not match",
                comparisonCss, parsedScss);
        return sheet;
    }

    static String normalize( String css) {
        css = css.replaceAll(CR, "");
        // add newline,tab before comments after semicolon
        css = css.replaceAll(";\\/\\*", ";\n\t/*");
        return css;
    }
}
