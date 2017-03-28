package org.cescg.modelviewer.Classes;

import com.google.api.client.util.DateTime;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by User on 28.03.2017..
 */

public class Scene extends RealmObject {
    @PrimaryKey
    private String sceneId;
    private String title;
    private String description;
    private String thumbnail;
    private String localPath;
    private String webContentLink;
    private Date createdDate;
    private Date modifiedTime;

    public String getSceneId() {
        return sceneId;
    }
    public void setSceneId(String sceneId) {
        this.sceneId = sceneId;
    }

    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocalPath() {
        return localPath;
    }

    public String getWebContentLink() {
        return webContentLink;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public void setWebContentLink(String webContentLink) {
        this.webContentLink = webContentLink;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }
    @Override
    public String toString() {
        return "projectId:"+this.sceneId+ " |title:"+this.title+ " |description:"+this.description+" |localPath:"+this.localPath+" |webContentLink:"+this.webContentLink;
        //+" |createdDate:" +this.createdDate+"| modifiedDate"+this.modifiedTime;
    }
}
