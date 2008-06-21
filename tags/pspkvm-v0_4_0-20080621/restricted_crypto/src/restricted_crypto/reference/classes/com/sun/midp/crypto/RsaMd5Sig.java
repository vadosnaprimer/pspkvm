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
 * Implements RSA MD5 Signatures.
 */ 
public final class RsaMd5Sig extends Signature {
    /**
     * Expected prefix in the decrypted result when MD5 hashing is used
     * with RSA signing. This prefix is followed by the MD5 hash.
     * If you are interested, more details are in the comments around 
     * the verify method in X509Certificate.
     */ 
    private static final byte[] PREFIX_MD5 = {    
        (byte) 0x30, (byte) 0x20, (byte) 0x30, (byte) 0x0c,
        (byte) 0x06, (byte) 0x08, (byte) 0x2a, (byte) 0x86,
        (byte) 0x48, (byte) 0x86, (byte) 0xf7, (byte) 0x0d,
        (byte) 0x02, (byte) 0x05, (byte) 0x05, (byte) 0x00,
        (byte) 0x04, (byte) 0x10
    };

    /** Common signature class. */
    RSASig rsaSig;

    /**
     * Constructs an RSA signature object that uses MD5 as 
     * message digest algorithm.
     *
     * @exception RuntimeException if MD5 is not available
     */
    public RsaMd5Sig() {
        try {
            rsaSig = new RSASig(PREFIX_MD5, MessageDigest.getInstance("MD5"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Needed algorithm not available");
        }
    }
    
    /**
     * Gets the signature algorithm.
     *
     * @return the algorithmimplemented by this signature object
     */ 
    public String getAlgorithm() {
        return "MD5withRSA";
    }
    
    /**
     * Gets the byte-length of the signature.
     * 
     * @return the byte-length of the signature produced by this object
     */ 
    public int getLength() {
        return rsaSig.getLength();
    }
    
    /**
     * Initializes the <CODE>RSASig</CODE> object with the appropriate
     * <CODE>Key</CODE> for signature verification.
     * 
     * @param theKey the key object to use for verification
     *
     * @exception InvalidKeyException if the key type is inconsistent 
     * with the mode or signature implementation.
     */
    public void initVerify(PublicKey theKey) throws InvalidKeyException {
        rsaSig.initVerify(theKey);
    }

    /**
     * Initializes the <CODE>RSASig</CODE> object with the appropriate
     * <CODE>Key</CODE> for signature creation.
     * 
     * @param theKey the key object to use for signing
     *
     * @exception InvalidKeyException if the key type is inconsistent 
     * with the mode or signature implementation.
     */
    public void initSign(PrivateKey theKey) throws InvalidKeyException {
        rsaSig.initSign(theKey);
    }

    /**
     * Accumulates a signature of the input data. When this method is used,
     * temporary storage of intermediate results is required. This method
     * should only be used if all the input data required for the signature
     * is not available in one byte array. The sign() or verify() method is 
     * recommended whenever possible. 
     *
     * @param inBuf the input buffer of data to be signed
     * @param inOff starting offset within the input buffer for data to
     *              be signed
     * @param inLen the byte length of data to be signed
     *
     * @exception SignatureException
     * if the signature algorithm does not pad the message and the
     * message is not block aligned
     *
     * @see #verify(byte[], int, int, byte[], int, short)
     */ 
    public void update(byte[] inBuf, int inOff, int inLen)
	throws SignatureException {

        rsaSig.update(inBuf, inOff, inLen);
    }

    /**
     * Generates the signature of all/last input data. A call to this
     * method also resets this signature object to the state it was in
     * when previously initialized via a call to init(). That is, the
     * object is reset and available to sign another message.
     * 
     * @param sigBuf the output buffer to store signature data
     * @param sigOff starting offset within the output buffer at which
     *               to begin signature data
     * @param sigLen max length the signature can be
     *
     * @return number of bytes of signature output in sigBuf
     *
     * @exception SignatureException 
     * if the signature algorithm does not pad the message and the
     * message is not block aligned
     */ 
    public int sign(byte[] sigBuf, int sigOff, int sigLen)
        throws SignatureException {

        return rsaSig.sign(sigBuf, sigOff, sigLen);
    }
    
    /**
     * Verifies the signature of all/last input data against the passed
     * in signature. A call to this method also resets this signature 
     * object to the state it was in when previously initialized via a
     * call to init(). That is, the object is reset and available to 
     * verify another message.
     * 
     * @param sigBuf the input buffer containing signature data
     * @param sigOff starting offset within the sigBuf where signature
     *               data begins
     * @param sigLen byte length of signature data
     *
     * @return true if signature verifies, false otherwise
     *
     * @exception SignatureException 
     * if the signature algorithm does not pad the message and the
     * message is not block aligned
     */ 
    public boolean verify(byte[] sigBuf, int sigOff, int sigLen)
	throws SignatureException {

        return rsaSig.verify(sigBuf, sigOff, sigLen);
    }
}
