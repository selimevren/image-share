package com.stjerneklart.imgshare.ui.photo;

import static android.content.ContentValues.TAG;
import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.stjerneklart.imgshare.R;
import com.stjerneklart.imgshare.databinding.FragmentPhotoBinding;
import com.stjerneklart.imgshare.ui.label.Label;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class PhotoFragment extends Fragment {
        private FragmentPhotoBinding binding;
        private int ResultImage = 1;
     List<String> firebaseLabelList = new ArrayList<>();
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            binding = FragmentPhotoBinding.inflate(inflater, container, false);
            View root = binding.getRoot();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference labelRef = db.collection("label");
            labelRef.addSnapshotListener((snapshots, e)-> {
                if(e != null){
                    return;
                    }

                firebaseLabelList.clear();

                LinearLayout layout = binding.linearLayout;
                layout.removeAllViews();

                for(QueryDocumentSnapshot document : snapshots){
                    Label label = document.toObject(Label.class);
                    CheckBox checkBox = new CheckBox(getActivity());
                    checkBox.setText(label.getLabelText());
                    layout.addView(checkBox);

                    firebaseLabelList.add(label.getLabelText());
                }
            });

            Button btnselect = binding.select;
            btnselect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent i = new Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, ResultImage);
                }
            });

            return root;

        }
    private List<String> getSelectedLabels() {
        List<String> selectedLabels = new ArrayList<>();
        LinearLayout layout = binding.linearLayout;

        for (int i = 0; i< layout.getChildCount(); i++){
            View view = layout.getChildAt(i);
            if(view instanceof  CheckBox){
                CheckBox checkBox = (CheckBox) view;
                if (checkBox.isChecked()){
                    selectedLabels.add(checkBox.getText().toString());
                }
            }
        }
        return selectedLabels;
    }

        @Override
        public void onActivityResult( int requestCode, int resultCode, Intent data ) {
            super.onActivityResult(requestCode, resultCode , data);
            if(requestCode == ResultImage && resultCode == RESULT_OK && null != data ){
                Uri selectedImage = data.getData();
                String [] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getActivity().getContentResolver().query(selectedImage , filePathColumn, null , null , null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();


                ImageView imageView = binding.imgView;
                imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));


                Button share = binding.share;

                share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseStorage storage =   FirebaseStorage.getInstance();
                        StorageReference storageRef = storage.getReference();
                        StorageReference photoRef = storageRef.child("posts/" + UUID.randomUUID().toString());

                        UploadTask uploadTask = photoRef.putFile(selectedImage);

                        FirebaseAuth auth = FirebaseAuth.getInstance();

                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                while(!uriTask.isSuccessful());
                                Uri downloadUrl = uriTask.getResult();

                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                DocumentReference userRef = db.collection("users").document(auth.getUid());

                                userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        String name = documentSnapshot.getString("name");

                                        Map <String, Object> post = new HashMap<>();
                                        post.put("imageUrl", downloadUrl.toString());
                                        post.put("name", name);

                                        List <String> selectedLabels = getSelectedLabels();

                                        post.put("label", selectedLabels);

                                        db.collection("posts").document().set(post).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Log.d(TAG, "Başarılı bir şekilde yüklendi");
                                                Toast.makeText(getActivity(), "Başarılı yükleme işlemi", Toast.LENGTH_SHORT).show();


                                            }
                                        })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.d(TAG, "Başarısız bir şekilde yüklendi");
                                                        Toast.makeText(getActivity(), "Başarısız yükleme işlemi", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                });

                            }
                        });

                    }
                });

            }
        }

        @Override
    public  void onDestroyView(){
            super.onDestroyView();
            binding = null;
        }
}













