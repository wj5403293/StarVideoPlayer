package com.star.dlna.cast.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * DLNA 设备信息
 */
public class CastDevice implements Parcelable {

    private String id;
    private String name;
    private String ipAddress;
    private int port;
    private String descriptionUrl;
    private String manufacturer;
    private String modelName;
    private String modelNumber;
    private String udn;
    private long lastSeen;

    public CastDevice() {
    }

    protected CastDevice(Parcel in) {
        id = in.readString();
        name = in.readString();
        ipAddress = in.readString();
        port = in.readInt();
        descriptionUrl = in.readString();
        manufacturer = in.readString();
        modelName = in.readString();
        modelNumber = in.readString();
        udn = in.readString();
        lastSeen = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(ipAddress);
        dest.writeInt(port);
        dest.writeString(descriptionUrl);
        dest.writeString(manufacturer);
        dest.writeString(modelName);
        dest.writeString(modelNumber);
        dest.writeString(udn);
        dest.writeLong(lastSeen);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CastDevice> CREATOR = new Creator<CastDevice>() {
        @Override
        public CastDevice createFromParcel(Parcel in) {
            return new CastDevice(in);
        }

        @Override
        public CastDevice[] newArray(int size) {
            return new CastDevice[size];
        }
    };

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDescriptionUrl() {
        return descriptionUrl;
    }

    public void setDescriptionUrl(String descriptionUrl) {
        this.descriptionUrl = descriptionUrl;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public String getUdn() {
        return udn;
    }

    public void setUdn(String udn) {
        this.udn = udn;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    @NonNull
    @Override
    public String toString() {
        return "CastDevice{" +
                "name='" + name + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                '}';
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CastDevice that = (CastDevice) obj;
        return udn != null && udn.equals(that.udn);
    }

    @Override
    public int hashCode() {
        return udn != null ? udn.hashCode() : 0;
    }
}
