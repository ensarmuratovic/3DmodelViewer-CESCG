package org.cescg.modelviewer;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import com.google.api.services.drive.model.*;
import com.google.api.services.drive.model.File;

import com.sromku.simple.storage.InternalStorage;
import com.sromku.simple.storage.SimpleStorage;
import com.sromku.simple.storage.Storage;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.cescg.modelviewer.Classes.Project;
import org.cescg.modelviewer.Classes.Scene;
import org.cescg.modelviewer.Classes.ScenesAdapter;
import org.eclipse.xtend.lib.annotations.ToString;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class LaunchActivity extends Activity
        implements EasyPermissions.PermissionCallbacks {
    GoogleAccountCredential mCredential;
    private Button mCallApiButton;
    private Button viewModelButton;
    private Button deleteButton;
    ProgressDialog mProgress;
    ArrayList<Scene> scenes;
    ScenesAdapter adapter;
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { DriveScopes.DRIVE};
    private static final String TAG = "ENSAR";
    private static Realm realm;
    private String selectedSceneId;
    private String selectedSceneTitle;
    private Project selectedProject;
    private BroadcastReceiver receiver;
    IntentFilter filter ;
    private int queueSize;
    /**
     * Create the main activity.
     * @param savedInstanceState previously saved instance data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            //initialize realm
            Realm.init(getApplicationContext());
            //get a realm instance for this thread
            realm = Realm.getDefaultInstance();

            setContentView(R.layout.activity_launch);
            mProgress = new ProgressDialog(this);
            mProgress.setCancelable(false);
            RealmResults<Project> p = realm.where(Project.class).findAll();
            Log.i("broj projekta init:" + p.size(), TAG);
            RealmResults<Scene> s = realm.where(Scene.class).findAll().sort("title");
            Log.i("broj scena init:" + s.size(), TAG);
        try {
            //scenes = new ArrayList<Scene>();
           // scenes.addAll(s);
            adapter = new ScenesAdapter(s);
            adapter.setBaseActivity(this);
            ListView listView = (ListView) findViewById(R.id.sceneList);
            listView.setAdapter(adapter);

          /*  RealmChangeListener sceneListener= new RealmChangeListener() {
                @Override
                public void onChange(Object element) {
                    adapter.notifyDataSetChanged();
                }
            };
            s.addChangeListener(sceneListener);*/


        }
        catch (Exception e)
        {
            Log.e("error adapter",TAG,e);
        }
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                queueSize--;
                if(queueSize==0 && selectedSceneId!=null) {
                    Toast.makeText(getApplicationContext(), "Model "+selectedSceneTitle+" is downloaded!",
                            Toast.LENGTH_LONG).show();
                    adapter.DownloadScene(selectedSceneId);
                    setSelectedSceneId(null);
                    setSelectedSceneTitle(null);
                    unregisterReceiver(receiver);
                }
                else if(queueSize==0 && selectedSceneId==null)
                {
                    Toast.makeText(getApplicationContext(), "Project updated",
                            Toast.LENGTH_LONG).show();
                    unregisterReceiver(receiver);
                    mProgress.cancel();
                    ListView l= (ListView)findViewById(R.id.sceneList);
                    findViewById(R.id.projectTitle).setVisibility(View.VISIBLE);
                    mCallApiButton.setVisibility(View.VISIBLE);
                    findViewById(R.id.projectHead).setVisibility(View.VISIBLE);
                    TextView project=(TextView) findViewById(R.id.projectTitle);
                    project.setText(selectedProject.getTitle());
                    adapter.notifyDataSetChanged();
                }

            }
        };
        filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

        registerReceiver(receiver,filter);


        mCallApiButton = (Button) findViewById(R.id.callApi);

        mCallApiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    setSelectedSceneId(null);
                    setSelectedSceneTitle(null);
                    getResultsFromApi();
                }

        });

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission to write on external storage granted");

            } else {

                Log.v(TAG, "Permission to write on external storage revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission to write on external storage granted");
        }
        TextView project=(TextView) findViewById(R.id.projectTitle);
        if(p.size()!=0)
            project.setText(p.get(0).getTitle());
        else
        {
            project.setVisibility(View.GONE);
            mCallApiButton.setVisibility(View.GONE);
            findViewById(R.id.projectHead).setVisibility(View.GONE);
        }

    }

    public String getSelectedSceneId() {
        return selectedSceneId;
    }

    public void setSelectedSceneId(String selectedScene) {
        this.selectedSceneId = selectedScene;
    }
    public String getSelectedSceneTitle() {
        return selectedSceneTitle;
    }

    public void setSelectedSceneTitle(String selectedSceneTitle) {
        this.selectedSceneTitle = selectedSceneTitle;
    }
    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    public void getResultsFromApi() {

        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            Log.i("No network connection!",TAG);
        } else{
           // Log.i("taaag--"+sceneId,TAG);
            new MakeRequestTask(mCredential).execute();
        }
    }
    public void deleteModelData(String path) {
        java.io.File directory = new java.io.File(path);
        // If the directory exists then delete
        if (directory.exists()) {
            java.io.File[] files = directory.listFiles();
            if (files == null) {
                return;
            }
            // Run on all sub files and folders and delete them
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteModelData(files[i].getAbsolutePath());
                    //delete everything except thumbnail
                } else if(!(files[i].getName().equals("thumbnail.jpg"))) {
                    files[i].delete();

                }
            }
        }
    }
    public void viewSceneIntent(String sceneLocalPath)
    {

        Intent intent = new Intent(LaunchActivity.this, ViewerActivity.class);
        intent.putExtra("sceneLocalPath",sceneLocalPath);
        startActivity(intent);
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);

            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK)
                    Log.i("App requires GP Serv",TAG);
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }

    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
        if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        }

    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                LaunchActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * An asynchronous task that handles the Drive API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */


    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.drive.Drive mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.drive.Drive.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Drive API Android Quickstart")
                    .build();

        }

        /**
         * Background task to call Drive API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                //init download of scene data; Model data is not downloaded
                if(getSelectedSceneId()==null)
                    return getInitDataFromApi();
                else
                    return downloadSceneDataFromApi(getSelectedSceneId());
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }


        private List<String> getInitDataFromApi() throws IOException {

            List<File> result1 = new ArrayList<File>();
            DownloadManager mManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            //downloading main project folder
            Drive.Files.List request = mService.files().list().setFields("nextPageToken, files(id, name, parents, mimeType, createdTime,modifiedTime,viewedByMeTime,webContentLink,fileExtension)")
            .setQ("mimeType='application/vnd.google-apps.folder' AND name contains 'C2E0S1C7G'");

            do {
                try {
                    FileList files = request.execute();

                    result1.addAll(files.getFiles());
                    request.setPageToken(files.getNextPageToken());
                } catch (UserRecoverableAuthIOException userRecoverableException){
                    startActivityForResult(userRecoverableException.getIntent(), REQUEST_AUTHORIZATION);
                    request.setPageToken(null);
                }
            } while (request.getPageToken() != null &&
                    request.getPageToken().length() > 0);
           // Log.i(TAG, "******Number of projects: " + result1.size());
            List<String> fileInfo = new ArrayList<String>();
            java.io.File documentsFolder=new java.io.File(Environment.getExternalStorageDirectory(),"/projects/"+result1.get(0).getName());
            documentsFolder.mkdirs();

            //download child folders and saving to realm
           request = mService.files().list().setFields("nextPageToken, files(id, name, description, parents, mimeType, createdTime,modifiedTime,viewedByMeTime,webContentLink,fileExtension)")
                    .setQ("mimeType='application/vnd.google-apps.folder' AND '"+ result1.get(0).getId()+"' in parents");
            FileList fileList=request.execute();
            List<File> childs=fileList.getFiles();
           // Log.i(TAG, "******Number of scenes: " + childs.size());


            Project pr=new Project();
            pr.setProjectId(result1.get(0).getId());
            pr.setTitle(result1.get(0).getName());
            pr.setLocalPath(Environment.getExternalStorageDirectory()+"/projects/"+result1.get(0).getName());
            pr.setWebContentLink(result1.get(0).getWebContentLink());
            realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            for(int i=0;i<childs.size();i++) {
                documentsFolder = new java.io.File(Environment.getExternalStorageDirectory()+ "/projects/" + result1.get(0).getName()+"/"+childs.get(i).getName());
                documentsFolder.mkdirs();
                /*Scene sc= realm.where(Scene.class).equalTo("sceneId", childs.get(i).getId()).findFirst();
                if(!sc.isValid())
                    sc=new Scene();*/
                Scene sc=new Scene();
               /*try {
                    sc = realm.where(Scene.class).equalTo("sceneId", childs.get(i).getId()).findFirst();
                }
                catch (Exception e) {
                    sc=new Scene();
                    Log.i("alooo" , TAG);
                }*/

                sc.setSceneId(childs.get(i).getId());
                sc.setTitle(childs.get(i).getName());
                sc.setDescription(childs.get(i).getDescription());
                sc.setLocalPath("/projects/" + result1.get(0).getName()+"/"+childs.get(i).getName());
                sc.setWebContentLink(childs.get(i).getWebContentLink());

                boolean isDownloaded;
                try {
                    isDownloaded = realm.where(Scene.class).equalTo("sceneId", childs.get(i).getId()).findFirst().isModelDownloaded();
                }
                catch (Exception e)
                {
                    isDownloaded=false;
                }
                sc.setModelDownloaded(isDownloaded);
                java.io.File temp=new java.io.File(Environment.getExternalStorageDirectory()+ "/projects/" + result1.get(0).getName()+"/"+childs.get(i).getName()+"/thumbnail.jpg");
                if(temp.exists()) {
                    temp.delete();
                }
                request = mService.files().list().setFields("nextPageToken, files(id, name, description, parents, mimeType, createdTime,modifiedTime,viewedByMeTime,webContentLink,fileExtension)")
                        .setQ("name contains 'thumbnail' AND '" + childs.get(i).getId() + "' in parents");
                fileList = request.execute();
                File thumbnail = fileList.getFiles().get(0);
                Log.i("thumbnail link" + thumbnail.getWebContentLink(), TAG);
                DownloadManager.Request mRqRequest = new DownloadManager.Request(Uri.parse(thumbnail.getWebContentLink()));
                mRqRequest.setDescription("Downloading: " + thumbnail.getName());
                //Log.i("file:", file.getName());
                mRqRequest.setDestinationInExternalPublicDir(sc.getLocalPath(), thumbnail.getName());
                mManager.enqueue(mRqRequest);
                queueSize++;
                sc.setThumbnail(sc.getLocalPath() + "/" + thumbnail.getName());

                pr.addScene(sc);
                Log.i("nnn:"+sc.isModelDownloaded(),TAG);

                Log.i(sc.getLocalPath(),TAG);
            }
            realm.copyToRealmOrUpdate(pr);
            realm.commitTransaction();
            selectedProject=pr;
            RealmResults<Project> p= realm.where(Project.class).findAll();
            Log.i("broj projekta:"+ p.size(),TAG );
            RealmResults<Scene> s =realm.where(Scene.class).findAll();
            Log.i("broj scena:"+ s.size(),TAG );
            Log.i("broj scena iz klase:"+p.get(0).getScenes().size(),TAG );
            Log.e(p.get(0).getLocalPath().toString(),TAG);
            realm.close();

            return fileInfo;
        }

        private List<String> downloadSceneDataFromApi(String sceneId) throws IOException {

            List<String> fileInfo = new ArrayList<String>();
            List<File> result1 = new ArrayList<File>();
            Drive.Files.List request = mService.files().list().setFields("nextPageToken, files(id, name, parents, mimeType, createdTime,modifiedTime,viewedByMeTime,webContentLink,fileExtension)")
                    .setPageSize(200)
                    .setQ("'"+sceneId+"' in parents");
            FileList fileList=request.execute();
            List<File> modelData=fileList.getFiles();
            Log.i("Broj fileova"+modelData.size(),TAG);
            Log.i("sceneidd: "+sceneId,TAG);
            realm=Realm.getDefaultInstance();
            DownloadManager mManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            try {
                Scene scene = realm.where(Scene.class).equalTo("sceneId", sceneId).findFirst();
                deleteModelData(Environment.getExternalStorageDirectory()+scene.getLocalPath());
                for (File file : modelData) {
                    if(file.getName().equals("thumbnail.jpg"))
                        continue;
                   //Log.i("liiiink:"+file.getWebContentLink(),TAG);
                    DownloadManager.Request mRqRequest = new DownloadManager.Request(Uri.parse(file.getWebContentLink()));

                    mRqRequest.setDescription("Downloading: " + file.getName());
                   //Log.i("file:", file.getName());
                    mRqRequest.setDestinationInExternalPublicDir(scene.getLocalPath(),file.getName());
                    long idDownLoad = mManager.enqueue(mRqRequest);
                    queueSize++;
                }

            }

            catch (Exception e)
            {
                Log.e("error download",TAG,e);
            }
            return fileInfo;
        }

        @Override
        protected void onPreExecute() {
            registerReceiver(receiver,filter);
            if(getSelectedSceneId()==null) {
                mProgress.setTitle("Fetching project");
                mProgress.setMessage("Please wait...");
                mProgress.show();
            }
            else {
                mProgress.setTitle("Downloading scene");
                mProgress.setMessage("You will be notified when it is done.");
                mProgress.show();
            }

        }

        @Override
        protected void onPostExecute(List<String> output) {
           // mProgress.hide();


            //after model download completes change scene.modelExists to true
            if(getSelectedSceneId()!=null) {
                Runnable progressRunnable = new Runnable() {

                    @Override
                    public void run() {
                        mProgress.cancel();
                    }
                };

                Handler pdCanceller = new Handler();
                pdCanceller.postDelayed(progressRunnable, 2000);

            }
            if (output == null || output.size() == 0) {
                Log.i("No results returned.",TAG);
            } else {
                output.add(0, "Data retrieved using the Drive API:");

            }
        }
        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            LaunchActivity.REQUEST_AUTHORIZATION);
                } else {
                   // mOutputText.setText("The following error occurred:\n"
                          //  + mLastError.getMessage());
                }
            } else {
                //mOutputText.setText("Request cancelled.");
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}