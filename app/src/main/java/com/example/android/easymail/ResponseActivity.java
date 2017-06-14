package com.example.android.easymail;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.easymail.adapters.EmailGridViewAdapter;
import com.example.android.easymail.adapters.EmailTilesAdapter;
import com.example.android.easymail.interactor.ResponseInteractorImpl;
import com.example.android.easymail.models.CurrentDayMessageSendersList;
import com.example.android.easymail.models.HashTable;
import com.example.android.easymail.presenter.ResponsePresenterImpl;
import com.example.android.easymail.view.ResponseActivityView;
import com.example.android.easymail.views.ExpandableGridView;
import com.google.api.services.gmail.model.Message;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.gmail.GmailScopes;

import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import java.util.ArrayList;
import java.util.List;

public class ResponseActivity extends AppCompatActivity implements SenderNameInitialClickListener, ResponseActivityView {

    Context context = this;
    private String accessToken;
    GoogleAccountCredential credential;
    GoogleCredential googleCredential;
    public static final String[] scopes = {GmailScopes.GMAIL_READONLY};
    private LinearLayout linearLayout;
    private TextView responseText;
    private TextView tokenText;
    private RecyclerView emailNameInitialRecycler;
    private ProgressDialog progressDialog;
    private HashTable hashTable;
    private List<Message> currentDayMessages;
    private List<CurrentDayMessageSendersList> currentDayMessageSendersList;
    private static final int HASH_TABLE_SIZE = 100;
    private EmailTilesAdapter emailTilesAdapter;
    private ExpandableGridView emailNameInitialGridView;
    private EmailGridViewAdapter emailGridViewAdapter;
    ResponsePresenterImpl responsePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response);
        initViews();
        regListeners();
        responsePresenter = new ResponsePresenterImpl(this, new ResponseInteractorImpl(), ResponseActivity.this, getApplication());
        final AuthorizationResponse response = AuthorizationResponse.fromIntent(getIntent());
        final AuthorizationException exception = AuthorizationException.fromIntent(getIntent());
        String isAutoSignedInToken = getIntent().getExtras().getString("is_auto_signed_in_token");
        responsePresenter.performTokenRequest(response, isAutoSignedInToken);
    }

    private void initViews(){
        linearLayout = (LinearLayout) findViewById(R.id.layout);
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

        ProgressDialog dialog = new ProgressDialog(ResponseActivity.this);
        dialog.setMessage("Auto Signing In!");
        dialog.show();
    }

    @Override
    public void showTokenRequestDialog() {

        ProgressDialog dialog = new ProgressDialog(ResponseActivity.this);
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
    public void formRecyclerView(List<CurrentDayMessageSendersList> list, int i, int j, RecyclerView recyclerView) {

        emailTilesAdapter = new EmailTilesAdapter(this, this, list, i + 1, j + 1);
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

