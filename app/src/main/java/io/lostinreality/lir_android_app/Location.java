package io.lostinreality.lir_android_app;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jose on 30/05/16.
 */
public class Location implements Parcelable {

    public Long id;
    public Double latitude;
    public Double longitude;
    public Double radius;
    public Double zoom;
    public String name;
    public Boolean ismain;

    public Location(Double lat,Double lng, String nme, Double rd, Double zm) {
        this.latitude = lat;
        this.longitude = lng;
        this.radius = rd;
        this.zoom = zm;
        this.name = nme;
    }

    public Location() {
        this.latitude = null;
        this.longitude = null;
        this.radius = null;
        this.zoom = null;
        this.name = null;
    }

    public void setLocation(Location l) {
        this.latitude = l.latitude;
        this.longitude = l.longitude;
        this.radius = l.radius;
        this.zoom = l.zoom;
        this.name = l.name;
        this.ismain = l.ismain;
    }

    public void setAsMainStoryLocation(Boolean bol) {
        this.ismain = bol;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeStringArray(new String[] {
                this.latitude.toString(),
                this.longitude.toString(),
                this.name,
                this.zoom.toString(),
                this.radius.toString(),
                (this.id != null) ? this.id.toString() : "",
                (this.ismain != null) ? this.ismain.toString() : "",
        });
    }

    public static final Parcelable.Creator<Location> CREATOR
            = new Parcelable.Creator<Location>() {
        public Location createFromParcel(Parcel in) {
            return new Location(in);
        }

        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

    private Location(Parcel in) {
        String[] data = new String[7];

        in.readStringArray(data);
        this.latitude = Double.parseDouble(data[0]);
        this.longitude = Double.parseDouble(data[1]);
        this.name = data[2];
        this.zoom = Double.parseDouble(data[3]);
        this.radius = Double.parseDouble(data[4]);

        if (!data[5].equals(""))
            this.id = Long.parseLong(data[5]);

        if (!data[6].equals(""))
            this.ismain = Boolean.parseBoolean(data[6]);
    }
}
