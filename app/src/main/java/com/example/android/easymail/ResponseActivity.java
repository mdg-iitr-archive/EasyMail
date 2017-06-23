package com.example.android.easymail;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.android.easymail.adapters.EmailGridViewAdapter;
import com.example.android.easymail.adapters.EmailTilesAdapter;
import com.example.android.easymail.interactor.ResponseInteractorImpl;
import com.example.android.easymail.models.CurrentDayMessageSendersList;
import com.example.android.easymail.models.CurrentDayMessageSendersRealmList;
import com.example.android.easymail.presenter.ResponsePresenterImpl;
import com.example.android.easymail.view.ResponseActivityView;
import com.example.android.easymail.views.ExpandableGridView;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.gmail.model.Message;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import retrofit2.http.GET;

public class ResponseActivity extends AppCompatActivity implements
        SenderNameInitialClickListener, CurrentDayMessageClickListener, ResponseActivityView,
        NavigationView.OnNavigationItemSelectedListener {

    Context context = this;
    private LinearLayout linearLayout;
    private RecyclerView emailNameInitialRecycler;
    private EmailTilesAdapter emailTilesAdapter;
    private ExpandableGridView emailNameInitialGridView;
    private EmailGridViewAdapter emailGridViewAdapter;
    private ResponsePresenterImpl responsePresenter;
    private ProgressDialog dialog;
    private DrawerLayout drawerLayout;
    private com.example.android.easymail.models.Message message;
    private NavigationView leftNavigationView, rightNavigationView;
    List<CurrentDayMessageSendersList> list;
    AccountManager accountManager;
    private Realm realm;
    private static final int GET_ACCOUNTS_PERMISSION = 100;
    // Content provider authority
    public static final String AUTHORITY = "com.example.android.easymail.provider";
    // Account
    public static Account ACCOUNT = null;
    // Sync interval constants
    public static final long SECONDS_PER_MINUTE = 60L;
    public static final long SYNC_INTERVAL_IN_MINUTES = 60L;
    public static final long SYNC_INTERVAL =
            SYNC_INTERVAL_IN_MINUTES *
                    SECONDS_PER_MINUTE;
    // A content resolver for accessing the provider
    ContentResolver mResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response);

        initViews();
        regListeners();
        accountManager = AccountManager.get(this);

        // ask for the dangerous permission of adding accounts
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {

            // permission not granted, thus request again for the permissions
            ActivityCompat.requestPermissions(ResponseActivity.this,
                    new String[]{Manifest.permission.GET_ACCOUNTS},
                    GET_ACCOUNTS_PERMISSION);
        } else {

            // permission granted
            // acquire the list of accounts from account manager
            final Account accountList[] = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
            if (accountList.length == 0) {

                // if no account is present, then add a new account
                accountManager.addAccount(Constants.ACCOUNT_TYPE, Constants.AUTHTOKEN_TYPE_FULL_ACCESS, null
                        , null, this, null, null);
            } else {

                // account is present, thus get the deserialized auth state
                ACCOUNT =  accountList[0];
                final AccountManagerFuture<Bundle> future = accountManager.getAuthToken(accountList[0], Constants.AUTHTOKEN_TYPE_FULL_ACCESS, null, this, null, null);
                try {

                    Bundle bnd = future.getResult();
                    final String deserializedAuthState = bnd.getString(AccountManager.KEY_AUTHTOKEN);
                    AuthState state = AuthState.jsonDeserialize(deserializedAuthState);
                    AuthorizationService service = new AuthorizationService(context);
                    final ResponseActivityView instance = this;

                    // obtain the fresh access token from the auth state
                    state.performActionWithFreshTokens(service, new AuthState.AuthStateAction() {
                        @Override
                        public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                            if (ex == null) {
                                responsePresenter = new ResponsePresenterImpl(instance, new ResponseInteractorImpl(), ResponseActivity.this, getApplication());
                                final AuthorizationResponse response = AuthorizationResponse.fromIntent(getIntent());
                                final AuthorizationException exception = AuthorizationException.fromIntent(getIntent());
                                responsePresenter.getOfflineMessages();
                                responsePresenter.performTokenRequest(response, accessToken);
                            } else {
                                Log.e("auth token exception", ex.toString());
                            }
                        }
                    });
                    Log.d("easymail", "GetToken Bundle is " + bnd);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        mResolver = getContentResolver();
        mResolver.addPeriodicSync(ACCOUNT, AUTHORITY, Bundle.EMPTY, SYNC_INTERVAL);

        /*
        responsePresenter = new ResponsePresenterImpl(this, new ResponseInteractorImpl(), ResponseActivity.this, getApplication());
        final AuthorizationResponse response = AuthorizationResponse.fromIntent(getIntent());
        final AuthorizationException exception = AuthorizationException.fromIntent(getIntent());
        String isAutoSignedInToken = getIntent().getExtras().getString("is_auto_signed_in_token");
        ////////////////////////////////////////////////////////////////////////////////////////////
        responsePresenter.getOfflineMessages();
        responsePresenter.performTokenRequest(response, isAutoSignedInToken);
        //responsePresenter.getOfflineMessages();

*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch(requestCode){
            case GET_ACCOUNTS_PERMISSION:
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    final Account accountList[] = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);

                }
        }
    }

    private void initViews(){
        linearLayout = (LinearLayout) findViewById(R.id.layout);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        leftNavigationView = (NavigationView) findViewById(R.id.left_drawer);
        rightNavigationView = (NavigationView) findViewById(R.id.right_drawer);
        leftNavigationView.setNavigationItemSelectedListener(this);
        rightNavigationView.setNavigationItemSelectedListener(this);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    private void regListeners() {
    }

    @Override
    public void onSenderNameInitialClick(int row, int column, int isExpanded) {

        ArrayList<Integer> ids = new ArrayList<>();
        int layoutId = Integer.parseInt("1" + Integer.toString(row));
        for (int m = 1; m <= 4; m++) {
            if (m != column)
                ids.add(Integer.parseInt("2" + Integer.toString(row) + Integer.toString(m)));
        }
        if (isExpanded == 1) {
            for (int id : ids) {
                RecyclerView recyclerView = (RecyclerView) findViewById(id);
                if (recyclerView != null)
                    recyclerView.setVisibility(View.VISIBLE);
            }
        } else {
            for (int id : ids) {
                RecyclerView recyclerView = (RecyclerView) findViewById(id);
                if (recyclerView != null)
                    recyclerView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void showAutoSignedInDialog() {

        dialog = new ProgressDialog(ResponseActivity.this);
        dialog.setMessage("Auto Signing In!");
        dialog.show();
    }

    @Override
    public void showTokenRequestDialog() {

        dialog = new ProgressDialog(ResponseActivity.this);
        dialog.setMessage("Making Token Request!");
        dialog.show();
    }

    @Override
    public void showExchangeSucceddedToast() {
        Toast.makeText(context, "Token Request Completed!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showExchangeFailedToast() {
        Toast.makeText(context, "Token Request Failed!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showAuthorizationFailedToast() {
        Toast.makeText(context, "Authorization Request Failed!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void formRecyclerView(List<CurrentDayMessageSendersRealmList> list, int i, int j, RecyclerView recyclerView) {

        emailTilesAdapter = new EmailTilesAdapter(this, this, this, list, i + 1, j + 1);
        recyclerView.setAdapter(emailTilesAdapter);
    }

    @Override
    public void addLinearLayoutToDisplay(LinearLayout currentLinearLayout) {
        linearLayout.addView(currentLinearLayout);
    }

    @Override
    public void showZeroMessagesReceivedToast() {
        Toast.makeText(ResponseActivity.this, "No Messages Received!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void hideDialog() {
        dialog = new ProgressDialog(ResponseActivity.this);
        if (dialog.isShowing())
            dialog.hide();
    }

    @Override
    public void getCredential(String accessToken) {

        Intent serviceIntent = new Intent(ResponseActivity.this, MessagesPullService.class);
        serviceIntent.putExtra("token", accessToken);
        startService(serviceIntent);
    }

    @Override
    public void onCurrentDayMessageClickListener(View v, com.example.android.easymail.models.Message child) {

        message = child;
        RealmConfiguration configuration = new RealmConfiguration.Builder().build();
        realm = Realm.getInstance(configuration);
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(message);
        realm.commitTransaction();
        drawerLayout.openDrawer(GravityCompat.END);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent editMessageIntent = new Intent(ResponseActivity.this, EditMessageActivity.class);
        Intent customListMessagesIntent = new Intent(ResponseActivity.this, CustomListMessagesActivity.class);

        switch (item.getItemId()){

            // On click for left navigation view
            case R.id.left_nav_to_do:
                editMessageIntent.putExtra("listName", "To-Do");
                startActivity(customListMessagesIntent);
                break;
            case R.id.left_nav_follow_up:
                customListMessagesIntent.putExtra("listName", "Follow Up");
                startActivity(customListMessagesIntent);
                break;
            case R.id.left_nav_launch_events:
                customListMessagesIntent.putExtra("listName", "Launch Events");
                startActivity(customListMessagesIntent);
                break;
            case R.id.left_nav_business_events:
                customListMessagesIntent.putExtra("listName", "Business Events");
                startActivity(customListMessagesIntent);
                break;

            // On click for right navigation view
            case R.id.right_nav_to_do:
                editMessageIntent.putExtra("listName", "To-Do");
                editMessageIntent.putExtra("messageId", message.getId());
                startActivity(editMessageIntent);
                break;
            case R.id.right_nav_follow_up:
                editMessageIntent.putExtra("listName", "Follow Up");
                editMessageIntent.putExtra("messageId", message.getId());
                startActivity(editMessageIntent);
                break;
            case R.id.right_nav_launch_events:
                editMessageIntent.putExtra("listName", "Launch Events");
                editMessageIntent.putExtra("messageId", message.getId());
                RealmResults<com.example.android.easymail.models.Message> results = realm.where(com.example.android.easymail.models.Message.class).equalTo("id", message.getId()).findAll();
                com.example.android.easymail.models.Message messages = realm.copyFromRealm(results).get(0);
                startActivity(editMessageIntent);
                break;
            case R.id.right_nav_business_events:
                editMessageIntent.putExtra("listName", "Business Events");
                editMessageIntent.putExtra("messageId", message.getId());
                startActivity(editMessageIntent);
                break;
        }
        return true;
    }
}

       /*
        credential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(scopes)).
                setBackOff(new ExponentialBackOff());

        if( isAutoSignedInToken != null ){
            performTask(isAutoSignedInToken);
        }

        if (response != null){
            //authorization succeeded\\
            Toast.makeText(context, "Authorization response completed", Toast.LENGTH_SHORT).show();
            AuthorizationService service = new AuthorizationService(context);
            service.performTokenRequest(
                    response.createTokenExchangeRequest(),
                    new AuthorizationService.TokenResponseCallback() {
                        @Override
                        public void onTokenRequestCompleted(@Nullable TokenResponse resp, @Nullable AuthorizationException ex) {
                            if (resp != null) {
                                //exchange succeeded\\
                                Toast.makeText(context, "Token Request Completed", Toast.LENGTH_SHORT).show();
                                accessToken = resp.accessToken;
                                AuthState state = new AuthState(response, resp, ex);
                                writeAuthState(state);
                                performTask(accessToken);
                            }
                            else {
                                //exchange failed\\
                                responseText.setText(response.toString());
                                Toast.makeText(context, "Token Response Failed", Toast.LENGTH_SHORT).show();
                                AuthState state = new AuthState(response, null, ex);
                                writeAuthState(state);
                            }
                        }
                    }
            );

        }
        else {
            //authorization failed\\
            Toast.makeText(context, "Authorization response failed", Toast.LENGTH_SHORT).show();
        }

    }

    private void regListeners() {
    }


    private void initViews(){

        hashTable = new HashTable(HASH_TABLE_SIZE);
        currentDayMessages = new ArrayList<Message>();
        currentDayMessageSendersList = new ArrayList<>();

        responseText = (TextView) findViewById(R.id.txt_response);
        tokenText = (TextView) findViewById(R.id.txt_token);
        emailNameInitialRecycler = (RecyclerView) findViewById(R.id.email_name_tile_recycler);
        emailNameInitialRecycler.setHasFixedSize(true);
        emailNameInitialGridView = (ExpandableGridView) findViewById(R.id.email_name_tile_grid_view);

        linearLayout = (LinearLayout) findViewById(R.id.layout);
        //RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ResponseActivity.this);
        //emailNameInitialRecycler.setLayoutManager(layoutManager);
    }

    public void performTask(String accessToken) {

        googleCredential = new GoogleCredential().setAccessToken(accessToken);
        progressDialog = new ProgressDialog(ResponseActivity.this);
        progressDialog.setMessage("Calling Gmail API ...");
        new MakeMessageRequestTask(googleCredential).execute();
    }

    public void writeAuthState(@NonNull AuthState state) {

        SharedPreferences authPrefs = getSharedPreferences(context.getResources().getString(R.string.AuthSharedPref), MODE_PRIVATE);
        authPrefs.edit()
                .putString(context.getResources().getString(R.string.StateJson), state.jsonSerializeString())
                .apply();
    }

    private class MakeMessageRequestTask extends AsyncTask<Void, Void, List<Message>> {

        private com.google.api.services.gmail.Gmail service = null;
        private Exception lastError = null;

        MakeMessageRequestTask(GoogleCredential credential) {

            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            service = new com.google.api.services.gmail.Gmail.Builder(
                    transport, jsonFactory, credential
            ).setApplicationName(getResources().getString(R.string.GmailApi)).build();
        }

        @Override
        protected List<Message> doInBackground(Void... params) {

            try {
                return getCurrentDayMessagesFromApi();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (Exception c) {
                lastError = c;
                cancel(true);
                return null;
            }
        }

        private List<Message> getCurrentDayMessagesFromApi() throws IOException {

            String user = "harshit.bansalec@gmail.com";
            List<String> messagesId = new ArrayList<String>();
            ArrayList<Message> currentDayMessages = new ArrayList<>();

            ListMessagesResponse listMessagesResponse = service.users().messages().list(user).execute();
            for (Message message : listMessagesResponse.getMessages())
                //    currentDayMessages.add(message);

                messagesId.add(message.getId());

            for (String messageId : messagesId) {
                Message message =
                        service.users().messages().get(user, messageId).execute();

                String date = message.getPayload().getHeaders().get(2).getValue().split(",")[1];
                String[] words = date.split("\\s");

                SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
                String day = dayFormat.format(Calendar.getInstance().getTime());

                SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
                String month = monthFormat.format(Calendar.getInstance().getTime());

                SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
                String year = yearFormat.format(Calendar.getInstance().getTime());

                if (words[1].equals("13") && words[2].equals(month) && words[3].equals(year)) {
                    currentDayMessages.add(message);
                } else
                    break;
            }
            return currentDayMessages;
        }

        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(List<Message> output) {

            progressDialog.hide();
            if (output.size() == 0) {
                //responseText.setText(getResources().getString(R.string.NoMessagesResponseDisplayText));
            } else {
                currentDayMessages = output;
                List<String> senders = new ArrayList<>();
                int index = 0;
                for (Message message : output) {
                    for(int i=0; i<output.get(index).getPayload().getHeaders().size(); i++){
                        String name = output.get(index).getPayload().getHeaders().get(i).getName();
                        if(name.equals("From")){
                            String sender = output.get(index).getPayload().getHeaders().get(i).getValue();
                            hashTable.insert(sender, message);
                            break;
                        }
                    }
                    index++;
                }
                for(int i = 0; i < HASH_TABLE_SIZE; i++) {
                    if (hashTable.keys[i] != null) {

                        List<Message> list  = hashTable.vals.get(i);
                        currentDayMessageSendersList.add(new CurrentDayMessageSendersList(hashTable.keys[i], hashTable.vals.get(i)));
                    }
                }
                formMessagesGridView(currentDayMessageSendersList.size());

                emailTilesAdapter = new EmailTilesAdapter(ResponseActivity.this, currentDayMessageSendersList);
                emailNameInitialRecycler.setAdapter(emailTilesAdapter);
                emailNameInitialRecycler.setLayoutManager(new LinearLayoutManager(ResponseActivity.this));
                emailGridViewAdapter = new EmailGridViewAdapter(ResponseActivity.this, currentDayMessageSendersList);
                emailNameInitialGridView.setAdapter(emailGridViewAdapter);
                emailNameInitialGridView.setExpanded(true);

            }
        }

        @Override
        protected void onCancelled() {
            progressDialog.hide();
        }
    }

    public void formMessagesGridView(int count) {

        int numberOfRows = (int) Math.ceil((float)count / 4);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        for (int i = 0, k = 0; i < numberOfRows; i++) {

            int linearLayoutId = Integer.parseInt("1" + Integer.toString(i+1));
            LinearLayout l1 = new LinearLayout(this);
            l1.setLayoutParams(params);
            l1.setOrientation(LinearLayout.HORIZONTAL);
            l1.setId(linearLayoutId);
            for (int j = 0; j < 4 && k < count; j++,k++){
                List<CurrentDayMessageSendersList> list = new ArrayList<>();
                list.add(currentDayMessageSendersList.get(k));
                emailTilesAdapter = new EmailTilesAdapter(this, this, list, i+1, j+1);
                RecyclerView m1 = new RecyclerView(this);
                int recyclerViewId = Integer.parseInt("2" + Integer.toString(i + 1) + Integer.toString(j + 1));
                m1.setId(recyclerViewId);
                m1.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                l1.addView(m1);
                m1.setAdapter(emailTilesAdapter);
                m1.setLayoutManager(new LinearLayoutManager(this));
            }
            linearLayout.addView(l1);
        }
    }
    */

