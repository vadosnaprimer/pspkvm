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

import com.sun.perseus.util.SVGConstants;

import org.w3c.dom.DOMException;

import org.w3c.dom.svg.SVGAnimationElement;

import com.sun.perseus.j2d.RenderGraphics;

/**
 * <code>TimedElementNode</code> models <code>ModelNodes</code which 
 * represent Timed Elements.
 *
 *
 * @version $Id: TimedElementNode.java,v 1.6 2006/07/13 00:55:58 st125089 Exp $
 */
public class TimedElementNode extends ElementNode 
                              implements SVGAnimationElement {
    /**
     * The timing support is added by compositing rather than inheritance
     * because Java only allows single inheritance.
     */
    protected TimedElementSupport timedElementSupport;

    /**
     * The timed element's local name.
     */
    protected String localName;

    /**
     * Builds a new timed element that belongs to the given
     * document. This <code>TimedElementNode</code> will belong 
     * to the <code>DocumentNode</code>'s time container.
     *
     * @param ownerDocument the document this node belongs to.
     * @param localName the element's local name
     * @throws IllegalArgumentException if the input ownerDocument is null
     */
    public TimedElementNode(final DocumentNode ownerDocument,
                            final String localName) {
        this(ownerDocument, new TimedElementSupport(), localName);
    }

    /**
     * Builds a new timed element that belongs to the given
     * document. This <code>TimedElementNode</code> will belong 
     * to the <code>DocumentNode</code>'s time container.
     *
     * @param ownerDocument the document this node belongs to.
     * @throws IllegalArgumentException if the input ownerDocument is null
     */
    public TimedElementNode(final DocumentNode ownerDocument) {
        this(ownerDocument, new TimedElementSupport());
    }

    /**
     * Constructor used by derived classes.
     *
     * @param ownerDocument the document this node belongs to.
     * @param timedElementSupport the associated 
     *        <code>TimedElementSupport</code>.
     * @throws IllegalArgumentException if the input ownerDocument is null.
     */
    protected TimedElementNode(final DocumentNode ownerDocument, 
                               final TimedElementSupport timedElementSupport) {
        this(ownerDocument, 
             timedElementSupport,
             // <!> IMPL NOTE : DO THIS WHILE THE REST OF THE ANIMATION
             // IMPLEMENTATION IS PENDING.
             SVGConstants.SVG_SET_TAG);
    }

    /**
     * Constructor used by derived classes.
     *
     * @param ownerDocument the document this node belongs to.
     * @param timedElementSupport the associated 
     *        <code>TimedElementSupport</code>.
     * @param localName the element's local name. Should not be null.
     * @throws IllegalArgumentException if the input ownerDocument is null 
     *         or if the input timedElementSupport is null.
     */
    protected TimedElementNode(final DocumentNode ownerDocument, 
                               final TimedElementSupport timedElementSupport,
                               final String localName) {
        super(ownerDocument);

        if (timedElementSupport == null) {
            throw new IllegalArgumentException();
        }

        if (localName == null) {
            throw new IllegalArgumentException();
        }

        this.timedElementSupport = timedElementSupport;

        this.localName = localName;

        timedElementSupport.animationElement = this;
    }
    
    /**
     * When a TimedElementNode is hooked into the document tree, it needs
     * to register with the closest ancestor TimeContainerNode it can 
     * find. If none, it must register with the root time container.
     */
    void nodeHookedInDocumentTree() {
        super.nodeHookedInDocumentTree();

        ModelNode p = parent;
        while (p != ownerDocument && p != null) {
            if (p instanceof TimeContainerNode) {
                timedElementSupport.setTimeContainer
                    (((TimeContainerNode) p).timeContainerSupport);
                break;
            }
            p = p.parent;
        }

        if (p == ownerDocument) {
            timedElementSupport.setTimeContainer
                (ownerDocument.timeContainerRootSupport);
        }
    }

    /**
     * When a TimedElementNode is unhooked from the document tree, it 
     * needs to unregister from its TimeContainer node. Extentions, such 
     * as Animation, may have to perform additional operations, such as
     * removing themselves from TraitAnim.
     */
    void nodeUnhookedFromDocumentTree() {
        timedElementSupport.setTimeContainer(null);
        timedElementSupport.reset();
    }

    /**
     * @return the animation tag name passed at construction time.
     */
    public String getLocalName() {
        return localName;
    }

    /**
     * The default value for the begin attribute is '0s'.
     *
     * @return an array of trait default values, used if this element
     *         requires that the default trait value be explicitly 
     *         set through a setTrait call. This happens, for example,
     *         with the begin trait value on animation elements.
     */
    public String[][] getDefaultTraits() {
        return new String[][] { {SVGConstants.SVG_BEGIN_ATTRIBUTE, "0s"} };
    }

    /**
     * Used by <code>DocumentNode</code> to create a new instance from
     * a prototype <code>TimedElementNode</code>.
     *
     * @param doc the <code>DocumentNode</code> for which a new node is
     *        should be created.
     * @return a new <code>TimedElementNode</code> for the requested document.
     */
    public ElementNode newInstance(final DocumentNode doc) {
        return new TimedElementNode(doc, new TimedElementSupport(), localName);
    }

    /**
     * @return this node's <code>TimedElementSupport</code>
     */
    public TimedElementSupport getTimedElementSupport() {
        return timedElementSupport;
    }

    /**
     *
     */
    public void beginElementAt(float offset) {
        timedElementSupport.beginAt((long) (offset * 1000));
    }

    /**
     * Creates a begin instance time for the current time.
     */
    public void beginElement() {
        timedElementSupport.beginAt(0);
    }

    /**
     *
     */
    public void endElementAt(float offset) {
        timedElementSupport.endAt((long) (offset * 1000));
    }

    /**
     * Creates an end instance time for the current time.
     */
    public void endElement() {
        timedElementSupport.endAt(0);
    }

    /**
     * Pauses the element. If the element is already paused, this method has no
     * effect. See the SMIL 2 specification for a description of <a
     * href="http://www.w3.org/TR/2001/REC-smil20-20010807/smil20.html#smil-timing-Timing-PausedElementsAndActiveDur">pausing
     * elements</a>.
     */
    public void pauseElement() {
        throw new Error("NOT IMPLEMENTED");
    }

    /**
     * Unpauses the element if it was paused. If the element was not paused,
     * this method has no effect. See the SMIL 2 specification for a description
     * of <a
     * href="http://www.w3.org/TR/2001/REC-smil20-20010807/smil20.html#smil-timing-Timing-PausedElementsAndActiveDur">pausing
     * elements</a>.
     */
    public void unpauseElement() {
        throw new Error("NOT IMPLEMENTED");
    }

    /**
     * @return true if the element is currently paused, false otherwise. See the
     * SMIL 2 specification for a description of <a
     * href="http://www.w3.org/TR/2001/REC-smil20-20010807/smil20.html#smil-timing-Timing-PausedElementsAndActiveDur">pausing
     * elements</a>.
     */
    public boolean getElementPaused() {
        throw new Error("NOT IMPLEMENTED");
    }

    /**
     * TimedElementNode supports the begin, end, dur, min, max, restart,
     * repeatCount, repeatDur and fill attributes.
     *
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods (such as <code>getTrait</code> or 
     *         <code>setFloatTrait</code>.
     */
    boolean supportsTrait(final String traitName) {
        if (SVGConstants.SVG_BEGIN_ATTRIBUTE == traitName
            || 
            SVGConstants.SVG_END_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_DUR_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_MIN_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_MAX_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_RESTART_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_REPEAT_COUNT_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_REPEAT_DUR_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_FILL_ATTRIBUTE == traitName) {
            return true;
        }

        return super.supportsTrait(traitName);
    }

    // JAVADOC COMMENT ELIDED
    public String getTraitImpl(final String name)
        throws DOMException {
        if (SVGConstants.SVG_BEGIN_ATTRIBUTE == name) {
            if (timedElementSupport.beginConditions.size() == 0) {
                return "0s";
            }
            return TimeCondition.toStringTrait(
                    timedElementSupport.beginConditions);
        } else if (SVGConstants.SVG_END_ATTRIBUTE == name) {
            if (timedElementSupport.endConditions.size() == 0) {
                return SVGConstants.SVG_INDEFINITE_VALUE;
            }
            return TimeCondition.toStringTrait(
                    timedElementSupport.endConditions);
        } else if (SVGConstants.SVG_DUR_ATTRIBUTE == name) {
            return Time.toStringTrait(timedElementSupport.dur);
        } else if (SVGConstants.SVG_MIN_ATTRIBUTE == name) {
            return Time.toStringTrait(timedElementSupport.min);
        } else if (SVGConstants.SVG_MAX_ATTRIBUTE == name) {
            return Time.toStringTrait(timedElementSupport.max);
        } else if (SVGConstants.SVG_RESTART_ATTRIBUTE == name) {
            switch (timedElementSupport.restart) {
            case TimedElementSupport.RESTART_ALWAYS:
                return SVGConstants.SVG_ALWAYS_VALUE;
            case TimedElementSupport.RESTART_WHEN_NOT_ACTIVE:
                return SVGConstants.SVG_WHEN_NOT_ACTIVE_VALUE;
            case TimedElementSupport.RESTART_NEVER:
                return SVGConstants.SVG_NEVER_VALUE;
            default:
                throw new IllegalStateException();
            }
        } else if (SVGConstants.SVG_REPEAT_COUNT_ATTRIBUTE == name) {
            if (Float.isNaN(timedElementSupport.repeatCount)) {
                return null; // Unspecified
            } else if (timedElementSupport.repeatCount == Float.MAX_VALUE) {
                return SVGConstants.SVG_INDEFINITE_VALUE;
            }
            return Float.toString(timedElementSupport.repeatCount);
        } else if (SVGConstants.SVG_REPEAT_DUR_ATTRIBUTE == name) {
            return Time.toStringTrait(timedElementSupport.repeatDur);
        } else if (SVGConstants.SVG_FILL_ATTRIBUTE == name) {
            switch (timedElementSupport.fillBehavior) {
            case TimedElementSupport.FILL_BEHAVIOR_REMOVE:
                return SVGConstants.SVG_REMOVE_VALUE;
            case TimedElementSupport.FILL_BEHAVIOR_FREEZE:
                return SVGConstants.SVG_FREEZE_VALUE;
            default:
                throw new IllegalStateException();
            }
        } else {
            return super.getTraitImpl(name);
        }
    }

    /**
     * Parses the input value an creates TimeCondition instances for the
     * current instance.
     *
     * @param traitName the name of the time condition trait.
     * @param value the trait value.
     * @param isBegin true if this should be parsed as a begin value, i.e., 
     * with a 0s default.
     * @throws DOMException if the input value is invalid.
     */
    protected void parseTimeConditionsTrait(final String traitName,
                                            final String value,
                                            final boolean isBegin) 
        throws DOMException {
        try {
            ownerDocument.timeConditionParser.parseBeginEndAttribute(value,
                                                                     this,
                                                                     isBegin);
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
            throw illegalTraitValue(traitName, value);
        }                                                           
    }

    // JAVADOC COMMENT ELIDED
    public void setTraitImpl(final String name, 
                             final String value)
        throws DOMException {
        if (SVGConstants.SVG_BEGIN_ATTRIBUTE == name) {
            checkWriteLoading(name);
            timedElementSupport.beginConditions.removeAllElements();
            parseTimeConditionsTrait(name, value, true);
        } else if (SVGConstants.SVG_END_ATTRIBUTE == name) {
            checkWriteLoading(name);
            timedElementSupport.endConditions.removeAllElements();
            parseTimeConditionsTrait(name, value, false);
        } else if (SVGConstants.SVG_DUR_ATTRIBUTE == name) {
            checkWriteLoading(name);
            // Ignore 'media'
            if (SVGConstants.SVG_MEDIA_VALUE.equals(value)) {
                return;
            }
            timedElementSupport.setDur(parseClockTrait(name, value));
        } else if (SVGConstants.SVG_MIN_ATTRIBUTE == name) {
            checkWriteLoading(name);

            // Ignore 'media'
            if (SVGConstants.SVG_MEDIA_VALUE.equals(value)) {
                return;
            }
            timedElementSupport.setMin(parseMinMaxClock(name, value, true));
        } else if (SVGConstants.SVG_MAX_ATTRIBUTE == name) {
            checkWriteLoading(name);

            // Ignore 'media'
            if (SVGConstants.SVG_MEDIA_VALUE.equals(value)) {
                return;
            }
            timedElementSupport.setMax(parseMinMaxClock(name, value, false));
        } else if (SVGConstants.SVG_RESTART_ATTRIBUTE == name) {
            checkWriteLoading(name);

            if (SVGConstants.SVG_ALWAYS_VALUE.equals(value)) {
                timedElementSupport.restart = 
                    TimedElementSupport.RESTART_ALWAYS;
            } else if (SVGConstants.SVG_WHEN_NOT_ACTIVE_VALUE.equals(value)) {
                timedElementSupport.restart =
                    TimedElementSupport.RESTART_WHEN_NOT_ACTIVE;
            } else if (SVGConstants.SVG_NEVER_VALUE.equals(value)) {
                timedElementSupport.restart =
                    TimedElementSupport.RESTART_NEVER;
            } else {
                throw illegalTraitValue(name, value);
            }
        } else if (SVGConstants.SVG_REPEAT_COUNT_ATTRIBUTE == name) {
            checkWriteLoading(name);

            if (SVGConstants.SVG_INDEFINITE_VALUE.equals(value)) {
                timedElementSupport.repeatCount = Float.MAX_VALUE;
            } else {
                timedElementSupport.repeatCount =
                    parseFloatTrait(name, value);
            }
        } else if (SVGConstants.SVG_REPEAT_DUR_ATTRIBUTE == name) {
            checkWriteLoading(name);
            if (SVGConstants.SVG_INDEFINITE_VALUE.equals(value)) {
                timedElementSupport.repeatDur = Time.INDEFINITE;
            } else {
                timedElementSupport.setRepeatDur(parseClockTrait(name, value));
            }
        } else if (SVGConstants.SVG_FILL_ATTRIBUTE == name) {
            checkWriteLoading(name);
            if (SVGConstants.SVG_REMOVE_VALUE.equals(value)) {
                timedElementSupport.fillBehavior 
                    = TimedElementSupport.FILL_BEHAVIOR_REMOVE;
            } else if (SVGConstants.SVG_FREEZE_VALUE.equals(value)) {
                timedElementSupport.fillBehavior
                    = TimedElementSupport.FILL_BEHAVIOR_FREEZE;
            } else {
                throw illegalTraitValue(name, value);
            }
        } else {
            super.setTraitImpl(name, value);
        }
    }

}
