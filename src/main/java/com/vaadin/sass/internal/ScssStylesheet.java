/*
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

package com.vaadin.sass.internal;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.InputSource;

import com.vaadin.sass.internal.handler.SCSSDocumentHandler;
import com.vaadin.sass.internal.handler.SCSSDocumentHandlerImpl;
import com.vaadin.sass.internal.handler.SCSSErrorHandler;
import com.vaadin.sass.internal.parser.ParseException;
import com.vaadin.sass.internal.parser.Parser;
import com.vaadin.sass.internal.parser.SCSSParseException;
import com.vaadin.sass.internal.resolver.FilesystemResolver;
import com.vaadin.sass.internal.resolver.ScssStylesheetResolver;
import com.vaadin.sass.internal.tree.Node;
import com.vaadin.sass.internal.visitor.ExtendNodeHandler;

public class ScssStylesheet extends Node {

    private static final long serialVersionUID = 3849790204404961608L;

    private String uri;

    private String charset;

    private ScssStylesheetResolver resolver;

    // relative path to use when importing files etc.
    private String prefix = "";

    private List<String> sourceUris = new ArrayList<String>();

    /**
     * Read in a file SCSS and parse it into a ScssStylesheet
     * 
     * @param file
     * @throws IOException
     */
    public ScssStylesheet() {
        super();
    }

    /**
     * Main entry point for the SASS compiler. Takes in a file and builds up a
     * ScssStylesheet tree out of it. Calling compile() on it will transform
     * SASS into CSS. Calling printState() will print out the SCSS/CSS.
     * 
     * @param identifier
     *            The file path. If null then null is returned.
     * @return
     * @throws CSSException
     * @throws IOException
     */
    public static ScssStylesheet get(String identifier) throws CSSException,
            IOException {
        return get(identifier, null);
    }

    /**
     * Main entry point for the SASS compiler. Takes in a file and an optional
     * parent style sheet, then builds up a ScssStylesheet tree out of it.
     * Calling compile() on it will transform SASS into CSS. Calling
     * printState() will print out the SCSS/CSS.
     * 
     * @param identifier
     *            The file path. If null then null is returned.
     * @param parentStylesheet
     *            Style sheet from which to inherit resolvers and encoding. May
     *            be null.
     * @return
     * @throws CSSException
     * @throws IOException
     */
    public static ScssStylesheet get(String identifier,
            ScssStylesheet parentStylesheet) throws CSSException, IOException {
        return get(identifier, parentStylesheet, new SCSSDocumentHandlerImpl(),
                new SCSSErrorHandler());
    }

    /**
     * Main entry point for the SASS compiler. Takes in a file, an optional
     * parent stylesheet, and document and error handlers. Then builds up a
     * ScssStylesheet tree out of it. Calling compile() on it will transform
     * SASS into CSS. Calling printState() will print out the SCSS/CSS.
     * 
     * @param identifier
     *            The file path. If null then null is returned.
     * @param parentStylesheet
     *            Style sheet from which to inherit resolvers and encoding. May
     *            be null.
     * @param documentHandler
     *            Instance of document handler. May not be null.
     * @param errorHandler
     *            Instance of error handler. May not be null.
     * @return
     * @throws CSSException
     * @throws IOException
     */
    public static ScssStylesheet get(String identifier,
            ScssStylesheet parentStylesheet,
            SCSSDocumentHandler documentHandler, SCSSErrorHandler errorHandler)
            throws CSSException, IOException {
        ScssStylesheetResolver resolver = parentStylesheet != null ? parentStylesheet.resolver : null;
        return get( identifier, parentStylesheet, documentHandler, errorHandler, resolver );
    }

    /**
     * Main entry point for the SASS compiler. Takes in a file, an optional
     * parent stylesheet, and document and error handlers. Then builds up a
     * ScssStylesheet tree out of it. Calling compile() on it will transform
     * SASS into CSS. Calling printState() will print out the SCSS/CSS.
     * 
     * @param identifier
     *            The file path. If null then null is returned.
     * @param parentStylesheet
     *            Style sheet from which to inherit resolvers and encoding. May
     *            be null.
     * @param documentHandler
     *            Instance of document handler. May not be null.
     * @param errorHandler
     *            Instance of error handler. May not be null.
     * @param resolver the used resolver
     * @return
     * @throws CSSException
     * @throws IOException
     */
    public static ScssStylesheet get(String identifier,
            ScssStylesheet parentStylesheet,
            SCSSDocumentHandler documentHandler, SCSSErrorHandler errorHandler, ScssStylesheetResolver resolver )
            throws CSSException, IOException {
        SCSSErrorHandler.set(errorHandler);
        /*
         * The encoding to be used is passed through "encoding" parameter. the
         * imported children scss node will have the same encoding as their
         * parent, ultimately the root scss file. The root scss node has this
         * "encoding" parameter to be null. Its encoding is determined by the
         * 
         * @charset declaration, the default one is ASCII.
         */

        if (identifier == null) {
            return null;
        }

        ScssStylesheet stylesheet = documentHandler.getStyleSheet();
        if (resolver == null) {
            // Use default resolvers
            stylesheet.resolver = new FilesystemResolver();
        } else {
            // Use parent resolvers
            stylesheet.resolver = resolver;
        }
        InputSource source = stylesheet.resolveStylesheet(identifier,
                parentStylesheet);
        if (source == null) {
            return null;
        }
        if (parentStylesheet != null) {
            source.setEncoding(parentStylesheet.getCharset());
        }
        Parser parser = new Parser();
        parser.setErrorHandler(errorHandler);
        parser.setDocumentHandler(documentHandler);

        try {
            parser.parseStyleSheet(source);
        } catch (ParseException e) {
            // catch ParseException, re-throw a SCSSParseException which has
            // file name info.
            throw new SCSSParseException(e, identifier);
        }

        stylesheet.setCharset(parser.getInputSource().getEncoding());
        stylesheet.sourceUris.add(source.getURI());

        return stylesheet;
    }

    public InputSource resolveStylesheet(String identifier,
            ScssStylesheet parentStylesheet) {
        if( resolver != null ) {
            InputSource source = resolver.resolve(parentStylesheet, identifier);
            if (source != null) {
                uri = source.getURI();
                return source;
            }
        }

        return null;
    }

    public List<String> getSourceUris() {
        return Collections.unmodifiableList(sourceUris);
    }

    public void addSourceUris(Collection<String> uris) {
        sourceUris.addAll(uris);
    }

    /**
     * Applies all the visitors and compiles SCSS into Css.
     * 
     * @throws Exception
     */
    public void compile() throws Exception {
        compile(ScssContext.UrlMode.MIXED);
    }

    /**
     * Applies all the visitors and compiles SCSS into Css.
     * 
     * @param urlMode
     *            Specifies whether urls appearing in an scss style sheet are
     *            taken to be absolute or relative. It is also possible to use
     *            old Vaadin style urls (ScssContext.UrlMode.MIXED), where urls
     *            are relative only when they appear in simple properties such
     *            as "background-image:url(image.png)".
     * 
     *            Absolute url mode means that urls appear in the output css in
     *            the same form as they are in the source scss files. Relative
     *            urls take into account the locations of imported scss files.
     *            For instance, if a style sheets imports foo/bar.scss and
     *            bar.scss contains url(baz.png), it will be output as
     *            url(foo/baz.png) in relative mode and as url(baz.png) in
     *            absolute mode.
     * 
     * @throws Exception
     */
    public void compile(ScssContext.UrlMode urlMode) throws Exception {
        ScssContext context = new ScssContext(urlMode);
        traverse(context);
        ExtendNodeHandler.modifyTree(context, this);
    }

    /**
     * Prints out the current state of the node tree. Will return SCSS before
     * compile and CSS after.
     * 
     * For now this is an own method with it's own implementation that most node
     * types will implement themselves.
     */
    @Override
    public String printState() {
        return buildString(PRINT_STRATEGY);
    }

    @Override
    public String toString() {
        return "Stylesheet node [" + buildString(TO_STRING_STRATEGY) + "]";
    }

    /**
     * Traverses a node and its children recursively, calling all the
     * appropriate handlers via {@link Node#traverse()}.
     * 
     * The node itself may be removed during the traversal and replaced with
     * other nodes at the same position or later on the child list of its
     * parent.
     */
    @Override
    public Collection<Node> traverse(ScssContext context) {
        traverseChildren(context);
        return Collections.singleton((Node) this);
    }

    /**
     * Returns the directory containing this style sheet
     * 
     * @return The directory containing this style sheet
     */
    public String getDirectory() {
        int idx = uri.lastIndexOf( '/' );
        return idx < 0 ? "" : uri.substring( 0, idx );
    }

    /**
     * Returns the full file name for this style sheet
     * 
     * @return The full file name for this style sheet
     */
    public String getFileName() {
        return uri;
    }

    public static final void warning(String msg) {
        Logger.getLogger(ScssStylesheet.class.getName()).warning(msg);
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    private String buildString(BuildStringStrategy strategy) {
        StringBuilder string = new StringBuilder("");
        String delimeter = "\n\n";
        // add charset declaration, if it is not default "ASCII".
        if (!"ASCII".equals(getCharset())) {
            string.append("@charset \"").append(getCharset()).append("\";")
                    .append(delimeter);
        }
        List<Node> children = getChildren();
        if (children.size() > 0) {
            string.append(strategy.build(children.get(0)));
        }
        if (children.size() > 1) {
            for (int i = 1; i < children.size(); i++) {
                String childString = strategy.build(children.get(i));
                if (childString != null) {
                    string.append(delimeter).append(childString);
                }
            }
        }
        String output = string.toString();
        return output;
    }

    @Override
    public Node copy() {
        throw new UnsupportedOperationException(
                "ScssStylesheet cannot be copied");
    }

    public void write(Writer writer) throws IOException {
        writer.write(printState());
    }
}
