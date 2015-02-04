package zetbrush.com.generatingmain;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Arman on 2/3/15.
 */
public class Effects implements Parcelable {
    String name;

    public Effects(String nm){
    this.name = nm;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
    }

    private Effects(Parcel in) {
        this.name = in.readString();

    }

    public static final Parcelable.Creator<Effects> CREATOR = new Parcelable.Creator<Effects>() {
        public Effects createFromParcel(Parcel source) {
            return new Effects(source);
        }

        public Effects[] newArray(int size) {
            return new Effects[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String title) {
        this.name = title;
    }

    @Override
    public String toString() {
        return name;
    }


}
