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

import org.w3c.css.sac.SACMediaList;

import com.inet.sass.ScssContext;
import com.inet.sass.ScssStylesheet;
import com.inet.sass.util.StringUtil;
import com.inet.sass.visitor.ImportNodeHandler;

public class ImportNode extends Node implements NodeWithUrlContent {

    private String uri;
    private SACMediaList ml;
    private boolean isURL;

    // the stylesheet which contained this import node - usually empty as its
    // contents have been moved to its parent, but used to access resolvers,
    // charset etc.
    private ScssStylesheet styleSheet;

    public ImportNode(String uri, SACMediaList ml, boolean isURL) {
        super();
        this.uri = uri;
        this.ml = ml;
        this.isURL = isURL;
    }

    private ImportNode(ImportNode nodeToCopy) {
        super(nodeToCopy);
        uri = nodeToCopy.uri;
        ml = nodeToCopy.ml;
        isURL = nodeToCopy.isURL;
        styleSheet = nodeToCopy.styleSheet;
    }

    public boolean isPureCssImport() {
        return (isURL || uri.endsWith(".css") || uri.startsWith("http://") || hasMediaQueries());
    }

    private boolean hasMediaQueries() {
        return (ml != null && ml.getLength() >= 1 && !"all".equals(ml.item(0)));
    }

    @Override
    public String printState() {
        StringBuilder builder = new StringBuilder("@import ");
        if (isURL) {
            builder.append("url(").append(uri).append(")");
        } else {
            builder.append("\"").append(uri).append("\"");
        }
        if (hasMediaQueries()) {
            for (int i = 0; i < ml.getLength(); i++) {
                builder.append(" ").append(ml.item(i));
            }
        }
        builder.append(";");
        return builder.toString();
    }

    @Override
    public String toString() {
        return "Import node [" + printState() + "]";
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public SACMediaList getMl() {
        return ml;
    }

    @Override
    public Collection<Node> traverse(ScssContext context) {
        return ImportNodeHandler.traverse(context, this);
    }

    public void setStylesheet(ScssStylesheet styleSheet) {
        this.styleSheet = styleSheet;
    }

    public ScssStylesheet getStylesheet() {
        return styleSheet;
    }

    @Override
    public ImportNode copy() {
        return new ImportNode(this);
    }

    @Override
    public ImportNode updateUrl(String prefix) {
        if (isURL) {
            String newUri = getUri().replaceAll("^\"|\"$", "").replaceAll(
                    "^'|'$", "");
            if (!newUri.startsWith("/") && !newUri.contains(":")) {
                newUri = prefix + newUri;
                newUri = StringUtil.cleanPath(newUri);
            }
            ImportNode copy = copy();
            copy.setUri(newUri);
            return copy;
        }
        return this;
    }
}