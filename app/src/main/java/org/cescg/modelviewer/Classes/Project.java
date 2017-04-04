package org.cescg.modelviewer.Classes;

import com.google.api.client.util.DateTime;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


public class Project extends RealmObject {

    @PrimaryKey
    private String projectId;
    private String title;
    private String localPath;
    private String webContentLink;
    private Date createdDate;
    private Date modifiedTime;
    private RealmList<Scene> scenes;

    public Project()
    {
        scenes=new RealmList<Scene>();
    }
    public String getProjectId() {
        return projectId;
    }

    public String getTitle() {
        return title;
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

    public void setProjectId(String projectId) {
        this.projectId = projectId;
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
    public RealmList<Scene> getScenes() {
        return scenes;
    }

    public void setScenes(RealmList<Scene> scenes) {
        this.scenes = scenes;
    }
    public void addScene(Scene scene)
    {

        this.scenes.add(scene);
    }

    @Override
    public String toString() {
        return "projectId:"+this.projectId+ " |title:"+this.title+" |localPath:"+this.localPath+" |webContentLink:"+this.webContentLink;
                //+" |createdDate:" +this.createdDate+"| modifiedDate"+this.modifiedTime;
    }
}

