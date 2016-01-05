package com.eventshop.eventshoplinux.model;

/**
 * Created by nandhiniv on 5/20/15.
 */
public class STT<S, T, W, V> {

    Long _id;
    S loc;
    T timestamp;
    W theme;
    V value;
    String rawData;

    public STT(Long _id, S loc, T timestamp, W theme, V value, String rawData) {
        this._id = _id;
        this.loc = loc;
        this.timestamp = timestamp;
        this.theme = theme;
        this.value = value;
        this.rawData = rawData;
    }

    public STT(S loc, T timestamp, W theme, V value, String rawData) {
        this.loc = loc;
        this.timestamp = timestamp;
        this.theme = theme;
        this.value = value;
        this.rawData = rawData;
    }

    public STT() {

    }

    public Long get_id() {
        return _id;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }

    public S getLoc() {
        return loc;
    }

    public void setLoc(S loc) {
        this.loc = loc;
    }

    public T getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(T timestamp) {
        this.timestamp = timestamp;
    }

    public W getTheme() {
        return theme;
    }

    public void setTheme(W theme) {
        this.theme = theme;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public String getRawData() {
        return rawData;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

    @Override
    public String toString() {
        return "{" +
                "_id=" + _id +
                ", loc=" + loc +
                ", timestamp=" + timestamp +
                ", theme=" + theme +
                ", value=" + value +
                ", rawData='" + rawData + '\'' +
                '}';
    }
}
