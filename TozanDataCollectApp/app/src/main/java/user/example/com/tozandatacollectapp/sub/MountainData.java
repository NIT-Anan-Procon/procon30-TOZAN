package user.example.com.tozandatacollectapp.sub;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.util.Objects;

public class MountainData implements Comparable<MountainData>, Parcelable {
    private int cId, pId, mId;
    private String cName, pName, mName;

    public MountainData(int cId, String cName, int pId, String pName, int mId, String mName){
        this.cId = cId;
        this.cName = cName;
        this.pId = pId;
        this.pName = pName;
        this.mId = mId;
        this.mName = mName;
    }

    protected MountainData(Parcel in) {
        cId = in.readInt();
        pId = in.readInt();
        mId = in.readInt();
        cName = in.readString();
        pName = in.readString();
        mName = in.readString();
    }

    public static final Creator<MountainData> CREATOR = new Creator<MountainData>() {
        @Override
        public MountainData createFromParcel(Parcel in) {
            return new MountainData(in);
        }

        @Override
        public MountainData[] newArray(int size) {
            return new MountainData[size];
        }
    };

    public int getcId() {
        return cId;
    }

    public String getcName() {
        return cName;
    }

    public int getpId() {
        return pId;
    }

    public String getpName() {
        return pName;
    }

    public int getmId() {
        return mId;
    }

    public String getmName() {
        return mName;
    }

    @Override
    public int compareTo(MountainData mData) {
        int i = Integer.compare(getmId(), mData.getmId());
        if(i != 0) return i;
        i = Integer.compare(getpId(), mData.getpId());
        if(i != 0) return i;
        return Integer.compare(getcId(), mData.getcId());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj == null) return false;
        if(!(obj instanceof MountainData)) return false;
        MountainData mData = (MountainData) obj;
        return compareTo(mData) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cId, cName, pId, pName, mId, mName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(cId);
        parcel.writeInt(pId);
        parcel.writeInt(mId);
        parcel.writeString(cName);
        parcel.writeString(pName);
        parcel.writeString(mName);
    }

    @Override
    public String toString() {
        return mName;
    }
}
