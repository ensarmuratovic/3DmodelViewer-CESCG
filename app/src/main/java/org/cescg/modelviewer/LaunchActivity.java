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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.cescg.modelviewer.Classes.Project;
import org.cescg.modelviewer.Classes.Scene;
import org.eclipse.xtend.lib.annotations.ToString;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class LaunchActivity extends Activity
        implements EasyPermissions.PermissionCallbacks {
    GoogleAccountCredential mCredential;
    private TextView mOutputText;
    private Button mCallApiButton;
    private Button viewModelButton;
    private Button createInternalButton;
    private Button createExternalButton;
    ProgressDialog mProgress;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String BUTTON_TEXT = "Call Drive API";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { DriveScopes.DRIVE};
    private static final String TAG = "ENSAR";
    private static Realm realm;
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


        RealmResults<Project> p= realm.where(Project.class).findAll();
        Log.i("broj projekta:"+ p.size(),TAG );
        RealmResults<Scene> s =realm.where(Scene.class).findAll();
        Log.i("broj scena:"+ s.size(),TAG );

        LinearLayout activityLayout = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        activityLayout.setLayoutParams(lp);
        activityLayout.setOrientation(LinearLayout.VERTICAL);
        activityLayout.setPadding(16, 16, 16, 16);

        ViewGroup.LayoutParams tlp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        mCallApiButton = new Button(this);
        mCallApiButton.setText(BUTTON_TEXT);
        mCallApiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallApiButton.setEnabled(false);
                mOutputText.setText("");
                getResultsFromApi();
                mCallApiButton.setEnabled(true);
            }
        });
        activityLayout.addView(mCallApiButton);

        viewModelButton = new Button(this);
        viewModelButton.setText("View Model");
        viewModelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModelButton.setEnabled(false);
                mOutputText.setText("");
                viewModelButton.setEnabled(true);

                Intent intent=new Intent(LaunchActivity.this,ViewerActivity.class);
                startActivity(intent);
            }
        });
        activityLayout.addView(viewModelButton);


        createInternalButton = new Button(this);
        createInternalButton.setText("create internal dir");
        createInternalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Storage storage = SimpleStorage.getInternalStorage(getApplicationContext());


                java.io.File documentsFolder = new java.io.File(getFilesDir(),"projects");
                documentsFolder.mkdirs();
                documentsFolder=new java.io.File(getFilesDir(),"projects/White Bastion C2E0S1C7G");
                documentsFolder.mkdirs();
                documentsFolder=new java.io.File(getFilesDir(),"projects/White Bastion C2E0S1C7G/Medieval period M1");
                documentsFolder.mkdirs();
                documentsFolder=new java.io.File(getFilesDir(),"projects/White Bastion C2E0S1C7G/Ottoman period M2");
                documentsFolder.mkdirs();
                documentsFolder=new java.io.File(getFilesDir(),"projects/White Bastion C2E0S1C7G/Ottoman period M3");
                documentsFolder.mkdirs();
                documentsFolder=new java.io.File(getFilesDir(),"projects/White Bastion C2E0S1C7G/Ottoman period M3/config");
                documentsFolder.mkdirs();
                Log.i("external", TAG);
            }
        });
        activityLayout.addView(createInternalButton);

        createExternalButton = new Button(this);
        createExternalButton.setText("delete internal dir");
        createExternalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String path = getFilesDir().getAbsolutePath();
                deleteDirectoryImpl(path+"/projects");

            }
        });
        activityLayout.addView(createExternalButton);

        mOutputText = new TextView(this);
        mOutputText.setLayoutParams(tlp);
        mOutputText.setPadding(16, 16, 16, 16);
        mOutputText.setVerticalScrollBarEnabled(true);
        mOutputText.setMovementMethod(new ScrollingMovementMethod());
        mOutputText.setText(
                "Click the \'" + BUTTON_TEXT +"\' button to test the API.");
        activityLayout.addView(mOutputText);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Drive API ...");

        setContentView(activityLayout);

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission to write on external storage granted");

            } else {

                Log.v(TAG,"Permission to write on external storage revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission to write on external storage granted");
        }
    }



    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            mOutputText.setText("No network connection available.");
        } else {
            new MakeRequestTask(mCredential).execute();
        }
    }
    private boolean deleteDirectoryImpl(String path) {
        java.io.File directory = new java.io.File(path);
        // If the directory exists then delete
        if (directory.exists()) {
            java.io.File[] files = directory.listFiles();
            if (files == null) {
                return true;
            }
            // Run on all sub files and folders and delete them
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectoryImpl(files[i].getAbsolutePath());
                } else {
                    files[i].delete();
                }
            }
        }
        return directory.delete();
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
                getResultsFromApi();
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
                if (resultCode != RESULT_OK) {
                    mOutputText.setText(
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.");
                } else {
                    getResultsFromApi();
                }
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
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of up to 10 file names and IDs.
         * @return List of Strings describing files, or an empty list if no files
         *         found.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {

            List<File> result1 = new ArrayList<File>();
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
            java.io.File documentsFolder=new java.io.File(getFilesDir(),"projects/"+result1.get(0).getName());
            documentsFolder.mkdirs();

           request = mService.files().list().setFields("nextPageToken, files(id, name, parents, mimeType, createdTime,modifiedTime,viewedByMeTime,webContentLink,fileExtension)")
                    .setQ("mimeType='application/vnd.google-apps.folder' AND '"+ result1.get(0).getId()+"' in parents");
            FileList fileList=request.execute();
            List<File> childs=fileList.getFiles();
           // Log.i(TAG, "******Number of scenes: " + childs.size());
            Project pr=new Project();
            pr.setProjectId(result1.get(0).getId());
            pr.setTitle(result1.get(0).getName());
            pr.setLocalPath(getFilesDir()+"projects/"+result1.get(0).getName());
            pr.setWebContentLink(result1.get(0).getWebContentLink());
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            for(int i=0;i<childs.size();i++) {
                documentsFolder = new java.io.File(getFilesDir(), "projects/" + result1.get(0).getName()+"/"+childs.get(i).getName());
                documentsFolder.mkdirs();
                Scene sc= new Scene();
                sc.setSceneId(childs.get(i).getId());
                sc.setTitle(childs.get(i).getName());
                sc.setLocalPath(getFilesDir()+"projects/"+result1.get(0).getName()+"/"+childs.get(i).getName());
                sc.setWebContentLink(childs.get(i).getWebContentLink());
                pr.addScene(sc);
            }

            realm.copyToRealmOrUpdate(pr);
            realm.commitTransaction();
            RealmResults<Project> p= realm.where(Project.class).findAll();
            Log.i("broj projekta:"+ p.size(),TAG );
            RealmResults<Scene> s =realm.where(Scene.class).findAll();
            Log.i("broj scena:"+ s.size(),TAG );
            Log.i("broj scena iz klase:"+p.get(0).getScenes().size(),TAG );

            Log.e(p.get(0).getLocalPath().toString(),TAG);
            for(Scene scene:s)
            {
                Log.e(scene.getLocalPath(),TAG);
            }


           /* Log.i(TAG,mCredential.getSelectedAccountName());
            if (result1 != null) {
                for (File file : result1) {
                    //change Example.extension to the full name of file on your Drive(name.extension)
                    if (file.getName().contains("town.zip")) {


                        // mService.files().update()
                        //mService.files().update(file.getId(),file).execute();

                        fileInfo.add(String.format("%s (%s)\n", file.getName(), file.getId()));
                        DownloadManager mManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                        //DownloadManager.Request mRqRequest = new DownloadManager.Request(
                              //  Uri.parse("https://drive.google.com/uc?export=download&id="+file.getId().toString()));
                        DownloadManager.Request mRqRequest = new DownloadManager.Request( Uri.parse(file.getWebContentLink()));
                        mRqRequest.setDescription("This is Test File");
                        mRqRequest.setDestinationInExternalPublicDir("/download/proba",file.getName());
                        long idDownLoad = mManager.enqueue(mRqRequest);
                        Log.i(TAG, "Id: " + file.getId());
                        Log.i(TAG, "Parent: " + file.getParents().get(0));
                        Log.i(TAG, "Mime Type: " + file.getMimeType());
                        Log.i(TAG, "File Extension: " + file.getFileExtension());
                        Log.i(TAG, "Date created: " + file.getCreatedTime());
                        Log.i(TAG, "Date Modified: " + file.getModifiedTime());
                        Log.i(TAG, "Viewed by me: " + file.getViewedByMeTime());
                        Log.i(TAG, "Web content link: " + file.getWebContentLink());
                    }
                    //l= (ArrayList) file.getParents();
                    // Log.i(TAG,Integer.toString(l.size()));

                }
            }*/

            return fileInfo;
        }


        @Override
        protected void onPreExecute() {
            mOutputText.setText("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.hide();
            if (output == null || output.size() == 0) {
                mOutputText.setText("No results returned.");
            } else {
                output.add(0, "Data retrieved using the Drive API:");
                mOutputText.setText(TextUtils.join("\n", output));
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
                    mOutputText.setText("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
        }
    }
}