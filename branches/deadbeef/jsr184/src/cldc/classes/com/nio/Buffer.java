package com.nio;

public abstract class Buffer {

	int cap = 0;

	int limit = 0;

	int pos = 0;

	int mark = -1;

	Buffer(int capacity, int limit, int position, int mark) {
		if (capacity < 0)
			throw new IllegalArgumentException();

		cap = capacity;
		limit(limit);
		position(position);

		if (mark >= 0) {
			if (mark > pos)
				throw new IllegalArgumentException();
			this.mark = mark;
		}
	}

	public final int capacity() {
		return cap;
	}

	public final int position() {
		return pos;
	}

	public final Buffer position(int newPosition) {
		if ((newPosition < 0) || (newPosition > limit))
			throw new IllegalArgumentException();

		if (newPosition <= mark)
			mark = -1;

		pos = newPosition;
		return this;
	}

	public final int limit() {
		return limit;
	}

	public final Buffer limit(int newLimit) {
		if ((newLimit < 0) || (newLimit > cap))
			throw new IllegalArgumentException();

		if (newLimit < mark)
			mark = -1;

		if (pos > newLimit)
			pos = newLimit;

		limit = newLimit;
		return this;
	}

	public final Buffer mark() {
		mark = pos;
		return this;
	}

	/**
	 * InvalidMarkException has been changed to IllegalStateException
	 * @return
	 */
	public final Buffer reset() {
		if (mark == -1)
			throw new IllegalStateException();

		pos = mark;
		return this;
	}

	public final Buffer clear() {
		limit = cap;
		pos = 0;
		mark = -1;
		return this;
	}

	public final Buffer flip() {
		limit = pos;
		pos = 0;
		mark = -1;
		return this;
	}

	public final Buffer rewind() {
		pos = 0;
		mark = -1;
		return this;
	}

	public final int remaining() {
		return limit - pos;
		;
	}

	public final boolean hasRemaining() {
		return remaining() > 0;
	}

	public abstract boolean isReadOnly();

	public abstract int compareTo(Buffer other);


	/**
	 * Checks for underflow. This method is used internally to check
	 * whether a buffer has enough elements left to satisfy a read 
	 * request.
	 *
	 * @exception BufferUnderflowException If there are no remaining
	 * elements in this buffer.
	 */
	final void checkForUnderflow() {
		if (!hasRemaining())
			throw new BufferUnderflowException();
	}

	/**
	 * Checks for underflow. This method is used internally to check
	 * whether a buffer has enough elements left to satisfy a read 
	 * request for a given number of elements.
	 *
	 * @param length The length of a sequence of elements.
	 *
	 * @exception BufferUnderflowException If there are not enough 
	 * remaining elements in this buffer.
	 */
	final void checkForUnderflow(int length) {
		if (remaining() < length)
			throw new BufferUnderflowException();
	}

	/**
	 * Checks for overflow. This method is used internally to check
	 * whether a buffer has enough space left to satisfy a write 
	 * request.
	 *
	 * @exception BufferOverflowException If there is no remaining
	 * space in this buffer.
	 */
	final void checkForOverflow() {
		if (!hasRemaining())
			throw new BufferOverflowException();
	}

	/**
	 * Checks for overflow. This method is used internally to check
	 * whether a buffer has enough space left to satisfy a write 
	 * request for a given number of elements.
	 *
	 * @param length The length of a sequence of elements.
	 *
	 * @exception BufferUnderflowException If there is not enough 
	 * remaining space in this buffer.
	 */
	final void checkForOverflow(int length) {
		if (remaining() < length)
			throw new BufferOverflowException();
	}

	/**
	 * Checks if index is negative or not smaller than the buffer's 
	 * limit. This method is used internally to check whether
	 * an indexed request can be fulfilled.
	 *
	 * @param index The requested position in the buffer.
	 *
	 * @exception IndexOutOfBoundsException If index is negative or not smaller
	 * than the buffer's limit.
	 */
	final void checkIndex(int index) {
		if (index < 0 || index >= limit())
			throw new IndexOutOfBoundsException();
	}

	/**
	 * Checks if buffer is read-only. This method is used internally to
	 * check if elements can be put into a buffer.
	 *
	 * @exception ReadOnlyBufferException If this buffer is read-only.
	 */
	final void checkIfReadOnly() {
		if (isReadOnly())
			throw new UnsupportedOperationException();
	}

	/**
	 * Checks whether an array is large enough to hold the given number of
	 * elements at the given offset. This method is used internally to
	 * check if an array is big enough.
	 *
	 * @param arraylength The length of the array.
	 * @param offset The offset within the array of the first byte to be read;
	 * must be non-negative and no larger than arraylength.
	 * @param length The number of bytes to be read from the given array;
	 * must be non-negative and no larger than arraylength - offset.
	 *
	 * @exception IndexOutOfBoundsException If the preconditions on the offset
	 * and length parameters do not hold
	 */
	static final void checkArraySize(int arraylength, int offset, int length) {
		if ((offset < 0) || (length < 0) || (arraylength < length + offset))
			throw new IndexOutOfBoundsException();
	}

//	/**
//	 * Returns the backing array of this buffer, if this buffer has one.
//	 * Modification to the array are directly visible in this buffer and vice
//	 * versa.
//	 *
//	 * <p>
//	 * If this is a read-only buffer, then a {@link ReadOnlyBufferException} is
//	 * thrown because exposing the array would allow to circumvent the read-only
//	 * property. If this buffer doesn't have an array, then an
//	 * {@link UnsupportedOperationException} is thrown. Applications should check
//	 * if this buffer supports a backing array by calling {@link #hasArray}
//	 * first.</p>
//	 *
//	 * @return the backing array of this buffer
//	 *
//	 * @throws ReadOnlyBufferException when this buffer is read only
//	 * @throws UnsupportedOperationException when this buffer does not provide
//	 *         a backing array
//	 *
//	 * @since 1.6
//	 */
//	public abstract byte[] array();

	/**
	 * Returns <code>true</code> if this buffer can provide a backing array,
	 * <code>false</code> otherwise. When <code>true</code>, application code
	 * can call {@link #array()} to access this backing array.
	 *
	 * @return <code>true</code> if this buffer can provide a backing array,
	 *         <code>false</code> otherwise
	 *
	 * @since 1.6
	 */
	public abstract boolean hasArray();

	/**
	 * For buffers that are backed by a Java array, this returns the offset
	 * into that array at which the buffer content starts.
	 *
	 * @return the offset into the backing array at which the buffer content
	 *         starts
	 * @throws ReadOnlyBufferException when this buffer is read only
	 * @throws UnsupportedOperationException when this buffer does not provide
	 *         a backing array
	 *
	 * @since 1.6
	 */
	public abstract int arrayOffset();

	/**
	 * Returns <code>true</code> when this buffer is direct, <code>false</code>
	 * otherwise. A direct buffer is usually backed by a raw memory area instead
	 * of a Java array.
	 *
	 * @return <code>true</code> when this buffer is direct, <code>false</code>
	 *         otherwise
	 *
	 * @since 1.6
	 */
	public abstract boolean isDirect();

}
