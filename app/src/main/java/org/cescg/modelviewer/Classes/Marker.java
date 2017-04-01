package org.cescg.modelviewer.Classes;

/**
 * Created by User on 01.04.2017..
 */

public class Marker {
    private float x;
    private float y;
    private float z;
    private String link;

    public Marker(float x, float y, float z, String link)
    {
        this.x=x;
        this.y=y;
        this.z=z;
        this.link=link;

    }
    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
