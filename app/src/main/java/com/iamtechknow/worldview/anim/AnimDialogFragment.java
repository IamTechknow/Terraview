package com.iamtechknow.worldview.anim;

import android.app.ActionBar;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toolbar;

import com.iamtechknow.worldview.R;

public class AnimDialogFragment extends DialogFragment implements View.OnClickListener {
    public static final String ANIM_ARG = "anim";

    public interface Listener {
        void onDialogResult();
    }

    private TextView from, to;
    private RadioButton day, month, year;
    private CheckBox loop, saveGIF;

    private Listener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_anim, container, false);
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.tool_bar);
        getActivity().setActionBar(toolbar);

        ActionBar actionBar = getActivity().getActionBar();
        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        from = (TextView) rootView.findViewById(R.id.from_date);
        to = (TextView) rootView.findViewById(R.id.to_date);
        from.setOnClickListener(this);
        to.setOnClickListener(this);

        day = (RadioButton) rootView.findViewById(R.id.day_button);
        month = (RadioButton) rootView.findViewById(R.id.month_button);
        year = (RadioButton) rootView.findViewById(R.id.year_button);

        loop = (CheckBox) rootView.findViewById(R.id.loop_checkbox);
        saveGIF = (CheckBox) rootView.findViewById(R.id.save_checkbox);

        //Get date and set text
        String date = getArguments().getString(ANIM_ARG);
        from.setText(date);

        setHasOptionsMenu(true);
        return rootView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.anim, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                dismiss();
                break;
            case R.id.anim_start:
                mListener.onDialogResult();
                dismiss();
        }
        return true;
    }

    @Override
    public void onClick(View v) {

    }

    public void setListener(Listener listener) {
        mListener = listener;
    }
}
