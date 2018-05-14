package com.petyachoeva.motoassistant;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class ContactDialog extends AppCompatDialogFragment {

    private EditText editTextContactName;
    private EditText editTextContactPhone;
    private ContactDialogListener listener;
    static private String c_name;
    static private String c_phone;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_contact_dialog, null);

        editTextContactName = view.findViewById(R.id.EditTextContactName);
        editTextContactPhone = view.findViewById(R.id.EditTextContactPhone);
        if(c_name != null && c_phone != null) {
            editTextContactName.setText(c_name);
            editTextContactPhone.setText(c_phone);
        }

        builder.setView(view)
                .setTitle("Edit Contact")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String contactName = editTextContactName.getText().toString();
                        String contactPhone = editTextContactPhone.getText().toString();

                        listener.applyTexts(contactName, contactPhone);
                    }
                });


        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        listener = (ContactDialogListener) getTargetFragment();
    }

    public void setContactName(String cont_name){
        c_name = cont_name;
    }

    public void setContactPhone(String cont_phone) {
        c_phone = cont_phone;
    }

    public interface ContactDialogListener{
        void applyTexts(String contactName, String contactPhone);
    }
}
