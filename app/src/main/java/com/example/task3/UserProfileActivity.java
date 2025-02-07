package com.example.task3;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {
    private EditText name;
    private Button btnSave, btnAddRecipe;
    private ImageView profileImage;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> recipeList;

    private static final int IMAGE_PICKER_REQUEST = 1001; // For image selection

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        name = findViewById(R.id.name);
        btnSave = findViewById(R.id.btnSave);
        btnAddRecipe = findViewById(R.id.btnAddRecipe);
        profileImage = findViewById(R.id.profileImage);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recipeList = new ArrayList<>();
        recipeAdapter = new RecipeAdapter(recipeList);
        recyclerView.setAdapter(recipeAdapter);

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            try {
                loadUserProfile(user.getUid());
                loadUserRecipes(user.getUid());
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show();
            }
        }

        // Click listener for profile image to open image picker
        profileImage.setOnClickListener(v -> openImagePicker());

        // Save button logic
        btnSave.setOnClickListener(v -> {
            try {
                saveUserProfile();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error saving profile", Toast.LENGTH_SHORT).show();
            }
        });

        btnAddRecipe.setOnClickListener(v -> startActivity(new Intent(UserProfileActivity.this, AddRecipeActivity.class)));
    }

    private void loadUserProfile(String userId) throws Exception {
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String userName = documentSnapshot.getString("name");
                String profileBase64 = documentSnapshot.getString("profileImage"); // Now it's Base64 string
                name.setText(userName);
                if (profileBase64 != null) {
                    // Convert Base64 string to Bitmap and display
                    Bitmap bitmap = convertBase64ToBitmap(profileBase64);
                    if (bitmap != null) {
                        profileImage.setImageBitmap(bitmap);
                    } else {
                        Toast.makeText(UserProfileActivity.this, "Error displaying profile image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }).addOnFailureListener(e -> {
            try {
                throwException("Error loading user profile");
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    // Save the user profile to Firebase
    private void saveUserProfile() throws Exception {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) throw new Exception("User is not authenticated");

        String userId = user.getUid();
        String userName = name.getText().toString().trim();

        if (userName.isEmpty()) {
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the existing profile image from Firestore
        final String[] profileImageBase64 = {null};

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retain the existing profile image if no new image is selected
                        String existingProfileImage = documentSnapshot.getString("profileImage");
                        profileImageBase64[0] = existingProfileImage;  // Use existing image if no new one is selected
                    }

                    // Check if a new profile image is selected
                    Uri imageUri = (Uri) profileImage.getTag();  // Get the selected image Uri
                    if (imageUri != null) {
                        try {
                            // Convert the selected image to Base64
                            profileImageBase64[0] = convertImageToBase64(imageUri);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // Save the profile data to Firestore
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("name", userName);
                    if (profileImageBase64[0] != null) {
                        userMap.put("profileImage", profileImageBase64[0]);  // Store the Base64 string for profile image
                    }

                    db.collection("users").document(userId).set(userMap)
                            .addOnSuccessListener(aVoid -> Toast.makeText(UserProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> {
                                Toast.makeText(UserProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                            });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UserProfileActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                });
    }


    // Convert image to Base64 String
    private String convertImageToBase64(Uri imageUri) throws Exception {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (FileNotFoundException e) {
            throw new Exception("Image not found: " + e.getMessage());
        }
    }

    // Convert Base64 String to Bitmap
    private Bitmap convertBase64ToBitmap(String base64String) {
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Open Image Picker to select an image for profile
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICKER_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICKER_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                profileImage.setTag(selectedImageUri);  // Save the Uri to use later for conversion
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImageUri));
                    profileImage.setImageBitmap(bitmap);  // Display the image
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Load the user's recipes from Firebase
    private void loadUserRecipes(String uid) {
        db.collection("recipes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        recipeList.clear();  // Clear the old list
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String title = document.getString("title");
                            String ingredients = document.getString("ingredients");
                            String instructions = document.getString("instructions");

                            // Add recipe to the list
                            recipeList.add(new Recipe(title, ingredients, instructions));
                        }
                        recipeAdapter.notifyDataSetChanged(); // Notify the adapter that data has changed
                    } else {
                        Log.e("UserProfileActivity", "Error getting recipes: ", task.getException());
                        Toast.makeText(UserProfileActivity.this, "Error loading recipes", Toast.LENGTH_SHORT).show();
                    }
                });
    }




    private void throwException(String message) throws Exception {
        throw new Exception(message);
    }
}
