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

package com.sun.midp.crypto;

/**
 * Implements the SHA-1 message digest algorithm.
 */ 
final class SHA extends MessageDigest {
    /*
     * The compute intensive operations are implemented in C
     * based on the OpenSSL SHA code (from pilotSSLeay). Here we
     * replicate the state the C code needs.
     */

    /** State needed for SHA hash. */
    private int[] state = new int[5]; // h0 ... h4
    /** State needed for SHA hash. */
    private int[] num = new int[1];
    /** State needed for SHA hash. */
    private int[] count = new int[2]; // Nl and Nh 
    /** State needed for SHA hash. */
    private int[] data = new int[16]; // data block 

    /** Create SHA digest object. */
    SHA() {
        reset();
    }

    /** 
     * Gets the message digest algorithm.
     * @return algorithm implemented by this MessageDigest object
     */
    public String getAlgorithm() {
        return "SHA-1";
    }

    /** 
     * Gets the length (in bytes) of the hash.
     * @return byte-length of the hash produced by this object
     */
    public int getDigestLength() {
        return 20;
    }

    /** 
     * Resets the MessageDigest to the initial state for further use.
     */
    public void reset() {
        // SHA1 initialization constants
        state[0] = 0x67452301;
        state[1] = 0xEFCDAB89;
        state[2] = 0x98BADCFE;
        state[3] = 0x10325476;
        state[4] = 0xC3D2E1F0;
        num[0] = 0;
        count[0] = count[1] = 0;
        for (int i = 0; i < data.length; i++)
             data[i] = 0;
    }

    /**
     * Accumulates a hash of the input data. This method is useful when
     * the input data to be hashed is not available in one byte array. 
     * @param inBuf input buffer of data to be hashed
     * @param inOff offset within inBuf where input data begins
     * @param inLen length (in bytes) of data to be hashed
     * @see #doFinal(byte[], int, int, byte[], int)
     */
    public void update(byte[] inBuf, int inOff, int inLen) {
	if (inLen == 0) {
	    return;
	}

        // check parameters to avoid a VM crash
        int test = inBuf[inOff] + inBuf[inLen - 1] + inBuf[inOff + inLen - 1];
        nativeUpdate(inBuf, inOff, inLen, state, num, count, data);
    }

    /**
     * Accumulates a hash of the input data. This method is useful when
     * the input data to be hashed is not available in one byte array. 
     * @param inBuf input buffer of data to be hashed
     * @param inOff offset within inBuf where input data begins
     * @param inLen length (in bytes) of data to be hashed
     * @param state internal hash state
     * @param num internal hash state
     * @param count internal hash state
     * @param data internal hash state
     */
    private static native void nativeUpdate(byte[] inBuf, int inOff, 
	     int inLen, int[] state, int[] num, int[] count, int[] data);


    /**
     * Completes the hash computation by performing final operations
     * such as padding. The digest is reset after this call is made.
     *
     * @param buf output buffer for the computed digest
     *
     * @param offset offset into the output buffer to begin storing the digest
     *
     * @param len number of bytes within buf allotted for the digest
     *
     * @return the number of bytes placed into <code>buf</code>
     * 
     * @exception DigestException if an error occurs.
     */
    public int digest(byte[] buf, int offset, int len) throws DigestException {
        if (len < getDigestLength()) {
            throw new DigestException("Buffer too short.");
        }

        // check the parameters to prevent a VM crash
        int test = buf[offset] + buf[offset + getDigestLength() - 1];
        
        nativeFinal(null, 0, 0, buf, offset, state, num, count, data);
        return getDigestLength();
    }

    /** 
     * Generates a hash of all/last input data. Completes and returns the
     * hash compuatation after performing final operations such as padding.
     * The MessageDigest object is reset after this call. 
     * @param inBuf input buffer of data to be hashed
     * @param inOff offset within inBuf where input data begins
     * @param inLen length (in bytes) of data to be hashed
     * @param outBuf output buffer where the hash should be placed
     * @param outOff offset within outBuf where the resulting hash begins
     * @param state internal hash state
     * @param num internal hash state
     * @param count internal hash state
     * @param data internal hash state
     */ 
    private static native void nativeFinal(byte[] inBuf, int inOff, 
		    int inLen, byte[] outBuf, int outOff,
		    int[] state, int[] num, int[] count, int[] data);

    /** 
     * Clones the MessageDigest object.
     * @return a clone of this object
     */
    public Object clone() {
	SHA cpy = new SHA();

	System.arraycopy(this.state, 0, cpy.state, 0, this.state.length);
	System.arraycopy(this.num, 0, cpy.num, 0, this.num.length);
	System.arraycopy(this.count, 0, cpy.count, 0, this.count.length);
	System.arraycopy(this.data, 0, cpy.data, 0, this.data.length);
	return cpy;
    }
}
