package com.nio;

final class FloatBufferImpl extends FloatBuffer {
	private boolean readOnly;

	FloatBufferImpl(int capacity) {
		this(new float[capacity], 0, capacity, capacity, 0, -1, false);
	}

	FloatBufferImpl(float[] buffer, int offset, int capacity, int limit,
			int position, int mark, boolean readOnly) {
		super(capacity, limit, position, mark);
		this.backing_buffer = buffer;
		this.array_offset = offset;
		this.readOnly = readOnly;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public FloatBuffer slice() {
		return new FloatBufferImpl(backing_buffer, array_offset + position(),
				remaining(), remaining(), 0, -1, isReadOnly());
	}

	public FloatBuffer duplicate() {
		return new FloatBufferImpl(backing_buffer, array_offset, capacity(),
				limit(), position(), mark, isReadOnly());
	}

	public FloatBuffer asReadOnlyBuffer() {
		return new FloatBufferImpl(backing_buffer, array_offset, capacity(),
				limit(), position(), mark, true);
	}

	public FloatBuffer compact() {
		checkIfReadOnly();
		mark = -1;
		int p = position();
		int n = limit() - p;
		if (n > 0) {
			System.arraycopy(backing_buffer, array_offset + p, backing_buffer,
					array_offset, n);
		}
		position(n);
		limit(capacity());
		return this;
	}

	public boolean isDirect() {
		return false;
	}

	/**
	 * Reads the <code>float</code> at this buffer's current position,
	 * and then increments the position.
	 *
	 * @exception BufferUnderflowException If there are no remaining
	 * <code>floats</code> in this buffer.
	 */
	public float get() {
		checkForUnderflow();

		float result = backing_buffer[position()];
		position(position() + 1);
		return result;
	}

	/**
	 * Relative put method. Writes <code>value</code> to the next position
	 * in the buffer.
	 * 
	 * @exception BufferOverflowException If there no remaining 
	 * space in this buffer.
	 * @exception ReadOnlyBufferException If this buffer is read-only.
	 */
	public FloatBuffer put(float value) {
		checkIfReadOnly();
		checkForOverflow();

		backing_buffer[position()] = value;
		position(position() + 1);
		return this;
	}

	/**
	 * Absolute get method. Reads the <code>float</code> at position
	 * <code>index</code>.
	 *
	 * @exception IndexOutOfBoundsException If index is negative or not smaller
	 * than the buffer's limit.
	 */
	public float get(int index) {
		checkIndex(index);

		return backing_buffer[index];
	}

	/**
	 * Absolute put method. Writes <code>value</code> to position
	 * <code>index</code> in the buffer.
	 *
	 * @exception IndexOutOfBoundsException If index is negative or not smaller
	 * than the buffer's limit.
	 * @exception ReadOnlyBufferException If this buffer is read-only.
	 */
	public FloatBuffer put(int index, float value) {
		checkIfReadOnly();
		checkIndex(index);

		backing_buffer[index] = value;
		return this;
	}

	public ByteOrder order() {
		return ByteOrder.nativeOrder();
	}
}
