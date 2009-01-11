/*
 *
 *
 * Portions Copyright  2000-2007 Sun Microsystems, Inc. All Rights
 * Reserved.  Use is subject to license terms.
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

/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included with this distribution in  *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package com.sun.perseus.parser;

/**
 * Parser for SVG Clock values, as originally defined in the SMIL spec:
 * <pre>
 * Clock-val         ::= Full-clock-val | Partial-clock-val 
 *                       | Timecount-val
 * Full-clock-val    ::= Hours ":" Minutes ":" Seconds ("." Fraction)?
 * Partial-clock-val ::= Minutes ":" Seconds ("." Fraction)?
 * Timecount-val     ::= Timecount ("." Fraction)? (Metric)?
 * Metric            ::= "h" | "min" | "s" | "ms"
 * Hours             ::= DIGIT+; any positive number
 * Minutes           ::= 2DIGIT; range from 00 to 59
 * Seconds           ::= 2DIGIT; range from 00 to 59
 * Fraction          ::= DIGIT+
 * Timecount         ::= DIGIT+
 * 2DIGIT            ::= DIGIT DIGIT
 * DIGIT             ::= [0-9]
 * </pre>
 *
 * @author <a href="mailto:christopher.campbell@sun.com">Chris Campbell</a>
 * @version $Id: ClockParser.java,v 1.3 2006/04/21 06:40:23 st125089 Exp $
 */
public class ClockParser extends AbstractParser {

    /** Number of milliseconds in a second */
    public static final int MILLIS_PER_SECOND = 1000;

    /** Number of seconds in a minute */
    public static final int SECONDS_PER_MINUTE = 60;

    /** Number of milliseconds in a minute */
    public static final int MILLIS_PER_MINUTE =
        SECONDS_PER_MINUTE * MILLIS_PER_SECOND;

    /** Number of minutes in an hour */
    public static final int MINUTES_PER_HOUR = 60;

    /** Number of milliseconds in an hour */
    public static final int MILLIS_PER_HOUR =
        MINUTES_PER_HOUR * MILLIS_PER_MINUTE;

    /**
     * Total number of milliseconds represented by this clock value.
     */
    private long millis;

    /**
     * Parses a clock value.  This method throws an
     * <code>IllegalArgumentException</code> if the input argument's
     * syntax does not conform to that of a clock value, as defined
     * by the SMIL specification.
     *
     * @param clockString the value to convert to a long offset value.
     * @return long offset value corresponding to the input argument.
     */
    public long parseClock(final String clockString) {
        setString(clockString);
        current = read();
        return parseClock(true);
    }

    /**
     * Parses a clock value, beginning at the current character.
     *
     * @param eos if true, then there should be no more
     * characters at the end of the string (excess characters will produce
     * an <code>IllegalArgumentException</code>); if false, the parser will
     * treat whitespace and ';' characters as if it marked the end of string.
     * @return a long offset value.
     */
    protected long parseClock(final boolean eos) {
        millis = 0L;
        int[] wholeParts = new int[3];
        float fractionPart = 0.0f;
        int numWholeParts = 0;
        int tmp = 0;
        boolean isTimeCountVal = true;
        boolean isFirstDigitInferiorToSix = false;
        boolean hasFractionPart = false;
        
        // first digit (could be part of full, partial, or count)
        m1: switch (current) {
        default:
            throw new IllegalArgumentException();
        case '0': case '1': case '2': case '3': case '4': case '5':
            isFirstDigitInferiorToSix = true;
            break m1;
        case '6': case '7': case '8': case '9':
            break m1;
        }
        
        tmp = tmp * 10 + (current - '0');
        
        current = read();

        m2: switch (current) {
        default:
            throw new IllegalArgumentException();
        case 0x20: case 0x09: case 0x0D: case 0x0A: case ';':
            if (eos) {
                throw new IllegalArgumentException();
            }
            // FALLTHROUGH
        case -1:
            wholeParts[numWholeParts++] = tmp;
            break m2;
        case ':':
            if (isFirstDigitInferiorToSix) {
                isTimeCountVal = false;
                wholeParts[numWholeParts++] = tmp;
                tmp = 0;
                current = read();
                switch (current) {
                default:
                    throw new IllegalArgumentException();
                case '0': case '1': case '2': case '3': case '4': case '5':
                    tmp = tmp * 10 + (current - '0');
                    current = read();
                    switch (current) {
                    default:
                        throw new IllegalArgumentException();
                    case '0': case '1': case '2': case '3': case '4':
                    case '5': case '6': case '7': case '8': case '9':
                        tmp = tmp * 10 + (current - '0');
                        current = read();
                        switch (current) {
                        default:
                            throw new IllegalArgumentException();
                        case ':':
                            wholeParts[numWholeParts++] = tmp;
                            tmp = 0;
                            current = read();
                            switch (current) {
                            default:
                                throw new IllegalArgumentException();
                            case '0': case '1': case '2': 
                            case '3': case '4': case '5':
                                tmp = tmp * 10 + (current - '0');
                                current = read();
                                switch (current) {
                                default:
                                    throw new IllegalArgumentException();
                                case '0': case '1': case '2': case '3': 
                                case '4': case '5': case '6': case '7':
                                case '8': case '9':
                                    tmp = tmp * 10 + (current - '0');
                                    current = read();
                                    switch (current) {
                                    default:
                                        throw new IllegalArgumentException();
                                    case '.':
                                        break m2;
                                    case 0x20: case 0x09:
                                    case 0x0D: case 0x0A:
                                    case ';':
                                        if (eos) {
                                            throw
                                                new IllegalArgumentException();
                                        }
                                        // FALLTHROUGH
                                    case -1:
                                        wholeParts[numWholeParts++] = tmp;
                                        break m2;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            break m2;
        case '.': case 'h': case 'm': case 's':
            break m2;
        case '0': case '1': case '2': case '3': case '4':
        case '5': case '6': case '7': case '8': case '9':
            if (isFirstDigitInferiorToSix) {
                tmp = tmp * 10 + (current - '0');
                current = read();
                switch (current) {
                default:
                    throw new IllegalArgumentException();
                case 0x20: case 0x09: case 0x0D: case 0x0A: case ';':
                    if (eos) {
                        throw new IllegalArgumentException();
                    }
                    // FALLTHROUGH
                case -1:
                    wholeParts[numWholeParts++] = tmp;
                    break m2;
                case '.': case 'h': case 'm': case 's':
                case '0': case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':
                    break m2;
                case ':':
                    isTimeCountVal = false;
                    wholeParts[numWholeParts++] = tmp;
                    tmp = 0;
                    current = read();
                    switch (current) {
                    default:
                        throw new IllegalArgumentException();
                    case '0': case '1': case '2': case '3': case '4': case '5':
                        tmp = tmp * 10 + (current - '0');
                        current = read();
                        switch (current) {
                        default:
                            throw new IllegalArgumentException();
                        case '0': case '1': case '2': case '3': case '4':
                        case '5': case '6': case '7': case '8': case '9':
                            tmp = tmp * 10 + (current - '0');
                            current = read();
                            switch (current) {
                            default:
                                throw new IllegalArgumentException();
                            case '.':
                                break m2;
                            case 0x20: case 0x09: case 0x0D: case 0x0A:
                            case ';':
                                if (eos) {
                                    throw new IllegalArgumentException();
                                }
                                // FALLTHROUGH
                            case -1:
                                wholeParts[numWholeParts++] = tmp;
                                break m2;
                            case ':':
                                wholeParts[numWholeParts++] = tmp;
                                tmp = 0;
                                current = read();
                                switch (current) {
                                default:
                                    throw new IllegalArgumentException();
                                case '0': case '1': case '2': 
                                case '3': case '4': case '5':
                                    tmp = tmp * 10 + (current - '0');
                                    current = read();
                                    switch (current) {
                                    default:
                                        throw new IllegalArgumentException();
                                    case '0': case '1': case '2': case '3': 
                                    case '4': case '5': case '6': case '7':
                                    case '8': case '9':
                                        tmp = tmp * 10 + (current - '0');
                                        current = read();
                                        switch (current) {
                                        default:
                                            throw
                                                new IllegalArgumentException();
                                        case '.':
                                            break m2;
                                        case 0x20: case 0x09:
                                        case 0x0D: case 0x0A:
                                        case ';':
                                            if (eos) {
                                                throw
                                                    new
                                                    IllegalArgumentException();
                                            }
                                            // FALLTHROUGH
                                        case -1:
                                            wholeParts[numWholeParts++] = tmp;
                                            break m2;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        m3: switch (current) {
        default:
            throw new IllegalArgumentException();
        case 0x20: case 0x09: case 0x0D: case 0x0A: case ';':
            if (eos) {
                throw new IllegalArgumentException();
            }
            // FALLTHROUGH
        case -1:
            break m3;
        case ':': case '.': case 'h': case 'm': case 's':
            break m3;
        case '0': case '1': case '2': case '3': case '4':
        case '5': case '6': case '7': case '8': case '9':
            // must be in the hours field; loop until we reach the colon
            for (;;) {
                tmp = tmp * 10 + (current - '0');
                current = read();
                switch (current) {
                default:
                    throw new IllegalArgumentException();
                case 0x20: case 0x09: case 0x0D: case 0x0A: case ';':
                    if (eos) {
                        throw new IllegalArgumentException();
                    }
                    // FALLTHROUGH
                case -1:
                    wholeParts[numWholeParts++] = tmp;
                    break m3;
                case ':': case '.': case 'h': case 'm': case 's':
                    break m3;
                case '0': case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':
                }
            }
        }

        m4: switch (current) {
        default:
            throw new IllegalArgumentException();
        case 0x20: case 0x09: case 0x0D: case 0x0A: case ';':
            if (eos) {
                throw new IllegalArgumentException();
            }
            // FALLTHROUGH
        case -1:
            break m4;
        case '.': case 'h': case 'm': case 's':
            break m4;
        case ':':
            isTimeCountVal = false;
            wholeParts[numWholeParts++] = tmp;
            tmp = 0;
            current = read();
            switch (current) {
            default:
                throw new IllegalArgumentException();
            case '0': case '1': case '2': case '3': case '4': case '5':
                tmp = tmp * 10 + (current - '0');
                current = read();
                switch (current) {
                default:
                    throw new IllegalArgumentException();
                case '0': case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':
                    tmp = tmp * 10 + (current - '0');
                    current = read();
                    switch (current) {
                    default:
                        throw new IllegalArgumentException();
                    case ':':
                        wholeParts[numWholeParts++] = tmp;
                        tmp = 0;
                        current = read();
                        switch (current) {
                        default:
                            throw new IllegalArgumentException();
                        case '0': case '1': case '2':case '3': 
                        case '4': case '5':
                            tmp = tmp * 10 + (current - '0');
                            current = read();
                            switch (current) {
                            default:
                                throw new IllegalArgumentException();
                            case '0': case '1': case '2': case '3': case '4':
                            case '5': case '6': case '7': case '8': case '9':
                                tmp = tmp * 10 + (current - '0');
                                current = read();
                                switch (current) {
                                default:
                                    throw new IllegalArgumentException();
                                case '.':
                                    break m4;
                                case 0x20: case 0x09: case 0x0D: case 0x0A:
                                case ';':
                                    if (eos) {
                                        throw new IllegalArgumentException();
                                    }
                                    // FALLTHROUGH
                                case -1:
                                    wholeParts[numWholeParts++] = tmp;
                                    break m4;
                                }
                            }
                        }
                    }
                }
            }
        }

        m5: switch (current) {
        default:
            throw new IllegalArgumentException();
        case '.':
            wholeParts[numWholeParts++] = tmp;
            hasFractionPart = true;
            String frac = "0.";
            current = read();
            if (isTimeCountVal) {
                switch (current) {
                default:
                    throw new IllegalArgumentException();
                case '0': case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':
                    for (;;) {
                        frac = frac + (current - '0');
                        current = read();
                        switch (current) {
                        default:
                            break m5;
                        case 0x20: case 0x09: case 0x0D: case 0x0A: case ';':
                            if (eos) {
                                throw new IllegalArgumentException();
                            }
                            // FALLTHROUGH
                        case 'h': case 'm': case 's': case -1:
                            fractionPart = Float.parseFloat(frac);
                            break m5;
                        case '0': case '1': case '2': case '3': case '4':
                        case '5': case '6': case '7': case '8': case '9':
                        }
                    }
                }
            } else {
                switch (current) {
                default:
                    throw new IllegalArgumentException();
                case '0': case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':
                    for (;;) {
                        frac = frac + (current - '0');
                        current = read();
                        switch (current) {
                        default:
                            throw new IllegalArgumentException();
                        case 0x20: case 0x09: case 0x0D: case 0x0A: case ';':
                            if (eos) {
                                throw new IllegalArgumentException();
                            }
                            // FALLTHROUGH
                        case -1:
                            fractionPart = Float.parseFloat(frac);
                            break m5;
                        case '0': case '1': case '2': case '3': case '4':
                        case '5': case '6': case '7': case '8': case '9':
                        }
                    }
                }
            }
        case 0x20: case 0x09: case 0x0D: case 0x0A: case ';':
            if (eos) {
                throw new IllegalArgumentException();
            }
            // FALLTHROUGH
        case 'h': case 'm': case 's': case -1:
            break m5;
        }

        switch (current) {
        default:
            throw new IllegalArgumentException();
        case 'h':
            numWholeParts = 0; // so we don't fall into the seconds case below
            addHours(tmp);
            addMillis((int) (fractionPart * MILLIS_PER_HOUR));
            current = read();
            break;
        case 'm':
            numWholeParts = 0; // so we don't fall into the seconds case below
            current = read();
            switch (current) {
            case 'i':
                current = read();
                switch (current) {
                case 'n':
                    addMinutes(tmp);
                    addMillis((int) (fractionPart * MILLIS_PER_MINUTE));
                    current = read();
                    break;
                default:
                    throw new IllegalArgumentException();
                }
                break;
            case 's':
                addMillis(tmp);
                current = read();
                break;
            default:
                throw new IllegalArgumentException();
            }
            break;
        case 's':
            // seconds case is handled in the time count case below
            if (!hasFractionPart) {
                wholeParts[numWholeParts++] = tmp;
            }
            current = read();
            break;
        case 0x20: case 0x09: case 0x0D: case 0x0A: case ';':
            if (eos) {
                throw new IllegalArgumentException();
            }
            // FALLTHROUGH
        case -1:
            break;
        }

        if (eos) {
            skipSpaces();
            if (current != -1) {
                throw new IllegalArgumentException();
            }
        }

        switch (numWholeParts) {
        case 0: // time count was already handled above, just break
            break;
        case 1: // time count (seconds)
            addSeconds(wholeParts[0]);
            addMillis((int) (fractionPart * MILLIS_PER_SECOND));
            break;
        case 2: // partial clock value
            addMinutes(wholeParts[0]);
            addSeconds(wholeParts[1]);
            addMillis((int) (fractionPart * MILLIS_PER_SECOND));
            break;
        case 3: // full clock value
            addHours(wholeParts[0]);
            addMinutes(wholeParts[1]);
            addSeconds(wholeParts[2]);
            addMillis((int) (fractionPart * MILLIS_PER_SECOND));
            break;
        default:
            throw new IllegalArgumentException("wrong number of whole parts");
        }

        return millis;
    }

    /**
     * Adds the given number of hours to the total clock value.
     *
     * @param hours number of hours to add to the total clock value
     */
    private void addHours(final long hours) {
        millis += (hours * MILLIS_PER_HOUR);
    }

    /**
     * Adds the given number of minutes to the total clock value.
     *
     * @param minutes number of minutes to add to the total clock value
     */
    private void addMinutes(final long minutes) {
        millis += (minutes * MILLIS_PER_MINUTE);
    }

    /**
     * Adds the given number of seconds to the total clock value.
     *
     * @param seconds number of seconds to add to the total clock value
     */
    private void addSeconds(final long seconds) {
        millis += (seconds * MILLIS_PER_SECOND);
    }

    /**
     * Adds the given number of milliseconds to the total clock value.
     *
     * @param ms number of milliseconds to add to the total clock value
     */
    private void addMillis(final long ms) {
        millis += ms;
    }
}
