package com.example.shoppinglist.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.shoppinglist.database.ProductDao;
import com.example.shoppinglist.database.ProductDatabase;
import com.example.shoppinglist.entities.ProductEntity;
import com.example.shoppinglist.R;
import com.example.shoppinglist.database.ShoppingListDao;
import com.example.shoppinglist.entities.ShoppingListEntity;
import com.example.shoppinglist.adapter.ProductAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CreationListActivity extends AppCompatActivity {

    private TextView editTextListName;
    private SharedPreferences sharedPreferences;
    private static final String SHARED_PREF_NAME = "MySharedPref";
    private ShoppingListDao shoppingListDao;
    private RecyclerView recyclerView;  // RecyclerView per mostrare i prodotti
    private ProductDao productDao;
    private static final int NEW_PRODUCT_REQUEST_CODE = 1;  // codice della richiesta

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creation_list);

        editTextListName = findViewById(R.id.textView_list);
        // recupera il nome della lista salvato nelle SharedPreferences
        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        String savedListName = sharedPreferences.getString("LIST_NAME", "");
        editTextListName.setText(savedListName);

        // inizializza il ProductDao
        ProductDatabase productDatabase = Room.databaseBuilder(getApplicationContext(),
                        ProductDatabase.class, "product_database")
                .build();
        productDao = productDatabase.productDao();

        recyclerView = findViewById(R.id.recycler_view_products);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        shoppingListDao = productDatabase.shoppingListDao();
        // caricamento dei prodotti su un thread separato
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<ProductEntity> products = productDao.getProductsByListName(savedListName);
            runOnUiThread(() -> {
                if (products == null) {
                    updateUI(new ArrayList<>());
                } else {
                    updateUI(products);
                }
            });
        });
    }

    // metodo per aggiornare l'interfaccia utente con i prodotti recuperati
    private void updateUI(List<ProductEntity> products) {
        // aggiungi l'adattatore per la RecyclerView
        ProductAdapter productAdapter = new ProductAdapter(products, false);
        recyclerView.setAdapter(productAdapter);
    }

    public void saveList(View view) {
        String listName = editTextListName.getText().toString().trim();

        if (!listName.isEmpty()) {
            new Thread(() -> {
                ShoppingListEntity existingList = shoppingListDao.getShoppingListByName(listName);

                runOnUiThread(() -> {
                    if (existingList == null) {
                        Toast.makeText(this, "Attenzione! inserisci almeno un prodotto", Toast.LENGTH_SHORT).show();
                    } else {
                        new Thread(() -> {
                            List<ProductEntity> products = productDao.getProductsByListName(listName);
                            runOnUiThread(() -> {
                                if (products != null && !products.isEmpty()) {
                                    Toast.makeText(this, "Lista salvata", Toast.LENGTH_SHORT).show();
                                    navigateToMainActivity();
                                } else {
                                    Toast.makeText(this, "Attenzione! inserisci almeno un prodotto", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }).start();
                    }
                });
            }).start();
        } else {
            Toast.makeText(this, "Inserisci un nome per la lista prima di salvare", Toast.LENGTH_SHORT).show();
        }
    }

    public void startNewProductActivity(View view) {
        String listName = editTextListName.getText().toString().trim();

        // salva il nome della lista nelle SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("LIST_NAME", listName);
        editor.apply();

        new Thread(() -> {
            ShoppingListEntity existingList = shoppingListDao.getShoppingListByName(listName);

            runOnUiThread(() -> {
                if (existingList != null) {
                    new Thread(() -> runOnUiThread(() -> {
                        Intent intent = new Intent(this, NewProductActivity.class);
                        intent.putExtra("LIST_NAME", existingList.getListName());
                        intent.putExtra("ADD_PRODUCT_FROM_DETAIL", false);
                        startActivityForResult(intent, NEW_PRODUCT_REQUEST_CODE);
                    })).start();
                } else {
                    // se la lista non esiste, crea una nuova lista e avvia NewProductActivity
                    ShoppingListEntity newList = new ShoppingListEntity(listName);
                    new Thread(() -> {
                        shoppingListDao.insertShoppingList(newList);

                        runOnUiThread(() -> {
                            Intent intent = new Intent(this, NewProductActivity.class);
                            intent.putExtra("LIST_NAME", newList.getListName());
                            intent.putExtra("ADD_PRODUCT_FROM_DETAIL", false);
                            startActivityForResult(intent, NEW_PRODUCT_REQUEST_CODE);
                        });
                    }).start();
                }
            });
        }).start();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        showExitConfirmationDialog();
    }

    public void goBackMainActivityFromCreation(View view) {
        showExitConfirmationDialog();
    }

    private void showExitConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Attenzione");
        builder.setMessage("Sei sicuro di voler uscire? Comporterà la perdita della lista.");

        builder.setPositiveButton("Sì", (dialogInterface, i) -> {
            // elimina la lista e torna alla MainActivity
            deleteListAndNavigateToMain();
        });

        builder.setNegativeButton("No", (dialogInterface, i) -> {
            // l'utente ha scelto di rimanere nella CreationListActivity
            dialogInterface.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteListAndNavigateToMain() {
        String listName = editTextListName.getText().toString().trim();

        if (!listName.isEmpty()) {
            ProductDatabase db = Room.databaseBuilder(getApplicationContext(),
                    ProductDatabase.class, "product_database").build();

            new Thread(() -> {
                ShoppingListEntity listToDelete = db.shoppingListDao().getShoppingListByName(listName);

                if (listToDelete != null) {
                    db.runInTransaction(() -> {
                        db.shoppingListDao().deleteShoppingList(listToDelete);
                        db.productDao().deleteProductsByListName(listToDelete.getListName());
                    });
                }

                runOnUiThread(this::navigateToMainActivity);
            }).start();
        } else {
            navigateToMainActivity();
        }
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NEW_PRODUCT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    String result = data.getStringExtra("RESULT");
                    if (result != null && result.equals("BACK_PRESSED")) {
                        Toast.makeText(this, "Il prodotto non è stato salvato", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
}