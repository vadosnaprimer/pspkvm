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
package com.sun.j2me.location;

import java.io.*;
import javax.microedition.location.*;

/**
 * This class is an implementation of the <code>Landmark</code> class defined
 * by the JSR-179 specification.
 */
// JAVADOC COMMENT ELIDED
public class LandmarkImpl {
    // JAVADOC COMMENT ELIDED
    String[] categories = new String[0];
    // JAVADOC COMMENT ELIDED
    int recordId = -2;
    // JAVADOC COMMENT ELIDED    
    String storeName = null;
    // JAVADOC COMMENT ELIDED
    String name;
    // JAVADOC COMMENT ELIDED
    String description;
    // JAVADOC COMMENT ELIDED    
    QualifiedCoordinates coordinates;
    // JAVADOC COMMENT ELIDED    
    AddressInfo addressInfo;

    // JAVADOC COMMENT ELIDED
    public LandmarkImpl(String name, String description, 
		    QualifiedCoordinates coordinates,
		    AddressInfo addressInfo) {
        setName(name);
        this.description = description;
        this.coordinates = coordinates;
        this.addressInfo = addressInfo;
    }

    // JAVADOC COMMENT ELIDED
    LandmarkImpl(LandmarkImpl lm) {
        this(lm.serialize(), lm.recordId, lm.storeName);
    }

    // JAVADOC COMMENT ELIDED
    LandmarkImpl(byte[] serializedForm, int recordId, String storeName) {
        try {
            this.recordId = recordId;
            this.storeName = storeName;
            DataInputStream stream = new 
		DataInputStream(new ByteArrayInputStream(serializedForm));
            if (stream.readBoolean()) {
                int field;
                addressInfo = new AddressInfo();
                while ((field = stream.readInt()) != 0) {
                    addressInfo.setField(field, stream.readUTF());
                }
            }
            if (stream.readBoolean()) {
                float verticalAccuracy = stream.readFloat();
                float horizontalAccuracy = stream.readFloat();
                float altitude = stream.readFloat();
                double longitude = stream.readDouble();
                double latitude = stream.readDouble();
                coordinates = new QualifiedCoordinates(latitude, longitude,
						       altitude,
						       horizontalAccuracy,
						       verticalAccuracy);
            }
            if (stream.readBoolean()) {
                description = stream.readUTF();
            }
            if (stream.readBoolean()) {
                name = stream.readUTF();
            }
            int categoryCount = stream.readInt();
            categories = new String[categoryCount];
            for (int iter = 0; iter < categoryCount; iter++) {
                if (stream.readBoolean()) {
                    categories[iter] = stream.readUTF();
                }
            }
            stream.close();
        } catch (IOException err) {
            // this should never occur
            err.printStackTrace();
        }
    }

    // JAVADOC COMMENT ELIDED
    byte[] serialize() {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream(100);
            DataOutputStream data = new DataOutputStream(stream);
            if (addressInfo != null) {
                data.writeBoolean(true);
                for (int i = AddressInfo.EXTENSION; 
                    i <= AddressInfo.PHONE_NUMBER; i++) {
                    String info = addressInfo.getField(i);
                    if (info != null) {
                        data.writeInt(i);
                        data.writeUTF(info);
                    }
                }
                data.writeInt(0);
            } else {
                data.writeBoolean(false);
            }
            if (coordinates == null) {
                data.writeBoolean(false);
            } else {
                data.writeBoolean(true);
                data.writeFloat(coordinates.getVerticalAccuracy());
                data.writeFloat(coordinates.getHorizontalAccuracy());
                data.writeFloat(coordinates.getAltitude());
                data.writeDouble(coordinates.getLongitude());
                data.writeDouble(coordinates.getLatitude());
            }
            writePossiblyNull(data, description);
            writePossiblyNull(data, name);
            data.writeInt(categories.length);
            for (int iter = 0; iter < categories.length; iter++) {
                writePossiblyNull(data, categories[iter]);
            }
            byte[] returnVal = stream.toByteArray();
            data.close();
            return returnVal;
        } catch (IOException err) {
            // this should never occur
            err.printStackTrace();
            return null;
        }
    }

    // JAVADOC COMMENT ELIDED
    private void writePossiblyNull(DataOutputStream data, String value)
	throws IOException {
            if (value == null) {
                data.writeBoolean(false);
            } else {
                data.writeBoolean(true);
                data.writeUTF(value);
            }
    }

    // JAVADOC COMMENT ELIDED
    int getRecordId() {
        return recordId;
    }

    // JAVADOC COMMENT ELIDED
    void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    // JAVADOC COMMENT ELIDED
    String getStoreName() {
        return storeName;
    }

    // JAVADOC COMMENT ELIDED
    void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    // JAVADOC COMMENT ELIDED
    String[] getCategories() {
        return categories;
    }

    // JAVADOC COMMENT ELIDED
    void removeCategories() {
	categories = new String[0];
    }

    // JAVADOC COMMENT ELIDED
    void addCategory(String category) {
        String[] categories = new String[this.categories.length + 1];
        System.arraycopy(this.categories, 0, categories, 0, 
			 this.categories.length);
        this.categories = categories;
        categories[categories.length - 1] = category;
    }

    // JAVADOC COMMENT ELIDED
    void removeCategory(String category) {
        if (category == null) {
            category = "";
        }
        String[] categories = new String[this.categories.length - 1];
        int count = 0;
        for (int iter = 0; iter < this.categories.length; iter++) {
            if (!category.equals(this.categories[iter])) {
                if (count == categories.length) {
                    // the category to remove is not within the array
                    return;
                }
                categories[count] = this.categories[iter];
                count++;
            }
        }
        this.categories = categories;
    }

    // JAVADOC COMMENT ELIDED    
    boolean isInCategory(String category) {
        if (category == null) {
            category = "";
        }
        for (int iter = 0; iter < categories.length; iter++) {
            if (category.equals(categories[iter])) {
                return true;
            }
        }
        return false;
    }

    // JAVADOC COMMENT ELIDED
    public String getName() {
        return name;
    }

    // JAVADOC COMMENT ELIDED
    public String getDescription() {
        return description;
    }

    // JAVADOC COMMENT ELIDED
    public QualifiedCoordinates getQualifiedCoordinates() {
        return coordinates;
    }

    // JAVADOC COMMENT ELIDED
    public AddressInfo getAddressInfo() {
        return addressInfo;
    }

    // JAVADOC COMMENT ELIDED
    public void setName(String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        this.name = name;
    }

    // JAVADOC COMMENT ELIDED
    public void setDescription(String description) {
        this.description = description;
    }

    // JAVADOC COMMENT ELIDED
    public void setQualifiedCoordinates(QualifiedCoordinates coordinates) {
        this.coordinates = coordinates;
    }

    // JAVADOC COMMENT ELIDED
    public void setAddressInfo(AddressInfo addressInfo) {
        this.addressInfo = addressInfo;
    }

    // JAVADOC COMMENT ELIDED
    String asString() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < categories.length; i++) {
            if (i > 0) buf.append("; ");
            buf.append(categories[i]);
        }
        String coordinates;
        if (this.coordinates != null) {
            coordinates = "Lat: " + this.coordinates.getLatitude() +
		" Lon: " + this.coordinates.getLongitude();
        } else {
            coordinates = "null";
        }
        return "Landmark: { storeName = " + storeName +
	    " recordId = " + recordId +
	    " name = " + name +
	    " description = " + description +
	    " coordinates = " + coordinates +
	    " addressInfo = " + addressInfo +
            " categories = " + buf.toString() + " }";
    }

    // JAVADOC COMMENT ELIDED
    public boolean equals(Object o) {
        if (!(o instanceof LandmarkImpl)) {
            return false;
        }
        if (this == o) {
            return true;
        }
        LandmarkImpl lm = (LandmarkImpl)o;
        boolean idEquals = recordId == lm.recordId;
        boolean storeEquals = (storeName == lm.storeName) ||
	    ((storeName != null) && storeName.equals(lm.storeName));
        return idEquals && storeEquals;
    }
}
