package vivekvaidya.com.lookingfor;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static android.R.attr.data;

/**
 * Created by Administrator on 2017/10/26.
 */

public class Event implements Parcelable {
    /**Event Data*/
    private String hostID;
    private String eventID;
    private String title;
    private String eventType;
    private String location;
    private String dateTime;
    private String description;
    private String[] attendeeID;

    public Event() {
    }
    /**Full Constructor*/
    public Event(String hostID, String eventID, String title, String eventType, String location, String dateTime, String description){
        this.hostID = hostID;
        this.eventID = eventID;
        this.title = title;
        this.eventType = eventType;
        this.location = location;
        this.dateTime = dateTime;
        this.description = description;
        this.attendeeID = new String[1];
        this.attendeeID[0] = hostID;
    }
    public Event(String hostID, String title, String eventType, String location, String dateTime, String description){
        this.hostID = hostID;
        this.title = title;
        this.eventID = null;
        this.eventType = eventType;
        this.location = location;
        this.dateTime = dateTime;
        this.description = description;
        this.attendeeID = new String[1];
        this.attendeeID[0] = hostID;
    }
    /**Getter and Setters*/
    public void setHostID(String hostID) {this.hostID = hostID;}
    public String getHostID(){
        return this.hostID;
    }
    public String getEventID(){
        return this.eventID;
    }
    public String getTitle(){
        return this.title;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public String getEventType(){
        return this.eventType;
    }
    public void setEventType(String eventType){
        this.eventType = eventType;
    }
    public String getLocation(){
        return this.location;
    }
    public void setLocation(String location){
        this.location = location;
    }
    public String getDateTime(){
        return this.dateTime;
    }
    public void setDateTime(String dateTime){
        this.dateTime = dateTime;
    }
    public String getDescription(){
        return this.description;
    }
    public void setDescription(String description){
        this.description = description;
    }
    public String[] getAttendeeID(){
        return this.attendeeID;
    }

    /**Someone tries to join.*/
    public boolean joinEvent(String UID){
        if (Arrays.asList(this.attendeeID).contains(UID)){
            return false;
        } else {
            String[] dummyArray = new String[this.attendeeID.length+1];
            for (int i = 0; i < this.attendeeID.length; i++){
                dummyArray[i] = this.attendeeID[i];
            }
            dummyArray[dummyArray.length-1] = UID;
            this.attendeeID = dummyArray;
            return true;
        }
    }
    /**Someone tries to leave*/
    public boolean leaveEvent(String UID){
        boolean inEvent = false;
        int IDLocation = 0;
        for (int i = 0; i < this.attendeeID.length; i++) {
            if (UID.equals(this.attendeeID[i])) {
                inEvent = true;
                this.attendeeID[i] = "";
                break;
            }
        }
        if (inEvent){
            String[] dummyArray = new String[this.attendeeID.length-1];
            boolean empty = false;
            for (int i = 0; i < dummyArray.length; i++){
                if (this.attendeeID[i].equals("")){
                    empty = true;
                }
                if (empty){
                    dummyArray[i] = attendeeID[i+1];
                } else {
                    dummyArray[i] = attendeeID[i];
                }
            }
            return true;
        } else {
            return false;
        }
    }

    //TODO:What is this function?
    @Override
    public int describeContents() {
        return 0;
    }


    /**Interface "Parcelable" required function*/
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(hostID);
        dest.writeString(eventID);
        dest.writeString(title);
        dest.writeString(eventType);
        dest.writeString(location);
        dest.writeString(dateTime);
        dest.writeString(description);
        dest.writeStringArray(attendeeID);
    }
    /**Interface "Parcelable" required function*/
    private Event(Parcel in) {
        hostID = in.readString();
        eventID = in.readString();
        title = in.readString();
        eventType = in.readString();
        location = in.readString();
        dateTime = in.readString();
        description = in.readString();
        attendeeID = in.createStringArray();
    }
    /**Interface "Parcelable" required function*/
    public static final Parcelable.Creator<Event> CREATOR
            = new Parcelable.Creator<Event>() {
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    /**Push event to Firebase.*/
    public void pushToFirebase(OnCompleteListener<Void> onCompleteListener){
        HashMap<String, Object> dataMap = new HashMap<>();
        dataMap.put("title", getTitle());
        dataMap.put("eventType", getEventType());
        dataMap.put("location", getLocation());
        dataMap.put("dateTime", getDateTime());
        dataMap.put("description", getDescription());
        dataMap.put("hostID", getHostID());
        List attendeeID = new ArrayList<>(Arrays.asList(getAttendeeID()));
        dataMap.put("attendeeID", attendeeID);

        DatabaseReference newEventReference = FirebaseDatabase.getInstance().getReference().child("events")
                .child("storage").push();
        newEventReference.setValue(dataMap).addOnCompleteListener(onCompleteListener);
    }

    @Override
    public String toString() {
        return title == null ? "No title" : title;
    }

    public static ArrayList<Event> searchForEvent(ArrayList<Event> events, String query){
        query = query.toLowerCase();
        ArrayList<Event> newList = new ArrayList<Event>();
        int count = 0;
        for (int i = 0; i < events.size(); i++){
            if ((events.get(i).getDescription().toLowerCase().contains(query))
                    || (events.get(i).getTitle().toLowerCase().contains(query))
                    || (events.get(i).getLocation().toLowerCase().contains(query))
                    || (events.get(i).getEventType().toLowerCase().contains(query))){
                newList.add(events.get(i));
                count++;
            }
        }
        if (count != 0){
            return newList;
        } else {
            newList.add(new Event("Sorry", "We", "Couldn't", "Find", "Any", "Matching", "Event"));
            return newList;
        }
    }
}
