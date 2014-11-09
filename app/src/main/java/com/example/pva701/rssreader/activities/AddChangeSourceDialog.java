package com.example.pva701.rssreader.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.pva701.rssreader.R;

/**
 * Created by pva701 on 18.10.14.
 */
public class AddChangeSourceDialog extends DialogFragment {
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_URL = "source";

    private void sendResult(String name, String url) {
        if (getTargetFragment() == null)
            return;
        Intent intent = new Intent();
        intent.putExtra(EXTRA_NAME, name);
        intent.putExtra(EXTRA_URL, url);
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String name = "", url = "";
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        if (getArguments() != null) {
            name = (getArguments().getString(EXTRA_NAME)  == null ? "" : getArguments().getString(EXTRA_NAME));
            url = (getArguments().getString(EXTRA_URL)  == null ? "" : getArguments().getString(EXTRA_URL));
            builder.setTitle("Change source");
        } else
            builder.setTitle("New source");

        final View v = inflater.inflate(R.layout.dialog_add_source, null);
        ((EditText)v.findViewById(R.id.dialog_source_name)).setText(name);
        ((EditText)v.findViewById(R.id.dialog_source_url)).setText(url);

        builder.setView(v)
                .setPositiveButton(R.string.ok_source, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        sendResult(((EditText)v.findViewById(R.id.dialog_source_name)).getText().toString(),
                                ((EditText)v.findViewById(R.id.dialog_source_url)).getText().toString());
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AddChangeSourceDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}
