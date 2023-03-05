package com.fyp.bambino;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SplashScreen extends AppCompatActivity {

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    long nbSubStrings = 0;
    int countRecievedSubStrings = 0;
    DataSnapshot previousSnapshot = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
//        databaseReference.child("/FlashLED").setValue(true);
//        databaseReference.child("/c").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DataSnapshot> task) {
//                if (!task.isSuccessful()) {
//                    Log.e("firebase", "Error getting data", task.getException());
//                }
//                else {
//                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
//                }
//            }
//        });

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                if(dataSnapshot.hasChild("c")){
                    if(dataSnapshot.hasChild("uploading")&& !(boolean) dataSnapshot.child("uploading").getValue()){
                        renderToImageView(dataSnapshot);
                    }
                }
                Log.i("POST", String.valueOf(dataSnapshot.getValue()));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        databaseReference.addValueEventListener(postListener);
    }

    private void renderToImageView(DataSnapshot dataSnapshot) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        for (long i = 0; i < (long)dataSnapshot.child("nbSubStrings").getValue(); i++) {
            String base64Chunk = (String) dataSnapshot.child("c").child("p" + i).getValue();
            Log.i("DECODING: ", String.valueOf(i));
            Log.i("DECODING: ", (String) dataSnapshot.child("c").child("p" + i).getValue());
            byte[] decodedBytes = Base64.decode(base64Chunk, Base64.DEFAULT);
            try {
                outputStream.write(decodedBytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        String base64Chunk = (String) dataSnapshot.child("c").child("pL").getValue();
        byte[] decodedBytes = Base64.decode(base64Chunk, Base64.DEFAULT);
        try {
            outputStream.write(decodedBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte fullDecodedBytes[] = outputStream.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(fullDecodedBytes, 0, fullDecodedBytes.length);

        ImageView imageView = findViewById(R.id.live_video);

        Matrix matrix = new Matrix();

        matrix.postRotate(90);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);

        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

        imageView.setImageBitmap(rotatedBitmap);
    }
}