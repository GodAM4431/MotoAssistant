package com.petyachoeva.motoassistant;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.google.android.gms.internal.zzs.TAG;

public class emergencyNumbersFragment extends Fragment implements ContactDialog.ContactDialogListener {

    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference ref;
    private TextView textViewContactPhone;
    private TextView textViewContactName;
    Contact contact;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_emergency_numbers, null);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.getActivity().setTitle("Emergency numbers");
        textViewContactPhone = view.findViewById(R.id.textViewContactPhone);
        textViewContactName = view.findViewById(R.id.textViewContactName);
        contact = new Contact();

        mDatabase = FirebaseDatabase.getInstance();
        ref = mDatabase.getReference("emergency-numbers").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        ref.addValueEventListener(new ValueEventListener() {


            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                contact.setContact_name(dataSnapshot.child("contact_name").getValue(String.class));
                contact.setContact_phone(dataSnapshot.child("contact_phone").getValue(String.class));
                Log.d("TAG", contact.getContact_name() + " / " + contact.getContact_phone());
                if(contact != null){
                    textViewContactName.setText(contact.getContact_name());
                    textViewContactPhone.setText(contact.getContact_phone());
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });

        view.findViewById(R.id.card112).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = "112";
                Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                startActivity(phoneIntent);
            }
        });

        textViewContactPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textViewPersonal = (TextView) view.findViewById(R.id.textViewContactPhone);
                String phone = textViewPersonal.getText().toString();
                Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                startActivity(phoneIntent);
            }
        });

        view.findViewById(R.id.imageViewEditContact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });
    }

    public void openDialog() {
        ContactDialog contactDialog = new ContactDialog();
        contactDialog.setTargetFragment(emergencyNumbersFragment.this, 1);
        contactDialog.show(getFragmentManager(), "My Contact Dialog");
        if(contact != null) {
            contactDialog.setContactName(contact.getContact_name());
            contactDialog.setContactPhone(contact.getContact_phone());
        }

    }

    @Override
    public void applyTexts(String contactName, String contactPhone) {
        textViewContactName.setText(contactName);
        textViewContactPhone.setText(contactPhone);
        FirebaseDatabase database =  FirebaseDatabase.getInstance();

        Log.d("Current user:", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mRef =  database.getReference().child("emergency-numbers").child(userId);
        mRef.child("contact_name").setValue(contactName);
        mRef.child("contact_phone").setValue(contactPhone);
    }
}
