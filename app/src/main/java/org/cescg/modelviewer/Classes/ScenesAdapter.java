package org.cescg.modelviewer.Classes;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
        realm.beginTransaction();
        //scene.setTitle(scene.getTitle()+"deleted");
        adapterData.get(position).setTitle(adapterData.get(position).getTitle()+"deleted");
        //realm.copyToRealmOrUpdate(scene);
        realm.commitTransaction();
        realm.close();
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Scene scene=getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if(convertView==null)
            convertView= LayoutInflater.from(parent.getContext()).inflate(R.layout.scene_item,parent,false);

        // Lookup view for data population
        TextView scTitle=(TextView) convertView.findViewById(R.id.scTitle);
        TextView scDescription=(TextView) convertView.findViewById(R.id.scDescription);

        // Populate the data into the template view using the data object
        scTitle.setText(scene.getTitle());
        scDescription.setText(scene.getDescription());
        Button viewButton= (Button) convertView.findViewById(R.id.viewButton);
        viewButton.setTag(scene);

        // Attach the click event handler
        viewButton.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                    Scene scene= (Scene) v.getTag();
                    Log.i(scene.getLocalPath(),TAG);
              }
        });

        Button deleteButon= (Button) convertView.findViewById(R.id.deleteButton);
        viewButton.setTag(scene);
        // Attach the click event handler
        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               deleteScene(position);
            }
        });
        Button downloadButton= (Button) convertView.findViewById(R.id.downloadButton);
        downloadButton.setTag(scene);
        // Attach the click event handler
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launcAct.getResultsFromApi();
            }
        });

        return convertView;

    }
}
