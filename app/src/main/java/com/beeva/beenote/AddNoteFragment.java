package com.beeva.beenote;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;
import com.beeva.beenote.models.MetaNoteMessage;
import com.beeva.beenote.models.MetaNoteResponse;
import com.beeva.beenote.models.Note;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;

/**
 * Created by marianclaudiu on 25/05/15.
 */
public class AddNoteFragment extends DialogFragment{

    @InjectView(R.id.addNoteTitle)
    EditText editTextTitle;

    @InjectView(R.id.addNoteContent)
    EditText editTextContent;

    private View mainView;
    private CognitoCachingCredentialsProvider credentialsProvider;
    private NoteProxy noteInterface;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainView = getLayoutInflater(savedInstanceState).inflate(R.layout.fragment_add_note, null, false);
        ButterKnife.inject(this, mainView);

        credentialsProvider = new CognitoCachingCredentialsProvider(getActivity(), MainActivity.COGNITO_POOL_ID, Regions.EU_WEST_1);
        LambdaInvokerFactory factory = new LambdaInvokerFactory(getActivity(), Regions.EU_WEST_1, credentialsProvider);
        noteInterface = factory.build(NoteProxy.class);
    }

    @Override
    public void onStart() {
        super.onStart();
        ((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                getDataAndCreateThing();
            }
        });
    }

    @NonNull
    @Override
    public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
        AppCompatDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle("Create Note")
                .setView(mainView)
                .setPositiveButton("Create", null
                )
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                ).setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        dialog.dismiss();

                    }
                }).setCancelable(false)
                .create();

        return dialog;
    }

    private void getDataAndCreateThing() {
        if(TextUtils.isEmpty(editTextTitle.getText())){
            editTextTitle.setError("Not empty... please");
            return;
        }
        if(TextUtils.isEmpty(editTextContent.getText())){
            editTextContent.setError("Not empty... please");
            return;
        }

        Note note = new Note(editTextTitle.getText().toString(), editTextContent.getText().toString());
        MetaNoteMessage metaMessage = new MetaNoteMessage(MetaNoteMessage.Operation.CREATE, note);
        new InvokeLambdaCreate().execute(metaMessage);
    }

    class InvokeLambdaCreate extends AsyncTask<MetaNoteMessage, Void, Note> {

        @Override
        protected Note doInBackground(MetaNoteMessage... params) {
            try {
                MetaNoteResponse response = noteInterface.invoke(params[0]);
                return params[0].getNote();
            } catch (LambdaFunctionException lfe) {
                String errorJson = lfe.getDetails();
                try {
                    JSONObject object = new JSONObject(errorJson);
                    Snackbar.make(mainView, object.getString("errorMessage"), Snackbar.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return null;
            }
        }

        @Override
        protected void onPostExecute(Note note) {
            if(note != null){
                EventBus.getDefault().post(note);
            }
            else Toast.makeText(mainView.getContext(), "Something bad happened...", Toast.LENGTH_SHORT).show();
            getDialog().dismiss();
        }
    }


    public LayoutInflater getLayoutInflater(Bundle savedInstanceState) {
        try {
            Field mDialog = DialogFragment.class.getDeclaredField("mDialog");
            mDialog.setAccessible(true);
            mDialog.set(this, onCreateDialog(savedInstanceState));
            return (LayoutInflater)((Dialog)mDialog.get(this)).getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



}
