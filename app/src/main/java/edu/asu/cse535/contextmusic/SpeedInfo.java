package edu.asu.cse535.contextmusic;

/**
 * Created by Shashank on 11/24/2016.
 */

public class SpeedInfo {
    float speed;

    public SpeedInfo(String speed) {
        this.speed = Float.parseFloat(speed);

    }

    public String getDrivingStyle() {
        if(speed > 70) {
            return "sport driving";
        }
        else {
            return "relaxed driving";
        }
    }
}
