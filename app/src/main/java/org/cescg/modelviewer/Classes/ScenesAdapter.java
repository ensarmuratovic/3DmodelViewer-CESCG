package org.cescg.modelviewer.Classes;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import org.cescg.modelviewer.LaunchActivity;
import org.cescg.modelviewer.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Created by User on 29.03.2017..
 */

public class ScenesAdapter extends RealmBaseAdapter<Scene> implements ListAdapter {
    private static final java.lang.String TAG ="ENSAR" ;
    private LaunchActivity launcAct;
    private boolean inDeletionMode = false;
    private Set<Integer> countersToDelete = new HashSet<Integer>();
    public ScenesAdapter(OrderedRealmCollection<Scene> realmResults)
    {
        super(realmResults);
    }
    public void setBaseActivity(LaunchActivity activity)
    {
        launcAct=activity;
    }

    void enableDeletionMode(boolean enabled) {
        inDeletionMode = enabled;
        if (!enabled) {
            countersToDelete.clear();
        }
        notifyDataSetChanged();
    }

    Set<Integer> getCountersToDelete() {
        return countersToDelete;
    }
    void deleteScene(int position)
    {
        //adapterData.remove(position);
        Realm realm =Realm.getDefaultInstance();
        ///Scene scene= realm.where(Scene.class).equalTo("sceneId",sceneId).findFirst();
        launcAct.deleteModelData(adapterData.get(position).getLocalPath());
        realm.beginTransaction();
        //scene.setTitle(scene.getTitle()+"deleted");
        adapterData.get(position).setTitle(adapterData.get(position).getTitle()+"deleted");
        adapterData.get(position).setModelDownloaded(false);
        //realm.copyToRealmOrUpdate(scene);
        launcAct.deleteModelData(Environment.getExternalStorageDirectory()+adapterData.get(position).getLocalPath());
        realm.commitTransaction();
        realm.close();
        //notifyDataSetChanged();
    }
    public void DownloadScene(String sceneId)
    {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        //adapterData.get(position).setModelDownloaded(true);
        //adapterData.
        Scene scene =realm.where(Scene.class).equalTo("sceneId",sceneId).findFirst();
        scene.setModelDownloaded(true);
        realm.commitTransaction();
        realm.close();
        //notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final Scene scene=getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if(convertView==null)
            convertView= LayoutInflater.from(parent.getContext()).inflate(R.layout.scene_item,parent,false);

        // Lookup view for data population
        TextView scTitle=(TextView) convertView.findViewById(R.id.scTitle);
        TextView scDescription=(TextView) convertView.findViewById(R.id.scDescription);

        // Populate the data into the template view using the data object
        scTitle.setText(scene.getTitle());
        scDescription.setText(scene.getDescription());

        Bitmap myBitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory()+scene.getThumbnail());
        ImageView imageView= (ImageView) convertView.findViewById(R.id.sceneThumbnail);
        imageView.setImageBitmap(myBitmap);
        Button viewButton= (Button) convertView.findViewById(R.id.viewButton);
        viewButton.setTag(scene);
        if(!scene.isModelDownloaded())
            viewButton.setVisibility(View.GONE);
        else viewButton.setVisibility(View.VISIBLE);
        // Attach the click event handler
        viewButton.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                    Scene scene= (Scene) v.getTag();
                    launcAct.viewSceneIntent(scene.getLocalPath());
                    Log.i(scene.getLocalPath(),TAG);
              }
        });

        Button deleteButon= (Button) convertView.findViewById(R.id.deleteButton);
        deleteButon.setTag(scene);
        if(!scene.isModelDownloaded())
            deleteButon.setVisibility(View.GONE);
        else deleteButon.setVisibility(View.VISIBLE);
        // Attach the click event handler
        deleteButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               deleteScene(position);
            }
        });
        final Button downloadButton= (Button) convertView.findViewById(R.id.downloadButton);
        downloadButton.setTag(scene);
       // Log.i("taaag"+scene.getSceneId(),TAG);
        // Attach the click event handler
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Scene scene= (Scene) v.getTag();
                Log.i("taaag1--"+scene.getSceneId(),TAG);
                launcAct.setSelectedScene(scene.getSceneId());
                launcAct.getResultsFromApi();
            }
        });
        return convertView;
    }
}
