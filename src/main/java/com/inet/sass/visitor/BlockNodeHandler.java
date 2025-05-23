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

package com.inet.sass.visitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.inet.sass.ScssContext;
import com.inet.sass.ScssStylesheet;
import com.inet.sass.selector.Selector;
import com.inet.sass.tree.BlockNode;
import com.inet.sass.tree.MediaNode;
import com.inet.sass.tree.NestPropertiesNode;
import com.inet.sass.tree.Node;

/**
 * Handle nesting of blocks by moving child blocks to their parent, updating
 * their selector lists while doing so. Also parent selectors (&amp;) are
 * handled here.
 * 
 * Sample SASS code (from www.sass-lang.com):
 * 
 * <pre>
 * table.hl {
 *   margin: 2em 0;
 *   td.ln {
 *     text-align: right;
 *   }
 * }
 * </pre>
 * 
 * Note that nested properties are handled by {@link NestPropertiesNode}, not
 * here.
 */
public class BlockNodeHandler {

    public static Collection<Node> traverse( ScssContext context, BlockNode node ) {

        if( node.getChildren().size() == 0 ) {
            return Collections.emptyList();
        }

        ArrayList<Node> result = new ArrayList<Node>();
        updateSelectors( node );

        List<Node> children = node.getChildren();
        if( !children.isEmpty() ) {
            context.openVariableScope();
            BlockNode oldParent = context.getParentBlock();
            context.setParentBlock( node );
            try {
                ArrayList<Node> newChildren = null;
                for( Node child : children ) {
                    if( child.getClass() == BlockNode.class ) {
                        ((BlockNode)child).setParentSelectors( node.getSelectorList() );
                        result.addAll( child.traverse( context ) );
                    } else if( child.getClass() == MediaNode.class ) {
                        bubbleMedia( result, context, node, (MediaNode)child, true );
                    } else {
                        Collection<Node> childTraversed = child.traverse( context );
                        for( Node n : childTraversed ) {
                            if( n.getClass() == BlockNode.class ) {
                                // already traversed
                                result.add( n );
                            } else if( n.getClass() == MediaNode.class ) {
                                bubbleMedia( result, context, node, (MediaNode)n, false );
                            } else {
                                if( newChildren == null ) {
                                    newChildren = new ArrayList<Node>();
                                }
                                newChildren.add( n );
                            }
                        }
                    }
                }
                // add the node with the remaining non-block children at the
                // beginning
                if( newChildren != null ) {
                    BlockNode newNode = new BlockNode( node, newChildren );
                    newNode.setParentSelectors( node.getParentSelectors() );
                    result.add( 0, newNode );
                }
            } finally {
                context.closeVariableScope();
                context.setParentBlock( oldParent );
            }
        }

        return result;
    }

    /**
     * Reorder the @media rule on top
     * @param result container for the traversed media
     * @param context current context
     * @param node the parent node of the MediaNode
     * @param child the media node which is child
     * @param traverse true, child must traverse; false, child was already traverse
     */
    static void bubbleMedia( ArrayList<Node> result, ScssContext context, BlockNode node, MediaNode child, boolean traverse ) {
        for( Selector selector : node.getSelectorList() ) {
            if( selector.isPlaceholder() ) {
                //TODO placeholder selectors must handle other
                return;
            }
        }
        TRAVERSE: if( !traverse ) {
            List<Node> children = child.getChildren();
            for( Node c : children ) {
                if( c.getClass() != BlockNode.class ) {
                    break TRAVERSE;
                }
            }
            // the media has only sub blocks which was already traverse
            result.add( child );
            return;
        }
        MediaNode media = new MediaNode( child.getUri(), child.getLineNumber(), child.getColumnNumber(), child.getMedia() );
        media.appendChild( new BlockNode( node, child.getChildren() ) );
        result.addAll( media.traverse( context ) );
    }

    private static void updateSelectors( BlockNode node ) {
        Node parentBlock = node.getNormalParentNode();
        if( parentBlock instanceof BlockNode ) {
            replaceParentSelectors( (BlockNode)parentBlock, node );

        } else if( node.getSelectors().contains( "&" ) ) {
            ScssStylesheet.warning("Base-level rule contains"
                    + " the parent-selector-referencing character '&';"
                    + " the character will be removed:\n" + node);
            removeParentReference(node);
        }
    }

    /**
     * Goes through the selector list of the given BlockNode and removes the '&'
     * character from the selectors.
     * 
     * @param node
     */
    private static void removeParentReference(BlockNode node) {
        ArrayList<Selector> newSelectors = new ArrayList<Selector>();

        for( Selector sel : node.getSelectorList() ) {
            newSelectors.add( sel.replaceParentReference( null ) );
        }

        node.setSelectorList(newSelectors);
    }

    private static void replaceParentSelectors( BlockNode parentBlock, BlockNode node ) {
        ArrayList<Selector> newSelectors = new ArrayList<Selector>();

        for (Selector parentSel : parentBlock.getSelectorList()) {
            for( Selector sel : node.getSelectorList() ) {
                newSelectors.add( sel.replaceParentReference( parentSel ) );
            }
        }

        node.setSelectorList(newSelectors);

    }
}
