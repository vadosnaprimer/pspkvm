/*
 *
 *
 * Copyright  1990-2007 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.perseus.model;

import com.sun.perseus.platform.MessagesSupport;

/**
 *
 * @version $Id: Messages.java,v 1.5 2006/06/29 10:47:32 ln156897 Exp $
 */
final class Messages {
    /*
     * Error message codes.
     *
     */

    /**
     * Used when trying to load a file which is not an SVG resource.
     *
     * {0} : the invalid SVG file URI.
     */
    public static String ERROR_NON_SVG_RESOURCE
        = "error.non.svg.resource";

    /**
     * Used when the xlink:href attribute is missing on an mpath 
     * element.
     * 
     */
    public static String ERROR_MISSING_MPATH_HREF
        = "error.missing.mpath.href";

    /**
     * Used when the xlink:href attribute on an mpath element is 
     * invalid (i.e., when there is no element with that id or the 
     * value is not a local reference.
     * 
     * {0} : the invalid href value.
     */
    public static String ERROR_INVALID_MPATH_HREF
        = "error.invalid.mpath.href";

    /**
     * Used when trying to set an id on an element which already has an id.
     *
     * {0} : the current id on the element.
     * {1} : the new id that cannot be set.
     * {2} : the element's local name.
     * {3} : the element's namespace.
     */
    public static String ERROR_CANNOT_MODIFY_ID
        = "error.cannot.modify.id";

    /**
     * Used when trying to set an id to a value already taken by another 
     * element.
     *
     * {0} : the duplicate id.
     * {1} : the element's local name.
     * {2} : the element's namespace uri.
     * {3} : the existing element's local name (i.e., the element which has the 
     *       id already).
     * {4} : the existing element's namespace.
     */
    public static String ERROR_DUPLICATE_ID_VALUE
        = "error.duplicate.id.value";

    /**
     * Used when trying to create an animation for a trait that is not 
     * animatable.
     *
     * {0} : the trait namespace URI
     * {1} : the trait name
     * {2} : the element's namespace URI
     * {3} : the element's local name.
     */
    public static String ERROR_TRAIT_NOT_ANIMATABLE
        = "error.trait.not.animatable";

    /**
     * Used when a reference to a paint server does not point
     * to a paint server element.
     *
     * {0} : the invalid reference value
     * {1} : the invalid reference namespace URI.
     * {2} : the invalide reference local name
     */
    public static final String ERROR_INVALID_PAINT_SERVER_REFERENCE
        = "error.invalid.paint.server.reference";

    /**
     * Used when a trait is not supported. 
     *
     * {0} : trait name.
     * {1} : trait namespace.
     * {2} : localName.
     * {3} : namespaceURI.
     */
    public static final String ERROR_UNSUPPORTED_TRAIT 
        = "error.unsupported.trait";

    /**
     * Used when trying to get a trait with the wrong type.
     * {0} : trait name.
     * {1} : trait namespace.
     * {2} : invalid type.
     * {3} : localName
     * {4} : namespaceURI.
     */
    public static final String ERROR_TRAIT_TYPE_NS_MISMATCH 
        = "error.trait.type.ns.mismatch";

    /**
     * Used when trying to get a trait with the wrong type.
     * {0} : trait name.
     * {1} : invalid type.
     * {2} : localName
     * {3} : namespaceURI.
     */
    public static final String ERROR_TRAIT_TYPE_MISMATCH 
        = "error.trait.type.mismatch";

    /**
     * Used when trying to set a trait to an illegal value.
     *
     * {0} : trait name
     * {1} : invalid value
     * {2} : localName
     * {3} : namespaceURI
     */
    public static final String ERROR_INVALID_TRAIT_VALUE 
        = "error.invalid.trait.value";

    /**
     * Used when an animation has an invalid configuration, for example,
     * if there is no to, from, by or values attribute defined.
     *
     * {0} : target element id (may be null)
     * {1} : trait namespace
     * {2} : trait name
     * {3} : target element namespace
     * {4} : target element local name
     * {5} : animation id
     * {6} : animation namespace
     * {7} : animation local name
     * {8} : error description
     */
    public static final String ERROR_INVALID_ANIMATION_CONFIGURATION
        = "error.invalid.animation.configuration";

    /**
     * Used to create the error description when a from animation does not
     * specify a to or a by attribyte.
     */
    public static final String ERROR_INVALID_ANIMATION_FROM_ANIM
        = "error.invalid.animation.from.anim";

    /**
     * Used to create the error description when an animation does not specify
     * any of the to, from, by or values attribute.
     *
     */
    public static final String ERROR_INVALID_ANIMATION_NO_VALUES
        = "error.invalid.animation.no.values";

    /**
     * Used to create an error description when an animations' keyTimes 
     * attribute is incompatible with the number of values it defines.
     *
     * {0} : the keyTimes attribute value
     */
    public static final String ERROR_INVALID_ANIMATION_KEY_TIMES
        = "error.invalid.animation.key.times";

    /**
     * Used to create an error description when an animations' keySplines 
     * attribute is incompatible with animation keyTimes
     *
     * {0} : the keySplines attribute value
     * {1} : the keyTimes attribute value
     */
    public static final String ERROR_INVALID_ANIMATION_KEY_SPLINES
        = "error.invalid.animation.key.splines";

    /**
     * Used when trying to set a read-only trait.
     *
     * {0} : trait name
     * {1} : localName
     * {2} : namespaceURI
     */
    public static final String ERROR_READ_ONLY_TRAIT 
        = "error.read.only.trait";

    /**
     * Used when trying to remove a child which is not a child of
     * the target node.
     */
    public static final String ERROR_NOT_A_CHILD
        = "error.not.a.child";

    /**
     * Used when trying to remove a child on a node which does not
     * support child removal (like the Document node).
     *
     * {0} : localName
     * {1} : namespaceURI
     */
    public static final String ERROR_REMOVE_CHILD_NOT_SUPPORTED
        = "error.remove.child.not.supported";

    /**
     * Used when trying to remove a node which has an id or which
     * has descendants with ids.
     *
     */
    public static final String ERROR_CANNOT_REMOVE_NODE_WITH_ID
        = "error.cannot.remove.node.with.id";

    /**
     * Used when trying to insert the Document node under another 
     * node.
     */
    public static final String ERROR_CANNOT_INSERT_DOCUMENT_NODE
        = "error.cannot.insert.document.node";

    /**
     * Used when trying to insert a node which comes from another
     * Document.
     */
    public static final String ERROR_CANNOT_INSERT_FROM_OTHER_DOCUMENT
        = "error.cannot.insert.from.other.document";

    /**
     * Used when trying to insert a node of an incompatible type.
     *
     * {0} : localName for the inserted node
     * {1} : namespaceURI for the inserted node
     * {2} : localName fot the target parent node
     * {3} : namespaceURI for the target parent node.
     */
    public static final String ERROR_CHILD_NOT_ALLOWED
        = "error.child.not.allowed";

    /**
     * Used when trying to insert an node's ancestor under the node.
     */
    public static final String ERROR_INSERTING_ANCESTOR
        = "error.inserting.ancestor";

    /**
     * Used when trying to insert a node under the Document node.
     */
    public static final String ERROR_INSERTING_UNDER_DOCUMENT
        = "error.inserting.under.document";

    /**
     * Used in Node.insertBefore when the refChild is not a child of
     * the target node.
     */
    public static final String ERROR_REF_NODE_NOT_A_CHILD
        = "error.ref.node.not.a.child";

    /**
     * Used when trying to insert the document element under another 
     * node.
     */
    public static final String ERROR_INSERTING_DOCUMENT_ELEMENT
        = "error.inserting.document.element";

    /**
     * Used when invoking the given method on the given interface with
     * an invalid parameter value.
     *
     * {0} : interface name.
     * {1} : method name.
     * {2} : parameter name.
     * {3} : value.
     */
    public static final String ERROR_INVALID_PARAMETER_VALUE
        = "error.invalid.parameter.value";

    /**
     * Used when invoking the given method on the given interface with
     * an out of bound parameter value.
     *
     * {0} : interface name.
     * {1} : method name.
     * {2} : parameter name.
     * {3} : value.
     */
    public static final String ERROR_OUT_OF_BOUND_PARAMETER_VALUE
        = "error.out.of.bound.parameter.value";

    /**
     * The error code used when there are unresolved 
     * href references in the content.
     * {0} = the list of unresolved hrefs
     */
    static String ERROR_UNRESOLVED_REFERENCES
        = "error.unresolved.href";

    /**
     * The error code used when the xlink:href attribute is 
     * unspecified on an element which requires it.
     *
     * {0} = the element's namespace URI
     * {1} = the element's local name.
     * {2} = the element's id
     */
    static String ERROR_MISSING_REFERENCE
        = "error.missing.href";

    /**
     * Used when the attributeName attribute is not specified on an animation
     * element.
     *
     * {0} : the animation element's id
     * {1} : the animation element's local name.
     * {2} : the animation element's namespace URI.
     */
    static String ERROR_UNSPECIFIED_ATTRIBUTE_NAME
        = "error.unspecified.attribute.name";

    /**
     * Used when/if trying to define from-by animation on a trait that
     * does not have an additive type.
     *
     * {0} : the traitName
     * {1} : the traitNamespace
     * {2} : the animation localName
     * {3} : the animation namespace URI.
     */
    static String ERROR_ATTRIBUTE_NOT_ADDITIVE_FROM_BY
        = "error.attribute.not.additive.from.by";

    /**
     * Used when/if trying to define by animation on a trait that
     * does not have an additive type.
     *
     * {0} : the traitName
     * {1} : the traitNamespace
     * {2} : the animation localName
     * {3} : the animation namespace URI.
     */
    static String ERROR_ATTRIBUTE_NOT_ADDITIVE_BY
        = "error.attribute.not.additive.by";

    /**
     * Used when/if trying to define a from-by animation with incompatible
     * from and by values.
     *
     * {0} : the traitName
     * {1} : the traitNamespace
     * {2} : the animation localName
     * {3} : the animation namespace URI.
     * {4} : the from value
     * {5} : the to value.
     */
    static String ERROR_INCOMPATIBLE_FROM_BY
        = "error.incompatible.from.by";

    /**
     * Error when a method is called while the target object is in the
     * wrong state.
     *
     * {0} : the target object class name.
     * {1} : the target object's current state.
     * {2} : the method name
     * {3} : the valid state(s) for the method.
     */
    static String ERROR_INVALID_STATE
        = "error.invalid.state";

    /**
     * This class does not need to be instantiated.
     */
    private Messages() {}

    /**
     * The error messages bundle class name.
     */
    protected static final String RESOURCES =
        "com.sun.perseus.model.resources.Messages";

    /**
     * The localizable support for the error messages.
     */
    protected static MessagesSupport messagesSupport =
        new MessagesSupport(RESOURCES);

    /**
     * Formats the message identified by <tt>key</tt> with the input
     * arguments.
     * 
     * @param key the message's key
     * @param args the arguments used to format the message
     * @return the formatted message
     */
    public static String formatMessage(final String key, final Object[] args) {
        return messagesSupport.formatMessage(key, args);
    }
}
