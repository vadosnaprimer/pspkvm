package com.nio;

final class FloatViewBufferImpl extends FloatBuffer {
	/** Position in bb (i.e. a byte offset) where this buffer starts. */
	private int offset;

	private ByteBuffer bb;

	private boolean readOnly;

	private ByteOrder endian;

	FloatViewBufferImpl(ByteBuffer bb, int capacity) {
		super(capacity, capacity, 0, -1);
		this.bb = bb;
		this.offset = bb.position();
		this.readOnly = bb.isReadOnly();
		this.endian = bb.order();

		// FIXME: Jacky
		// No Direct Buffer Support
		// if (bb.isDirect())
		//	this.address = VMDirectByteBuffer.adjustAddress(bb.address, offset);
	}

	public FloatViewBufferImpl(ByteBuffer bb, int offset, int capacity,
			int limit, int position, int mark, boolean readOnly,
			ByteOrder endian) {
		super(capacity, limit, position, mark);
		this.bb = bb;
		this.offset = offset;
		this.readOnly = readOnly;
		this.endian = endian;

		// FIXME: Jacky
		// No Direct Buffer Support
		// if (bb.isDirect())
		//	this.address = VMDirectByteBuffer.adjustAddress(bb.address, offset);
	}

	/**
	 * Reads the <code>float</code> at this buffer's current position,
	 * and then increments the position.
	 *
	 * @exception BufferUnderflowException If there are no remaining
	 * <code>floats</code> in this buffer.
	 */
	public float get() {
		int p = position();
		float result = ByteBufferHelper.getFloat(bb, (p << 2) + offset, endian);
		position(p + 1);
		return result;
	}

	/**
	 * Absolute get method. Reads the <code>float</code> at position
	 * <code>index</code>.
	 *
	 * @exception IndexOutOfBoundsException If index is negative or not smaller
	 * than the buffer's limit.
	 */
	public float get(int index) {
		return ByteBufferHelper.getFloat(bb, (index << 2) + offset, endian);
	}

	public FloatBuffer put(float value) {
		int p = position();
		ByteBufferHelper.putFloat(bb, (p << 2) + offset, value, endian);
		position(p + 1);
		return this;
	}

	public FloatBuffer put(int index, float value) {
		ByteBufferHelper.putFloat(bb, (index << 2) + offset, value, endian);
		return this;
	}

	public FloatBuffer compact() {
		if (position() > 0) {
			int count = limit() - position();
			bb.shiftDown(offset, offset + 4 * position(), 4 * count);
			position(count);
			limit(capacity());
		} else {
			position(limit());
			limit(capacity());
		}
		return this;
	}

	public FloatBuffer slice() {
		// Create a sliced copy of this object that shares its content.
		return new FloatViewBufferImpl(bb, (position() << 2) + offset,
				remaining(), remaining(), 0, -1, readOnly, endian);
	}

	FloatBuffer duplicate(boolean readOnly) {
		int pos = position();
		reset();
		int mark = position();
		position(pos);
		return new FloatViewBufferImpl(bb, offset, capacity(), limit(), pos,
				mark, readOnly, endian);
	}

	public FloatBuffer duplicate() {
		return duplicate(readOnly);
	}

	public FloatBuffer asReadOnlyBuffer() {
		return duplicate(true);
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public boolean isDirect() {
		return bb.isDirect();
	}

	public ByteOrder order() {
		return endian;
	}
}
