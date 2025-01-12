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
import java.util.Iterator;

import com.inet.sass.ScssContext;
import com.inet.sass.parser.ParseException;
import com.inet.sass.selector.Selector;
import com.inet.sass.selector.SelectorSet;
import com.inet.sass.selector.SimpleSelectorSequence;
import com.inet.sass.tree.BlockNode;
import com.inet.sass.tree.ExtendNode;
import com.inet.sass.tree.MediaNode;
import com.inet.sass.tree.Node;

public class ExtendNodeHandler {
    /*
     * TODOs:
     * 
     * - Print a warning when unification of an @extend-clause fails, unless
     * !optional specified. This may require some rework of the way the extends
     * map is built, as currently the connection to the @extend declaration is
     * lost.
     */

    public static Collection<Node> traverse( ScssContext context, ExtendNode node ) throws Exception {
        for( Selector s : node.getList() ) {
            if( !s.isSimple() ) {
                // @extend-selectors must not be nested
                throw new ParseException( "Nested selector not allowed in @extend-clause" );
            }
            Node parentNode = node.getNormalParentNode();
            if( parentNode instanceof BlockNode ) {
                BlockNode parentBlock = (BlockNode)parentNode;
                SimpleSelectorSequence extendSelector = s.firstSimple();
                for( Selector sel : parentBlock.getSelectorList() ) {
                    Collection<Selector> ctx = parentBlock.getParentSelectors();
                    context.addExtension( new Extension( extendSelector, sel, ctx ) );
                }
            }
        }
        return Collections.emptyList();
    }

    public static void modifyTree(ScssContext context, Node node) {
        Iterable<Extension> extensions = context.getExtensions();

        for( Iterator<Node> nodeIt = node.getChildren().iterator(); nodeIt.hasNext(); ) {
            Node child = nodeIt.next();

            Class<?> clazz = child.getClass();
            if( clazz == BlockNode.class ) {
                BlockNode blockNode = (BlockNode)child;
                // need a copy as the selector list is modified below
                SelectorSet newSelectors = new SelectorSet();
                for( Selector selector : blockNode.getSelectorList() ) {
                    // keep order while avoiding duplicates
                    newSelectors.add( selector );
                    newSelectors.addAll( createSelectorsForExtensions( selector, extensions ) );
                }

                // remove all placeholder selectors
                Iterator<Selector> it = newSelectors.iterator();
                while( it.hasNext() ) {
                    Selector s = it.next();
                    if( s.isPlaceholder() ) {
                        it.remove();
                    }
                }

                // remove block if selector list is empty
                if( newSelectors.isEmpty() ) {
                    nodeIt.remove();
                } else {
                    blockNode.setSelectorList( new ArrayList<Selector>( newSelectors ) );
                }
            } else if( clazz == MediaNode.class ) {
                modifyTree( context, child );
            }
        }

    }

    /**
     * Try to unify argument selector with each selector specified in an
     * extend-clause. For each match found, add the enclosing block's selectors
     * with substitutions (see examples below). Finally eliminates redundant
     * selectors (a selector is redundant in a set if subsumed by another
     * selector in the set).
     * 
     * .a {...}; .b { @extend .a } ---&gt; .b
     * <p>
     * .a.b {...}; .c { @extend .a } ---&gt; .b.c
     * <p>
     * .a.b {...}; .c .c { @extend .a } ---&gt; .c .b.c 
     * <p>
     * @param target
     *            the selector to match
     * @param extendsMap
     *            mapping from the simple selector sequence of the
     *            extend-selector to an extending selector
     * 
     * @return the generated selectors (may contain duplicates)
     */
    public static SelectorSet createSelectorsForExtensions(Selector target,
            Iterable<Extension> extendsMap) {
        SelectorSet newSelectors = new SelectorSet();
        createSelectorsForExtensionsRecursively(target, newSelectors,
                extendsMap);
        return newSelectors.eliminateRedundantSelectors();
    }

    /**
     * Create all selector extensions matching target. Mutable collection for
     * efficiency. Recursively applied to generated selectors.
     */
    private static void createSelectorsForExtensionsRecursively(
            Selector target, SelectorSet current, Iterable<Extension> extendsMap) {

        SelectorSet newSelectors = new SelectorSet();

        for (Extension extension : extendsMap) {
            Selector replaced = target.replace(extension);
            boolean newSelector = current.add(replaced);
            if (newSelector && !replaced.equals(target)) {
                newSelectors.add(replaced);
            }
        }

        for (Selector newSelector : newSelectors) {
            createSelectorsForExtensionsRecursively(newSelector, current,
                    extendsMap);
        }

    }
}
