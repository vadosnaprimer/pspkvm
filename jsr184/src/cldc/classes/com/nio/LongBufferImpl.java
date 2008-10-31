package com.nio;

final class LongBufferImpl extends LongBuffer {
	private boolean readOnly;

	LongBufferImpl(int capacity) {
		this(new long[capacity], 0, capacity, capacity, 0, -1, false);
	}

	LongBufferImpl(long[] buffer, int offset, int capacity, int limit,
			int position, int mark, boolean readOnly) {
		super(capacity, limit, position, mark);
		this.backing_buffer = buffer;
		this.array_offset = offset;
		this.readOnly = readOnly;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public LongBuffer slice() {
		return new LongBufferImpl(backing_buffer, array_offset + position(),
				remaining(), remaining(), 0, -1, isReadOnly());
	}

	public LongBuffer duplicate() {
		return new LongBufferImpl(backing_buffer, array_offset, capacity(),
				limit(), position(), mark, isReadOnly());
	}

	public LongBuffer asReadOnlyBuffer() {
		return new LongBufferImpl(backing_buffer, array_offset, capacity(),
				limit(), position(), mark, true);
	}

	public LongBuffer compact() {
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
	 * Reads the <code>long</code> at this buffer's current position,
	 * and then increments the position.
	 *
	 * @exception BufferUnderflowException If there are no remaining
	 * <code>longs</code> in this buffer.
	 */
	public long get() {
		checkForUnderflow();

		long result = backing_buffer[position()];
		position(position() + 1);
		return result;
	}

	/**
	 * Relative put method. Writes <code>value</code> to the next position
	 * in the buffer.
	 *
	 * @exception BufferOverflowException If there is insufficient space in this
	 * buffer.
	 * @exception ReadOnlyBufferException If this buffer is read-only.
	 */
	public LongBuffer put(long value) {
		checkIfReadOnly();
		checkForOverflow();

		backing_buffer[position()] = value;
		position(position() + 1);
		return this;
	}

	/**
	 * Absolute get method. Reads the <code>long</code> at position
	 * <code>index</code>.
	 *
	 * @exception IndexOutOfBoundsException If index is negative or not smaller
	 * than the buffer's limit.
	 */
	public long get(int index) {
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
	public LongBuffer put(int index, long value) {
		checkIfReadOnly();
		checkIndex(index);

		backing_buffer[index] = value;
		return this;
	}

	public ByteOrder order() {
		return ByteOrder.nativeOrder();
	}
}
