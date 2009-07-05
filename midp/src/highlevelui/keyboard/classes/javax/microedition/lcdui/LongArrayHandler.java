/*
 * /AJ Milne--custom semichordal version for PSP platform
 * Helper for turning arrays of 64 bit longs to byte arrays, for processing
 * images stored in these arrays -- we use longs because it's more compact in storage
 * than bytes or ints, and also allows larger images without segmenting.
 */

package javax.microedition.lcdui;

import com.sun.midp.lcdui.*;

class LongArrayHandler {

/**
 *	Create an image from an array of longs creating a byte stream representing
 *	a legal format for createImage (so far, just used for encoded .pngs)
 *	
 * @param a the array of longs
 * @param p the amount in bytes of padding to ignore at the end of the array
 * 
 * @ret the created image
 */       
public static Image createImage(long[] a, int p) {
	// Get the byte count
	int c = (a.length * 8) - p;
	// Create a temporary byte array to hold the data
	byte[] r = new byte[c];
	// Pack the array
	for(int i=0; i<c; i++) {
		r[i] = (byte)(a[i/8] >> ((i%8)*8)); }
	// Create and return the image
	return Image.createImage(r, 0, c); }
}	
