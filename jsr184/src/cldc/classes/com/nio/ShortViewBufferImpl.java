package com.nio;

final class ShortViewBufferImpl extends ShortBuffer {
	/** Position in bb (i.e. a byte offset) where this buffer starts. */
	private int offset;

	private ByteBuffer bb;

	private boolean readOnly;

	private ByteOrder endian;

	ShortViewBufferImpl(ByteBuffer bb, int capacity) {
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

	public ShortViewBufferImpl(ByteBuffer bb, int offset, int capacity,
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
	 * Reads the <code>short</code> at this buffer's current position,
	 * and then increments the position.
	 *
	 * @exception BufferUnderflowException If there are no remaining
	 * <code>short</code>s in this buffer.
	 */
	public short get() {
		int p = position();
		short result = ByteBufferHelper.getShort(bb, (p << 1) + offset, endian);
		position(p + 1);
		return result;
	}

	/**
	 * Absolute get method. Reads the <code>short</code> at position
	 * <code>index</code>.
	 *
	 * @exception IndexOutOfBoundsException If index is negative or not smaller
	 * than the buffer's limit.
	 */
	public short get(int index) {
		return ByteBufferHelper.getShort(bb, (index << 1) + offset, endian);
	}

	public ShortBuffer put(short value) {
		int p = position();
		ByteBufferHelper.putShort(bb, (p << 1) + offset, value, endian);
		position(p + 1);
		return this;
	}

	public ShortBuffer put(int index, short value) {
		ByteBufferHelper.putShort(bb, (index << 1) + offset, value, endian);
		return this;
	}

	public ShortBuffer compact() {
		if (position() > 0) {
			int count = limit() - position();
			bb.shiftDown(offset, offset + 2 * position(), 2 * count);
			position(count);
			limit(capacity());
		} else {
			position(limit());
			limit(capacity());
		}
		return this;
	}

	public ShortBuffer slice() {
		// Create a sliced copy of this object that shares its content.
		return new ShortViewBufferImpl(bb, (position() << 1) + offset,
				remaining(), remaining(), 0, -1, readOnly, endian);
	}

	ShortBuffer duplicate(boolean readOnly) {
		int pos = position();
		reset();
		int mark = position();
		position(pos);
		return new ShortViewBufferImpl(bb, offset, capacity(), limit(), pos,
				mark, readOnly, endian);
	}

	public ShortBuffer duplicate() {
		return duplicate(readOnly);
	}

	public ShortBuffer asReadOnlyBuffer() {
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
