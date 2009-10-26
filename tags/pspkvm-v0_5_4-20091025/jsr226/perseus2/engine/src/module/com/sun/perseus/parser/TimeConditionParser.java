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

package com.sun.perseus.parser;

import java.util.Date;
import java.util.Vector;

import com.sun.perseus.model.TimedElementNode;
import com.sun.perseus.model.TimedElementSupport;

import com.sun.perseus.model.AccessKeyCondition;
import com.sun.perseus.model.EventBaseCondition;
import com.sun.perseus.model.OffsetCondition;
import com.sun.perseus.model.RepeatCondition;
import com.sun.perseus.model.SyncBaseCondition;
import com.sun.perseus.model.TimeCondition;

import com.sun.perseus.util.SVGConstants;

/**
 * Parser for SVG time condition values, as defined in the SVG specification
 * for timing attributes:
 *   http://www.w3.org/TR/SVG11/animate.html#TimingAttributes
 *
 * @author <a href="mailto:christopher.campbell@sun.com">Chris Campbell</a>
 * @version $Id: TimeConditionParser.java,v 1.4 2006/04/21 06:40:40 st125089 Exp $
 */
public class TimeConditionParser extends ClockParser {

    /**
     * The list of time conditions being parsed.
     */
    private Vector conditions;

    /**
     * If true, a "begin" attribute is being parsed; otherwise, an "end"
     * attribute is being parsed.
     */
    private boolean isBegin;

    /**
     * The TimedElementNode for the attribute being parsed.
     */
    private TimedElementNode ten;

    /**
     * The TimedElementSupport for the TimedElementNode associated with
     * the attribute being parsed.
     */
    private TimedElementSupport tes;

    /**
     * Parses a begin/end timing attribute value.  This method throws an
     * <code>IllegalArgumentException</code> if the input argument's
     * syntax does not conform to that of a clock value, as defined
     * by the SVG animate tag specification.
     *
     * @param attrValue the value to parse.
     * @param ten the TimedElementNode for which the attribute is parsed.
     * @param isBegin defines whether this is a begin attribute.
     * @return an array of <code>TimeConditions</code> corresponding to the
     * input attrubute value.
     */
    public TimeCondition[] parseBeginEndAttribute(final String attrValue,
                                                  final TimedElementNode ten,
                                                  final boolean isBegin) {
        setString(attrValue);

        if (attrValue.length() == 0) {
            throw new IllegalArgumentException();
        }

        this.conditions = new Vector();
        this.ten = ten;
        this.tes = ten.getTimedElementSupport();
        this.isBegin = isBegin;

        loop: for (;;) {
            // parse leading whitespace
            current = read();
            skipSpaces();

            m1: switch (current) {
            case ';':
                throw new IllegalArgumentException();
            case -1:
                if (isBegin && (conditions.size() == 0)) {
                    // an empty begin attribute maps to offset(0)
                    conditions.addElement(new OffsetCondition(tes, isBegin, 0L));
                }
                break loop;
            case '+': case '-':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
                parseOffset();
                break m1;
            default:
                if (currentStartsWith("accessKey(")) {
                    parseAccessKey();
                } else if (currentStartsWith("repeat(")) {
                    parseRepeat(null);
                } else if (currentStartsWith("indefinite")) {
                    // indefinite maps to no time condition
                    break m1;
                } else {
                    parseOther();
                }
                break m1;
            }

            // parse trailing whitespace (current should point to the character
            // just following the last time condition)
            skipSpaces();

            // detect ';' separator, or -1
            m2: switch (current) {
            case ';':
                break m2;
            case -1:
                break loop;
            default:
                throw new IllegalArgumentException();
            }
        }

        TimeCondition[] ret = new TimeCondition[conditions.size()];
        conditions.copyInto(ret);
        return ret;
    }

    /**
     * Helper method that parses a signed clock value (one that may or may
     * not be preceded by a '-' or '+' sign.  Returns the parsed clock value
     * as a long offset.  If the clock string begins with a '-' sign, the
     * returned offset value will be negative.  If the clock string begins
     * with a '+' sign (or is unsigned), the returned offset value will be
     * positive.
     *
     * @return a signed long offset value (in milliseconds)
     * @see ClockParser
     */
    protected final long parseSignedClockValue() {
        boolean pos = true;
        switch (current) {
        case '-':
            pos = false;
            // FALLTHROUGH
        case '+':
            current = read();
            skipSpaces();
            break;
        default:
            break;
        }

        long offset = parseClock(false);
        if (!pos) {
            offset = -offset;
        }

        return offset;
    }

    /**
     * Parses an optional offset value.  If the current character is a sign
     * value ('+' or '-'), the offset value is parsed and returned as a long
     * value.  If the offset value is not present (<code>current</code> is
     * ';' or -1), this method returns zero.  If some other character is
     * encountered, an <code>IllegalArgumentException</code> is thrown.
     *
     * @return a signed long offset value (in milliseconds)
     * @see #parseSignedClockValue
     */
    protected final long parseOptionalOffset() {
        long offset = 0L;

        switch (current) {
        case '+': case '-':
            offset = parseSignedClockValue();
            break;
        case ';':
        case -1:
            break;
        default:
            throw new IllegalArgumentException("Unsupported character in optional offset value: " + ((char) current));
        }

        return offset;
    }

    /**
     * Parses an offset time condition.  The current character is assumed
     * to be '+', '-', or a numeral.  If parsing is successful, an
     * OffsetCondition is added to the list of time conditions and the
     * current character is the character that immediately follows the offset
     * value.
     *
     * @see #parseSignedClockValue
     */
    protected final void parseOffset() {
        long offset = parseSignedClockValue();
        conditions.addElement(new OffsetCondition(tes, isBegin, offset));
    }

    /**
     * Parses an accessKey time condition.  The current character is assumed
     * to be the first character after "accesskey(".  If parsing is successful,
     * an AccessKeyCondition is added to the list of time conditions and the
     * current character is the character that immediately follows the
     * access key value (either the ')' or the optional offset).
     */
    protected final void parseAccessKey() {
        // parse access key character (followed by ')')
        if ((current == -1) || (current == ')')) {
            throw new IllegalArgumentException();
        }
        char accesskey = (char) current;
        current = read();
        if (current != ')') {
            throw new IllegalArgumentException();
        }

        // parse whitespace followed by optional offset
        current = read();
        skipSpaces();
        long offset = parseOptionalOffset();

        conditions.addElement(new AccessKeyCondition(tes, isBegin,
                                                     offset, accesskey));
    }

    /**
     * Parses a repeat time condition.  The current character is assumed
     * to be the first character after "repeat(".  If parsing is successful,
     * a RepeatCondition is added to the list of time conditions and the
     * current character is the character that immediately follows the
     * repeat value (either the ')' or the optional offset).
     *
     * @param id the identifier for this time condition; if null, the event
     * base id will be "unspecified"
     */
    protected final void parseRepeat(final String id) {
        // parse iteration value (followed by ')')
        int repeatCount = 0;
        loop: for (;;) {
            switch (current) {
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
                repeatCount = repeatCount * 10 + (current - '0');
                break;
            case ')':
                break loop;
            default:
                throw new IllegalArgumentException("Illegal character in repeat condition: " + ((char) current));
            }
            current = read();
        }

        // parse whitespace followed by optional offset
        current = read();
        skipSpaces();
        long offset = parseOptionalOffset();

        conditions.addElement(new RepeatCondition(tes, isBegin, id,
                                                  offset, repeatCount));
    }

    /**
     * Parses a syncbase time condition.  The current character is assumed
     * to be the first character after "begin" or "end".  If parsing is
     * successful, a SyncBaseCondition is added to the list of time conditions
     * and the current character is the character that immediately follows the
     * syncbase value (either the "begin", "end", or the optional offset).
     *
     * @param id the identifier for this time condition; should not be null
     * @param isBeginSync true if this condition is on the syncBase's begin
     * condition; false if this condition is on the syncBase's end condition.
     */
    protected final void parseSyncBase(final String id,
                                       final boolean isBeginSync) {
        // parse whitespace followed by optional offset
        skipSpaces();
        long offset = parseOptionalOffset();

        conditions.addElement(new SyncBaseCondition(ten, isBegin, id,
                                                    isBeginSync, offset));
    }

    /**
     * Parses an event base time condition.  If <code>id</code> is
     * <code>null</code>, the event base id will be "unspecified", and the
     * eventType is assumed to be non-<code>null</code> (the current character
     * is assumed to be the first character after the event type).  If
     * <code>eventType</code> is <code>null</code>, the event type will be
     * parsed (the current character is assumed to be the first character after
     * the event base id and dot, e.g. "foo.").
     *
     * If parsing is successful, an EventBaseCondition is added to the list
     * of time conditions and the current character is the character that
     * immediately follows the event base value (either the id, eventType,
     * or the optional offset).
     *
     * @param id the identifier for this time condition; can be null
     * @param eventType the event type for this time condition; can be null
     * if the id was already parsed
     */
    protected final void parseEvent(final String id, String eventType) {
        if (eventType == null) {
            StringBuffer etbuf = new StringBuffer();
            loop: for (;;) {
                switch (current) {
                case '\\':
                    // skip escape characters
                    current = read();
                    switch (current) {
                    case '.':
                    case '-':
                    case '_':
                        etbuf.append((char) current);
                        break;
                    default:
                        if (isLetterOrDigit((char) current)) {
                            etbuf.append((char) current);
                        } else {
                            throw new IllegalArgumentException();
                        }
                        break;
                    }
                    break;
                case '.':
                    throw new IllegalArgumentException();
                case '+':
                case '-':
                case ';':
                case -1:
                    if (etbuf.length() == 0) {
                        throw new IllegalArgumentException();
                    }
                    eventType = etbuf.toString();
                    break loop;
                case 0x20:
                case 0x09:
                case 0x0D:
                case 0x0A:
                    skipSpaces();
                    switch (current) {
                    case '+':
                    case '-':
                    case ';':
                    case -1:
                        // no id, must be an event
                        if (etbuf.length() == 0) {
                            throw new IllegalArgumentException();
                        }
                        eventType = etbuf.toString();
                        break loop;
                    default:
                        throw new IllegalArgumentException();
                    }     
                default:
                    etbuf.append((char) current);
                    break;
                }
                current = read();
            }
        }

        // parse optional offset (whitespace has already been parsed)
        long offset = parseOptionalOffset();

        conditions.addElement(new EventBaseCondition(tes, isBegin,
                                                     id, toDOMEventType(eventType),
                                                     offset));
    }

    /**
     * Parses a time condition with an optional id prefix.  Delegates to one
     * of the other <code>parse*()</code> methods, depending on whether the
     * string matches a repeat, syncbase, or event time condition.
     */
    protected final void parseOther() {
        StringBuffer idbuf = new StringBuffer();
        String id = null;
        String eventType = null;

        loopid: for (;;) {
            switch (current) {
            case '\\':
                // skip escape characters
                current = read();
                switch (current) {
                case '.':
                case '-':
                case '_':
                    idbuf.append((char) current);
                    break;
                default:
                    if (isLetterOrDigit((char) current)) {
                        idbuf.append((char) current);
                    } else {
                        throw new IllegalArgumentException();
                    }
                    break;
                }
                break;
            case '.':
                if (idbuf.length() == 0) {
                    throw new IllegalArgumentException();
                }
                id = idbuf.toString();
                current = read();
                break loopid;
            case '+':
            case '-':
            case ';':
            case -1:
                // no id, must be an event (we will never enter this method
                // with one of these characters as the current character, so
                // idbuf will always have a non-zero length at this point)
                eventType = idbuf.toString();
                break loopid;
            case 0x20:
            case 0x09:
            case 0x0D:
            case 0x0A:
                skipSpaces();
                switch (current) {
                case '+':
                case '-':
                case ';':
                case -1:
                    // no id, must be an event (we will never enter this method
                    // with one of these characters as the current character,
                    // so idbuf will always have a non-zero length at this
                    // point)
                    eventType = idbuf.toString();
                    break loopid;
                default:
                    throw new IllegalArgumentException();
                }
            default:
                idbuf.append((char) current);
                break;
            }
            current = read();
        }

        if (id != null) {
            if (currentStartsWith("repeat(")) {
                parseRepeat(id);
            } else if (currentStartsWith("begin")) {
                parseSyncBase(id, true);
            } else if (currentStartsWith("end")) {
                parseSyncBase(id, false);
            } else {
                parseEvent(id, null);
            }
        } else {
            parseEvent(null, eventType);
        }
    }

    /**
     * Do not use Character.isLetterOrDigit because it is not supported on 
     * all Java platforms.
     * 
     * @param c the character to test.
     * @return true if c is a letter or a digit.
     */
    public static final boolean isLetterOrDigit(final char c) {
        return (Character.isDigit(c) || Character.isUpperCase(c) || Character.isLowerCase(c));
    }

    /**
     * Converts the event to the DOMEvent type. This is required because for some
     * events, the name used in the SMIL begin/end attribute is not the same as
     * the DOM event so some translation is needed.
     *
     * @param smilEventType the name of the animation event type.
     * @return the DOM Level 2 Event name.
     *
     * @see http://www.w3.org/TR/SVG11/interact.html#SVGEvents
     */
    public static final String toDOMEventType(final String smilEventType) {
        if (SVGConstants.SVG_SMIL_FOCUS_IN_EVENT_TYPE.equals(smilEventType)) {
            return SVGConstants.SVG_DOMFOCUSIN_EVENT_TYPE;
        } else if (SVGConstants.SVG_SMIL_FOCUS_OUT_EVENT_TYPE.equals(smilEventType)) {
            return SVGConstants.SVG_DOMFOCUSOUT_EVENT_TYPE;
        } else if (SVGConstants.SVG_SMIL_ACTIVATE_EVENT_TYPE.equals(smilEventType)) {
            return SVGConstants.SVG_DOMACTIVATE_EVENT_TYPE;
        }

        return smilEventType;
    }
}
