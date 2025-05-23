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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.inet.sass.ScssContext;
import com.inet.sass.parser.MediaList;

public class MediaNode extends Node {

    MediaList media;

    public MediaNode( String uri, int line, int column, MediaList media ) {
        super( uri, line, column );
        this.media = media;
    }

    private MediaNode(MediaNode nodeToCopy) {
        super(nodeToCopy);
        media = nodeToCopy.media;
    }

    public MediaList getMedia() {
        return media;
    }

    public void setMedia( MediaList media ) {
        this.media = media;
    }

    @Override
    public String printState() {
        return buildString(PRINT_STRATEGY, true);
    }

    @Override
    public String toString() {
        return buildString(TO_STRING_STRATEGY, true);
    }

    @Override
    public Collection<Node> traverse(ScssContext context) {
        if( media != null ) {
            media = media.replaceVariables( context );
        }

        if( getNormalParentNode() instanceof BlockNode ) {
            // we are inside a call of BlockNodeHandler.traverse(). The caller must reorder first bubbleMedia() before we can continue with traverse.
            return Collections.singletonList( this );
        }
        Collection<Node> children = traverseChildren(context);
        ArrayList<Node> result = new ArrayList<>();
        result.add( this );

        for( Iterator<Node> it = children.iterator(); it.hasNext(); ) {
            Node child = it.next();
            if( child.getClass() == MediaNode.class ) {
                MediaNode mediaChild = (MediaNode)child;
                MediaList medium = new MediaList();
                mediaChild.media.replaceVariables( context );
                medium.addItem( media + " and " + mediaChild.media );
                mediaChild.setMedia( medium );
                result.add( child );
                it.remove();
            }
        }

        if( result.size() > 1 ) {
            if( children.size() == 0 ) {
                // seems there was only inner MediaNodes
                result.remove( 0 );
            } else {
                setChildren( children );
            }
        }
        return result;
    }

    private String buildString(BuildStringStrategy strategy, boolean indent) {
        StringBuilder builder = new StringBuilder("@media ");
        if (media != null) {
            for (int i = 0; i < media.getLength(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append(media.item(i));
            }
        }
        builder.append(" {\n");
        for (Node child : getChildren()) {
            builder.append('\t');
            if (child instanceof BlockNode) {
                if (PRINT_STRATEGY.equals(strategy)) {
                    builder.append(((BlockNode) child).buildString(indent));
                } else {
                    builder.append(strategy.build(child));

                }
            } else {
                builder.append(strategy.build(child));
            }
            builder.append('\n');
        }
        builder.append("}");
        return builder.toString();
    }

    @Override
    public MediaNode copy() {
        return new MediaNode(this);
    }
}
