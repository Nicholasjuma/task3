package com.example.task3;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddRecipeActivity extends AppCompatActivity {
    private EditText title, ingredients, instructions;
    private Button btnAddRecipe;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        db = FirebaseFirestore.getInstance();

        title = findViewById(R.id.title);
        ingredients = findViewById(R.id.ingredients);
        instructions = findViewById(R.id.instructions);
        btnAddRecipe = findViewById(R.id.btnAddRecipe);

        // Add recipe
        btnAddRecipe.setOnClickListener(v -> addRecipe());
    }

    private void addRecipe() {
        String recipeTitle = title.getText().toString().trim();
        String recipeIngredients = ingredients.getText().toString().trim();
        String recipeInstructions = instructions.getText().toString().trim();

        // Check if fields are empty
        if (recipeTitle.isEmpty() || recipeIngredients.isEmpty() || recipeInstructions.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare the recipe data
        Map<String, Object> recipe = new HashMap<>();
        recipe.put("title", recipeTitle);
        recipe.put("ingredients", recipeIngredients);
        recipe.put("instructions", recipeInstructions);

        // Add recipe to Firestore
        db.collection("recipes").add(recipe)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Recipe added", Toast.LENGTH_SHORT).show();
                    finish();  // Close the activity
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add recipe", Toast.LENGTH_SHORT).show());
    }
}
