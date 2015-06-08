package com.beeva.beenote;

import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;
import com.beeva.beenote.models.MetaNoteMessage;
import com.beeva.beenote.models.MetaNoteResponse;
import com.beeva.beenote.models.Note;
import com.beeva.beenote.viewutils.DividerItemDecoration;
import com.beeva.beenote.viewutils.NotesAdapter;
import com.beeva.beenote.viewutils.OnRecyclerItemClick;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;

public class MainActivity extends AppCompatActivity {

    @InjectView(R.id.toolBar)
    Toolbar toolbar;

    @InjectView(R.id.floatingButton)
    FloatingActionButton floatingActionButton;

    @InjectView(R.id.recycler)
    RecyclerView recyclerView;

    public static final String TAG = "BeeNote";
    public static final String COGNITO_POOL_ID = "** SOME COOL COGNITO POOL ID **";

    private CognitoCachingCredentialsProvider credentialsProvider;
    private NoteProxy noteInterface;

    private NotesAdapter notesAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);
        
        setUpCredentials();
        setUpViews();
        fetchNotes();
        
        EventBus.getDefault().register(this);

    }

    private void fetchNotes() {
        new InvokeLambda().execute(new MetaNoteMessage(MetaNoteMessage.Operation.FETCH, null));
    }

    private void setUpCredentials(){
        // Initialize the Amazon Cognito credentials provider
        credentialsProvider = new CognitoCachingCredentialsProvider(this, COGNITO_POOL_ID, Regions.EU_WEST_1);
        LambdaInvokerFactory factory = new LambdaInvokerFactory(this, Regions.EU_WEST_1, credentialsProvider);
        noteInterface = factory.build(NoteProxy.class);
    }

    private void setUpViews() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);

        //LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        recyclerView.setLayoutManager(layoutManager);

        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecoration(this);
        recyclerView.addItemDecoration(itemDecoration);

        recyclerView.setItemAnimator(new SlideInLeftAnimator());

        notesAdapter = new NotesAdapter(new ArrayList<Note>(), new OnRecyclerItemClick<Note>() {
            @Override
            public void onItemClick(int position, Note note) {

            }

            @Override
            public void onItemLongClick(int position, Note note) {
                new InvokeLambdaDelete().execute(new MetaNoteMessage(MetaNoteMessage.Operation.REMOVE, note));
            }
        });
        recyclerView.setAdapter(notesAdapter);
    }

    public void onEvent(Note note) {
        Log.wtf(TAG, note.getTitle());
        notesAdapter.addItem(note);
    }

    

    class InvokeLambda extends AsyncTask<MetaNoteMessage, Void, MetaNoteResponse>{

        @Override
        protected MetaNoteResponse  doInBackground(MetaNoteMessage... params) {
            try {
                MetaNoteResponse response = noteInterface.invoke(params[0]);
                if(response == null)
                    return new MetaNoteResponse();
                else return response;
            } catch (LambdaFunctionException lfe) {
                String errorJson = lfe.getDetails();
                try {
                    JSONObject object = new JSONObject(errorJson);
                    Snackbar.make(floatingActionButton, object.getString("errorMessage"), Snackbar.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                return null;
            }
        }

        @Override
        protected void onPostExecute(MetaNoteResponse s) {
            if(s != null){
                for (Note note : s.getObjects()) {
                    notesAdapter.addItem(note);
                    Log.e(note.getTitle(), note.getContent());
                }
            }
        }
    }

    class InvokeLambdaDelete extends AsyncTask<MetaNoteMessage, Void, Note>{

        @Override
        protected Note  doInBackground(MetaNoteMessage... params) {
            try {
                MetaNoteResponse response = noteInterface.invoke(params[0]);
                return params[0].getNote();
            } catch (LambdaFunctionException lfe) {
                String errorJson = lfe.getDetails();
                try {
                    JSONObject object = new JSONObject(errorJson);
                    Snackbar.make(floatingActionButton, object.getString("errorMessage"), Snackbar.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                return null;
            }
        }

        @Override
        protected void onPostExecute(Note s) {
            if(s != null){
                notesAdapter.deleteItem(s);
            }
        }
    }



    @OnClick(R.id.floatingButton)
    public void click(){
        AddNoteFragment newFragment = new AddNoteFragment();
        newFragment.show(getSupportFragmentManager(), "addNote");
    }

}
