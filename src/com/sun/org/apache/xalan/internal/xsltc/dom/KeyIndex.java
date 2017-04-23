/*
 * Copyright (c) 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/*
 * Copyright 2001-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id: KeyIndex.java,v 1.6 2006/06/19 19:49:02 spericas Exp $
 */

package com.sun.org.apache.xalan.internal.xsltc.dom;

import java.util.StringTokenizer;

import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.DOMEnhancedForDTM;
import com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary;
import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;
import com.sun.org.apache.xalan.internal.xsltc.util.IntegerArray;

import com.sun.org.apache.xml.internal.dtm.Axis;
import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase;

/**
 * Stores mappings of key values or IDs to DTM nodes.
 * <em>Use of an instance of this class as a {@link DTMAxisIterator} is
 * <b>deprecated.</b></em>
 * @author Morten Jorgensen
 * @author Santiago Pericas-Geertsen
 */
public class KeyIndex extends DTMAxisIteratorBase {

    /**
     * A mapping between values and nodesets for the current document.  Used
     * only while building keys.
     */
    private Hashtable _index;

    /**
     * The document node currently being processed.  Used only while building
     * keys.
     */
    private int _currentDocumentNode = DTM.NULL;

    /**
     * A mapping from a document node to the mapping between values and nodesets
     */
    private Hashtable _rootToIndexMap = new Hashtable();

    /**
     * The node set associated to the current value passed
     * to lookupKey();
     */
    private IntegerArray _nodes = null;

    /**
     * The XSLTC DOM object if this KeyIndex is being used to implement the
     * id() function.
     */
    private DOM        _dom;

    private DOMEnhancedForDTM    _enhancedDOM;

    /**
     * Store position after call to setMark()
     */
    private int _markedPosition = 0;

    public KeyIndex(int dummy) {
    }

    public void setRestartable(boolean flag) {
    }

    /**
     * Adds a node to the node list for a given value. Nodes will
     * always be added in document order.
     */
    public void add(Object value, int node, int rootNode) {
        if (_currentDocumentNode != rootNode) {
            _currentDocumentNode = rootNode;
            _index = new Hashtable();
            _rootToIndexMap.put(new Integer(rootNode), _index);
        }

        IntegerArray nodes = (IntegerArray) _index.get(value);

        if (nodes == null) {
             nodes = new IntegerArray();
            _index.put(value, nodes);
            nodes.add(node);

        // Because nodes are added in document order,
        // duplicates can be eliminated easily at this stage.
        } else if (node != nodes.at(nodes.cardinality() - 1)) {
            nodes.add(node);
        }
    }

    /**
     * Merge the current value's nodeset set by lookupKey() with _nodes.
     * @deprecated
     */
    public void merge(KeyIndex other) {
        if (other == null) return;

        if (other._nodes != null) {
            if (_nodes == null) {
                _nodes = (IntegerArray)other._nodes.clone();
            }
            else {
                _nodes.merge(other._nodes);
            }
        }
    }

    /**
     * This method must be called by the code generated by the id() function
     * prior to returning the node iterator. The lookup code for key() and
     * id() differ in the way the lookup value can be whitespace separated
     * list of tokens for the id() function, but a single string for the
     * key() function.
     * @deprecated
     */
    public void lookupId(Object value) {
        // Clear _nodes array
        _nodes = null;

        final StringTokenizer values = new StringTokenizer((String) value,
                                                           " \n\t");
        while (values.hasMoreElements()) {
            final String token = (String) values.nextElement();
            IntegerArray nodes = (IntegerArray) _index.get(token);

            if (nodes == null && _enhancedDOM != null
                && _enhancedDOM.hasDOMSource()) {
                nodes = getDOMNodeById(token);
            }

            if (nodes == null) continue;

            if (_nodes == null) {
                 nodes = (IntegerArray)nodes.clone();
                _nodes = nodes;
            }
            else {
                _nodes.merge(nodes);
            }
        }
    }

    /**
     * Return an IntegerArray for the DOM Node which has the given id.
     *
     * @param id The id
     * @return A IntegerArray representing the Node whose id is the given value.
     */
    public IntegerArray getDOMNodeById(String id) {
        IntegerArray nodes = null;

        if (_enhancedDOM != null) {
            int ident = _enhancedDOM.getElementById(id);

            if (ident != DTM.NULL) {
                Integer root = new Integer(_enhancedDOM.getDocument());
                Hashtable index = (Hashtable) _rootToIndexMap.get(root);

                if (index == null) {
                    index = new Hashtable();
                    _rootToIndexMap.put(root, index);
                } else {
                    nodes = (IntegerArray) index.get(id);
                }

                if (nodes == null) {
                    nodes = new IntegerArray();
                    index.put(id, nodes);
                }

                nodes.add(_enhancedDOM.getNodeHandle(ident));
            }
        }

        return nodes;
    }

    /**
     * <p>This method must be called by the code generated by the key() function
     * prior to returning the node iterator.</p>
     * <p><em>Use of an instance of this class as a {@link DTMAxisIterator} is
     * <b>deprecated.</b></em></p>
     * @deprecated
     */
    public void lookupKey(Object value) {
        IntegerArray nodes = (IntegerArray) _index.get(value);
        _nodes = (nodes != null) ? (IntegerArray) nodes.clone() : null;
        _position = 0;
    }

    /**
     * <p>Callers should not call next() after it returns END.</p>
     * <p><em>Use of an instance of this class as a {@link DTMAxisIterator} is
     * <b>deprecated.</b></em></p>
     * @deprecated
     */
    public int next() {
        if (_nodes == null) return DTMAxisIterator.END;

        return (_position < _nodes.cardinality()) ?
            _dom.getNodeHandle(_nodes.at(_position++)) : DTMAxisIterator.END;
    }

    /**
     * Given a context node and the argument to the XPath <code>id</code>
     * function, checks whether the context node is in the set of nodes that
     * results from that reference to the <code>id</code> function.  This is
     * used in the implementation of <code>id</code> patterns.
     *
     * @param node The context node
     * @param value The argument to the <code>id</code> function
     * @return <code>1</code> if the context node is in the set of nodes
     *         returned by the reference to the <code>id</code> function;
     *         <code>0</code>, otherwise
     */
    public int containsID(int node, Object value) {
        final String string = (String)value;
        int rootHandle = _dom.getAxisIterator(Axis.ROOT)
                                 .setStartNode(node).next();

        // Get the mapping table for the document containing the context node
        Hashtable index =
            (Hashtable) _rootToIndexMap.get(new Integer(rootHandle));

        // Split argument to id function into XML whitespace separated tokens
        final StringTokenizer values = new StringTokenizer(string, " \n\t");

        while (values.hasMoreElements()) {
            final String token = (String) values.nextElement();
            IntegerArray nodes = null;

            if (index != null) {
                nodes = (IntegerArray) index.get(token);
            }

            // If input was from W3C DOM, use DOM's getElementById to do
            // the look-up.
            if (nodes == null && _enhancedDOM != null
                && _enhancedDOM.hasDOMSource()) {
                nodes = getDOMNodeById(token);
            }

            // Did we find the context node in the set of nodes?
            if (nodes != null && nodes.indexOf(node) >= 0) {
                return 1;
            }
        }

        // Didn't find the context node in the set of nodes returned by id
        return 0;
    }

    /**
     * <p>Given a context node and the second argument to the XSLT
     * <code>key</code> function, checks whether the context node is in the
     * set of nodes that results from that reference to the <code>key</code>
     * function.  This is used in the implementation of key patterns.</p>
     * <p>This particular {@link KeyIndex} object is the result evaluating the
     * first argument to the <code>key</code> function, so it's not taken into
     * any further account.</p>
     *
     * @param node The context node
     * @param value The second argument to the <code>key</code> function
     * @return <code>1</code> if and only if the context node is in the set of
     *         nodes returned by the reference to the <code>key</code> function;
     *         <code>0</code>, otherwise
     */
    public int containsKey(int node, Object value) {
        int rootHandle = _dom.getAxisIterator(Axis.ROOT)
                                 .setStartNode(node).next();

        // Get the mapping table for the document containing the context node
        Hashtable index =
                    (Hashtable) _rootToIndexMap.get(new Integer(rootHandle));

        // Check whether the context node is present in the set of nodes
        // returned by the key function
        if (index != null) {
            final IntegerArray nodes = (IntegerArray) index.get(value);
            return (nodes != null && nodes.indexOf(node) >= 0) ? 1 : 0;
        }

        // The particular key name identifies no nodes in this document
        return 0;
    }

    /**
     * <p>Resets the iterator to the last start node.</p>
     * <p><em>Use of an instance of this class as a {@link DTMAxisIterator} is
     * <b>deprecated.</b></em></p>
     * @deprecated
     */
    public DTMAxisIterator reset() {
        _position = 0;
        return this;
    }

    /**
     * <p>Returns the number of elements in this iterator.</p>
     * <p><em>Use of an instance of this class as a {@link DTMAxisIterator} is
     * <b>deprecated.</b></em></p>
     * @deprecated
     */
    public int getLast() {
        return (_nodes == null) ? 0 : _nodes.cardinality();
    }

    /**
     * <p>Returns the position of the current node in the set.</p>
     * <p><em>Use of an instance of this class as a {@link DTMAxisIterator} is
     * <b>deprecated.</b></em></p>
     * @deprecated
     */
    public int getPosition() {
        return _position;
    }

    /**
     * <p>Remembers the current node for the next call to gotoMark().</p>
     * <p><em>Use of an instance of this class as a {@link DTMAxisIterator} is
     * <b>deprecated.</b></em></p>
     * @deprecated
     */
    public void setMark() {
        _markedPosition = _position;
    }

    /**
     * <p>Restores the current node remembered by setMark().</p>
     * <p><em>Use of an instance of this class as a {@link DTMAxisIterator} is
     * <b>deprecated.</b></em></p>
     * @deprecated
     */
    public void gotoMark() {
        _position = _markedPosition;
    }

    /**
     * <p>Set start to END should 'close' the iterator,
     * i.e. subsequent call to next() should return END.</p>
     * <p><em>Use of an instance of this class as a {@link DTMAxisIterator} is
     * <b>deprecated.</b></em></p>
     * @deprecated
     */
    public DTMAxisIterator setStartNode(int start) {
        if (start == DTMAxisIterator.END) {
            _nodes = null;
        }
        else if (_nodes != null) {
            _position = 0;
        }
        return (DTMAxisIterator) this;
    }

    /**
     * <p>Get start to END should 'close' the iterator,
     * i.e. subsequent call to next() should return END.</p>
     * <p><em>Use of an instance of this class as a {@link DTMAxisIterator} is
     * <b>deprecated.</b></em></p>
     * @deprecated
     */
    public int getStartNode() {
        return 0;
    }

    /**
     * <p>True if this iterator has a reversed axis.</p>
     * <p><em>Use of an instance of this class as a {@link DTMAxisIterator} is
     * <b>deprecated.</b></em></p>
     * @deprecated
     */
    public boolean isReverse() {
        return(false);
    }

    /**
     * <p>Returns a deep copy of this iterator.</p>
     * <p><em>Use of an instance of this class as a {@link DTMAxisIterator} is
     * <b>deprecated.</b></em></p>
     * @deprecated
     */
    public DTMAxisIterator cloneIterator() {
        KeyIndex other = new KeyIndex(0);
        other._index = _index;
        other._rootToIndexMap = _rootToIndexMap;
        other._nodes = _nodes;
        other._position = _position;
        return (DTMAxisIterator) other;
    }

    public void setDom(DOM dom, int node) {
        _dom = dom;

        // If a MultiDOM, ensure _enhancedDOM is correctly set
        // so that getElementById() works in lookupNodes below
        if (dom instanceof MultiDOM) {
            dom = ((MultiDOM) dom).getDTM(node);
        }

        if (dom instanceof DOMEnhancedForDTM) {
            _enhancedDOM = (DOMEnhancedForDTM)dom;
        }
        else if (dom instanceof DOMAdapter) {
            DOM idom = ((DOMAdapter)dom).getDOMImpl();
            if (idom instanceof DOMEnhancedForDTM) {
                _enhancedDOM = (DOMEnhancedForDTM)idom;
            }
        }
    }

    /**
     * Create a {@link KeyIndexIterator} that iterates over the nodes that
     * result from a reference to the XSLT <code>key</code> function or
     * XPath <code>id</code> function.
     *
     * @param keyValue A string or iterator representing the key values or id
     *                 references
     * @param isKeyCall A <code>boolean</code> indicating whether the iterator
     *                 is being created for a reference <code>key</code> or
     *                 <code>id</code>
     */
    public KeyIndexIterator getKeyIndexIterator(Object keyValue,
                                                boolean isKeyCall) {
        if (keyValue instanceof DTMAxisIterator) {
            return getKeyIndexIterator((DTMAxisIterator) keyValue, isKeyCall);
        } else {
            return getKeyIndexIterator(BasisLibrary.stringF(keyValue, _dom),
                                       isKeyCall);
        }
    }

    /**
     * Create a {@link KeyIndexIterator} that iterates over the nodes that
     * result from a reference to the XSLT <code>key</code> function or
     * XPath <code>id</code> function.
     *
     * @param keyValue A string representing the key values or id
     *                 references
     * @param isKeyCall A <code>boolean</code> indicating whether the iterator
     *                 is being created for a reference <code>key</code> or
     *                 <code>id</code>
     */
    public KeyIndexIterator getKeyIndexIterator(String keyValue,
                                                boolean isKeyCall) {
        return new KeyIndexIterator(keyValue, isKeyCall);
    }

    /**
     * Create a {@link KeyIndexIterator} that iterates over the nodes that
     * result from a reference to the XSLT <code>key</code> function or
     * XPath <code>id</code> function.
     *
     * @param keyValue An iterator representing the key values or id
     *                 references
     * @param isKeyCall A <code>boolean</code> indicating whether the iterator
     *                 is being created for a reference <code>key</code> or
     *                 <code>id</code>
     */
    public KeyIndexIterator getKeyIndexIterator(DTMAxisIterator keyValue,
                                                boolean isKeyCall) {
        return new KeyIndexIterator(keyValue, isKeyCall);
    }

    /**
     * Used to represent an empty node set.
     */
    final private static IntegerArray EMPTY_NODES = new IntegerArray(0);


    /**
     * An iterator representing the result of a reference to either the
     * XSLT <code>key</code> function or the XPath <code>id</code> function.
     */
    public class KeyIndexIterator extends MultiValuedNodeHeapIterator {

        /**
         * <p>A reference to the <code>key</code> function that only has one
         * key value or to the <code>id</code> function that has only one string
         * argument can be optimized to ignore the multi-valued heap.  This
         * field will be <code>null</code> otherwise.
         */
        private IntegerArray _nodes;

        /**
         * <p>This field contains the iterator representing a node set key value
         * argument to the <code>key</code> function or a node set argument
         * to the <code>id</code> function.</p>
         *
         * <p>Exactly one of this field and {@link #_keyValue} must be
         * <code>null</code>.</p>
         */
        private DTMAxisIterator _keyValueIterator;

        /**
         * <p>This field contains the iterator representing a non-node-set key
         * value argument to the <code>key</code> function or a non-node-set
         * argument to the <code>id</code> function.</p>
         *
         * <p>Exactly one of this field and {@link #_keyValueIterator} must be
         * <code>null</code>.</p>
         */
        private String _keyValue;

        /**
         * Indicates whether this object represents the result of a reference
         * to the <code>key</code> function (<code>true</code>) or the
         * <code>id</code> function (<code>false</code>).
         */
        private boolean _isKeyIterator;

        /**
         * Represents the DTM nodes retrieved for one key value or one string
         * argument to <code>id</code> for use as one heap node in a
         * {@link MultiValuedNodeHeapIterator}.
         */
        protected class KeyIndexHeapNode
                extends MultiValuedNodeHeapIterator.HeapNode
        {
            /**
             * {@link IntegerArray} of DTM nodes retrieved for one key value.
             * Must contain no duplicates and be stored in document order.
             */
            private IntegerArray _nodes;

            /**
             * Position in {@link #_nodes} array of next node to return from
             * this heap node.
             */
            private int _position = 0;

            /**
             * Marked position.  Used by {@link #setMark()} and
             * {@link #gotoMark()}
             */
            private int _markPosition = -1;

            /**
             * Create a heap node representing DTM nodes retrieved for one
             * key value in a reference to the <code>key</code> function
             * or string argument to the <code>id</code> function.
             */
            KeyIndexHeapNode(IntegerArray nodes) {
                _nodes = nodes;
            }

            /**
             * Advance to the next node represented by this {@link HeapNode}
             *
             * @return the next DTM node.
             */
            public int step() {
                if (_position < _nodes.cardinality()) {
                    _node = _nodes.at(_position);
                    _position++;
                } else {
                    _node = DTMAxisIterator.END;
                }

                return _node;
            }

            /**
             * Creates a deep copy of this {@link HeapNode}.  The clone is not
             * reset from the current position of the original.
             *
             * @return the cloned heap node
             */
            public HeapNode cloneHeapNode() {
                KeyIndexHeapNode clone =
                        (KeyIndexHeapNode) super.cloneHeapNode();

                clone._nodes = _nodes;
                clone._position = _position;
                clone._markPosition = _markPosition;

                return clone;
            }

            /**
             * Remembers the current node for the next call to
             * {@link #gotoMark()}.
             */
            public void setMark() {
                _markPosition = _position;
            }

            /**
             * Restores the current node remembered by {@link #setMark()}.
             */
            public void gotoMark() {
                _position = _markPosition;
            }

            /**
             * Performs a comparison of the two heap nodes
             *
             * @param heapNode the heap node against which to compare
             * @return <code>true</code> if and only if the current node for
             *         this heap node is before the current node of the
             *         argument heap node in document order.
             */
            public boolean isLessThan(HeapNode heapNode) {
                return _node < heapNode._node;
            }

            /**
             * <p>Sets context with respect to which this heap node is
             * evaluated.</p>
             * <p>This has no real effect on this kind of heap node.  Instead,
             * the {@link KeyIndexIterator#setStartNode(int)} method should
             * create new instances of this class to represent the effect of
             * changing the context.</p>
             */
            public HeapNode setStartNode(int node) {
                return this;
            }

            /**
             * Reset the heap node back to its beginning.
             */
            public HeapNode reset() {
                _position = 0;
                return this;
            }
        }

        /**
         * Constructor used when the argument to <code>key</code> or
         * <code>id</code> is not a node set.
         *
         * @param keyValue the argument to <code>key</code> or <code>id</code>
         *                 cast to a <code>String</code>
         * @param isKeyIterator indicates whether the constructed iterator
         *                represents a reference to <code>key</code> or
         *                <code>id</code>.
         */
        KeyIndexIterator(String keyValue, boolean isKeyIterator) {
            _isKeyIterator = isKeyIterator;
            _keyValue = keyValue;
        }

        /**
         * Constructor used when the argument to <code>key</code> or
         * <code>id</code> is a node set.
         *
         * @param keyValues the argument to <code>key</code> or <code>id</code>
         * @param isKeyIterator indicates whether the constructed iterator
         *                represents a reference to <code>key</code> or
         *                <code>id</code>.
         */
        KeyIndexIterator(DTMAxisIterator keyValues, boolean isKeyIterator) {
            _keyValueIterator = keyValues;
            _isKeyIterator = isKeyIterator;
        }

        /**
         * Retrieve nodes for a particular key value or a particular id
         * argument value.
         *
         * @param root The root node of the document containing the context node
         * @param keyValue The key value of id string argument value
         * @return an {@link IntegerArray} of the resulting nodes
         */
        protected IntegerArray lookupNodes(int root, String keyValue) {
            IntegerArray result = null;

            // Get mapping from key values/IDs to DTM nodes for this document
            Hashtable index = (Hashtable)_rootToIndexMap.get(new Integer(root));

            if (!_isKeyIterator) {
                // For id function, tokenize argument as whitespace separated
                // list of values and look up nodes identified by each ID.
                final StringTokenizer values =
                        new StringTokenizer(keyValue, " \n\t");

                while (values.hasMoreElements()) {
                    final String token = (String) values.nextElement();
                    IntegerArray nodes = null;

                    // Does the ID map to any node in the document?
                    if (index != null) {
                        nodes = (IntegerArray) index.get(token);
                    }

                    // If input was from W3C DOM, use DOM's getElementById to do
                    // the look-up.
                    if (nodes == null && _enhancedDOM != null
                            && _enhancedDOM.hasDOMSource()) {
                        nodes = getDOMNodeById(token);
                    }

                    // If we found any nodes, merge them into the cumulative
                    // result
                    if (nodes != null) {
                        if (result == null) {
                            result = (IntegerArray)nodes.clone();
                        } else {
                            result.merge(nodes);
                        }
                    }
                }
            } else if (index != null) {
                // For key function, map key value to nodes
                result = (IntegerArray) index.get(keyValue);
            }

            return result;
        }

        /**
         * Set context node for the iterator.  This will cause the iterator
         * to reset itself, reevaluate arguments to the function, look up
         * nodes in the input and reinitialize its internal heap.
         *
         * @param node the context node
         * @return A {@link DTMAxisIterator} set to the start of the iteration.
         */
        public DTMAxisIterator setStartNode(int node) {
            _startNode = node;

            // If the arugment to the function is a node set, set the
            // context node on it.
            if (_keyValueIterator != null) {
                _keyValueIterator = _keyValueIterator.setStartNode(node);
            }

            init();

            return super.setStartNode(node);
        }

        /**
         * Get the next node in the iteration.
         *
         * @return The next node handle in the iteration, or END.
         */
        public int next() {
            int nodeHandle;

            // If at most one key value or at most one string argument to id
            // resulted in nodes being returned, use the IntegerArray
            // stored at _nodes directly.  This relies on the fact that the
            // IntegerArray never includes duplicate nodes and is always stored
            // in document order.
            if (_nodes != null) {
                if (_position < _nodes.cardinality()) {
                    nodeHandle = returnNode(_nodes.at(_position));
                } else {
                    nodeHandle = DTMAxisIterator.END;
                }
            } else {
                nodeHandle = super.next();
            }

            return nodeHandle;
        }

        /**
         * Resets the iterator to the last start node.
         *
         * @return A DTMAxisIterator, which may or may not be the same as this
         *         iterator.
         */
        public DTMAxisIterator reset() {
            if (_nodes == null) {
                init();
            } else {
                super.reset();
            }

            return resetPosition();
        }

        /**
         * Evaluate the reference to the <code>key</code> or <code>id</code>
         * function with the context specified by {@link #setStartNode(int)}
         * and set up this iterator to iterate over the DTM nodes that are
         * to be returned.
         */
        protected void init() {
            super.init();
            _position = 0;

            // All nodes retrieved are in the same document
            int rootHandle = _dom.getAxisIterator(Axis.ROOT)
                                      .setStartNode(_startNode).next();

            // Is the argument not a node set?
            if (_keyValueIterator == null) {
                // Look up nodes returned for the single string argument
                _nodes = lookupNodes(rootHandle, _keyValue);

                if (_nodes == null) {
                    _nodes = EMPTY_NODES;
                }
            } else {
                DTMAxisIterator keyValues = _keyValueIterator.reset();
                int retrievedKeyValueIdx = 0;
                boolean foundNodes = false;

                _nodes = null;

                // For each node in the node set argument, get the string value
                // and look up the nodes returned by key or id for that string
                // value.  If at most one string value has nodes associated,
                // the nodes will be stored in _nodes; otherwise, the nodes
                // will be placed in a heap.
                for (int keyValueNode = keyValues.next();
                     keyValueNode != DTMAxisIterator.END;
                     keyValueNode = keyValues.next()) {

                    String keyValue = BasisLibrary.stringF(keyValueNode, _dom);

                    IntegerArray nodes = lookupNodes(rootHandle, keyValue);

                    if (nodes != null) {
                        if (!foundNodes) {
                            _nodes = nodes;
                            foundNodes = true;
                        } else {
                            if (_nodes != null) {
                                addHeapNode(new KeyIndexHeapNode(_nodes));
                                _nodes = null;
                            }
                            addHeapNode(new KeyIndexHeapNode(nodes));
                        }
                    }
                }

                if (!foundNodes) {
                    _nodes = EMPTY_NODES;
                }
            }
        }

        /**
         * Returns the number of nodes in this iterator.
         *
         * @return the number of nodes
         */
        public int getLast() {
            // If nodes are stored in _nodes, take advantage of the fact that
            // there are no duplicates.  Otherwise, fall back to the base heap
            // implementaiton and hope it does a good job with this.
            return (_nodes != null) ? _nodes.cardinality() : super.getLast();
        }

        /**
         * Return the node at the given position.
         *
         * @param position The position
         * @return The node at the given position.
         */
        public int getNodeByPosition(int position) {
            int node = DTMAxisIterator.END;

            // If nodes are stored in _nodes, take advantage of the fact that
            // there are no duplicates and they are stored in document order.
            // Otherwise, fall back to the base heap implementation to do a
            // good job with this.
            if (_nodes != null) {
                if (position > 0) {
                    if (position <= _nodes.cardinality()) {
                        _position = position;
                        node = _nodes.at(position-1);
                    } else {
                        _position = _nodes.cardinality();
                    }
                }
            } else {
                node = super.getNodeByPosition(position);
            }

            return node;
        }
    }
}
