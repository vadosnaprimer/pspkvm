package com.nio;

public abstract class ShortBuffer extends Buffer {
	int array_offset;

	short[] backing_buffer;

	ShortBuffer(int capacity, int limit, int position, int mark) {
		super(capacity, limit, position, mark);
		array_offset = 0;
	}

	/**
	 * Allocates a new <code>ShortBuffer</code> object with a given capacity.
	 */
	public static ShortBuffer allocate(int capacity) {
		return new ShortBufferImpl(capacity);
	}

	/**
	 * Wraps a <code>short</code> array into a <code>ShortBuffer</code>
	 * object.
	 *
	 * @exception IndexOutOfBoundsException If the preconditions on the offset
	 * and length parameters do not hold
	 */
	public static final ShortBuffer wrap(short[] array, int offset, int length) {
		return new ShortBufferImpl(array, 0, array.length, offset + length,
				offset, -1, false);
	}

	/**
	 * Wraps a <code>short</code> array into a <code>ShortBuffer</code>
	 * object.
	 */
	public static final ShortBuffer wrap(short[] array) {
		return wrap(array, 0, array.length);
	}

	/**
	 * This method transfers <code>short</code>s from this buffer into the given
	 * destination array. Before the transfer, it checks if there are fewer than
	 * length <code>short</code>s remaining in this buffer. 
	 *
	 * @param dst The destination array
	 * @param offset The offset within the array of the first <code>short</code>
	 * to be written; must be non-negative and no larger than dst.length.
	 * @param length The maximum number of bytes to be written to the given array;
	 * must be non-negative and no larger than dst.length - offset.
	 *
	 * @exception BufferUnderflowException If there are fewer than length
	 * <code>short</code>s remaining in this buffer.
	 * @exception IndexOutOfBoundsException If the preconditions on the offset
	 * and length parameters do not hold.
	 */
	public ShortBuffer get(short[] dst, int offset, int length) {
		checkArraySize(dst.length, offset, length);
		checkForUnderflow(length);

		for (int i = offset; i < offset + length; i++) {
			dst[i] = get();
		}

		return this;
	}

	/**
	 * This method transfers <code>short</code>s from this buffer into the given
	 * destination array.
	 *
	 * @param dst The byte array to write into.
	 *
	 * @exception BufferUnderflowException If there are fewer than dst.length
	 * <code>short</code>s remaining in this buffer.
	 */
	public ShortBuffer get(short[] dst) {
		return get(dst, 0, dst.length);
	}

	/**
	 * Writes the content of the the <code>ShortBUFFER</code> src
	 * into the buffer. Before the transfer, it checks if there is fewer than
	 * <code>src.remaining()</code> space remaining in this buffer.
	 *
	 * @param src The source data.
	 *
	 * @exception BufferOverflowException If there is insufficient space in this
	 * buffer for the remaining <code>short</code>s in the source buffer.
	 * @exception IllegalArgumentException If the source buffer is this buffer.
	 * @exception ReadOnlyBufferException If this buffer is read-only.
	 */
	public ShortBuffer put(ShortBuffer src) {
		if (src == this)
			throw new IllegalArgumentException();

		checkForOverflow(src.remaining());

		if (src.remaining() > 0) {
			short[] toPut = new short[src.remaining()];
			src.get(toPut);
			put(toPut);
		}

		return this;
	}

	/**
	 * Writes the content of the the <code>short array</code> src
	 * into the buffer. Before the transfer, it checks if there is fewer than
	 * length space remaining in this buffer.
	 *
	 * @param src The array to copy into the buffer.
	 * @param offset The offset within the array of the first byte to be read;
	 * must be non-negative and no larger than src.length.
	 * @param length The number of bytes to be read from the given array;
	 * must be non-negative and no larger than src.length - offset.
	 * 
	 * @exception BufferOverflowException If there is insufficient space in this
	 * buffer for the remaining <code>short</code>s in the source array.
	 * @exception IndexOutOfBoundsException If the preconditions on the offset
	 * and length parameters do not hold
	 * @exception ReadOnlyBufferException If this buffer is read-only.
	 */
	public ShortBuffer put(short[] src, int offset, int length) {
		checkArraySize(src.length, offset, length);
		checkForOverflow(length);

		for (int i = offset; i < offset + length; i++)
			put(src[i]);

		return this;
	}

	/**
	 * Writes the content of the the <code>short array</code> src
	 * into the buffer.
	 *
	 * @param src The array to copy into the buffer.
	 * 
	 * @exception BufferOverflowException If there is insufficient space in this
	 * buffer for the remaining <code>short</code>s in the source array.
	 * @exception ReadOnlyBufferException If this buffer is read-only.
	 */
	public final ShortBuffer put(short[] src) {
		return put(src, 0, src.length);
	}

	/**
	 * Tells whether ot not this buffer is backed by an accessible
	 * <code>short</code> array.
	 */
	public final boolean hasArray() {
		return (backing_buffer != null && !isReadOnly());
	}

	/**
	 * Returns the <code>short</code> array that backs this buffer.
	 *
	 * @exception ReadOnlyBufferException If this buffer is read-only.
	 * @exception UnsupportedOperationException If this buffer is not backed
	 * by an accessible array.
	 */
	public final short[] array() {
		if (backing_buffer == null)
			throw new UnsupportedOperationException();

		checkIfReadOnly();

		return backing_buffer;
	}

	/**
	 * Returns the offset within this buffer's backing array of the first element.
	 *
	 * @exception ReadOnlyBufferException If this buffer is read-only.
	 * @exception UnsupportedOperationException If this buffer is not backed
	 * by an accessible array.
	 */
	public final int arrayOffset() {
		if (backing_buffer == null)
			throw new UnsupportedOperationException();

		checkIfReadOnly();

		return array_offset;
	}

	/**
	 * Calculates a hash code for this buffer.
	 *
	 * This is done with <code>int</code> arithmetic,
	 * where ** represents exponentiation, by this formula:<br>
	 * <code>s[position()] + 31 + (s[position()+1] + 30)*31**1 + ... +
	 * (s[limit()-1]+30)*31**(limit()-1)</code>.
	 * Where s is the buffer data. Note that the hashcode is dependent
	 * on buffer content, and therefore is not useful if the buffer
	 * content may change.
	 *
	 * @return the hash code
	 */
	public int hashCode() {
		int hashCode = get(position()) + 31;
		int multiplier = 1;
		for (int i = position() + 1; i < limit(); ++i) {
			multiplier *= 31;
			hashCode += (get(i) + 30) * multiplier;
		}
		return hashCode;
	}

	/**
	 * Checks if this buffer is equal to obj.
	 */
	public boolean equals(Object obj) {
		if (obj instanceof ShortBuffer) {
			return compareTo((ShortBuffer) obj) == 0;
		}

		return false;
	}

	/**
	 * Compares two <code>ShortBuffer</code> objects.
	 *
	 * @exception ClassCastException If obj is not an object derived from
	 * <code>ShortBuffer</code>.
	 */
	public int compareTo(Buffer other) {
		int num = Math.min(remaining(), other.remaining());
		int pos_this = position();
		int pos_other = other.position();

		for (int count = 0; count < num; count++) {
			short a = get(pos_this++);
			short b = ((ShortBuffer) other).get(pos_other++);

			if (a == b)
				continue;

			if (a < b)
				return -1;

			return 1;
		}

		return remaining() - other.remaining();
	}

	/**
	 * Returns the byte order of this buffer.
	 */
	public abstract ByteOrder order();

	/**
	 * Reads the <code>short</code> at this buffer's current position,
	 * and then increments the position.
	 *
	 * @exception BufferUnderflowException If there are no remaining
	 * <code>short</code>s in this buffer.
	 */
	public abstract short get();

	/**
	 * Writes the <code>short</code> at this buffer's current position,
	 * and then increments the position.
	 *
	 * @exception BufferOverflowException If there no remaining 
	 * <code>short</code>s in this buffer.
	 * @exception ReadOnlyBufferException If this buffer is read-only.
	 */
	public abstract ShortBuffer put(short b);

	/**
	 * Absolute get method.
	 *
	 * @exception IndexOutOfBoundsException If index is negative or not smaller
	 * than the buffer's limit.
	 */
	public abstract short get(int index);

	/**
	 * Absolute put method.
	 *
	 * @exception IndexOutOfBoundsException If index is negative or not smaller
	 * than the buffer's limit.
	 * @exception ReadOnlyBufferException If this buffer is read-only.
	 */
	public abstract ShortBuffer put(int index, short b);

	/**
	 * Compacts this buffer.
	 * 
	 * @exception ReadOnlyBufferException If this buffer is read-only.
	 */
	public abstract ShortBuffer compact();

	/**
	 * Tells wether or not this buffer is direct.
	 */
	public abstract boolean isDirect();

	/**
	 * Creates a new <code>ShortBuffer</code> whose content is a shared
	 * subsequence of this buffer's content.
	 */
	public abstract ShortBuffer slice();

	/**
	 * Creates a new <code>ShortBuffer</code> that shares this buffer's
	 * content.
	 */
	public abstract ShortBuffer duplicate();

	/**
	 * Creates a new read-only <code>ShortBuffer</code> that shares this
	 * buffer's content.
	 */
	public abstract ShortBuffer asReadOnlyBuffer();
}
