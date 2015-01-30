package bugacity.com.bugacity.utils;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Joao on 27/01/2015.
 */
public class MarkerObject {

    String name;
    String descrition;
    String zone;
    String type;
    LatLng latlng;
    String image;

    public MarkerObject(){}

    public MarkerObject(String descrition, String name, String zone, String type,LatLng latlng, String image) {
        this.descrition = descrition;
        this.name = name;
        this.zone = zone;
        this.type = type;
        this.latlng = latlng;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescrition() {
        return descrition;
    }

    public void setDescrition(String descrition) {
        this.descrition = descrition;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LatLng getLatlng() {
        return latlng;
    }

    public void setLatlng(LatLng latlng) {
        this.latlng = latlng;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
