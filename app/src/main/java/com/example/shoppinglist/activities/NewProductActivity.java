package com.example.shoppinglist.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.shoppinglist.database.ProductDao;
import com.example.shoppinglist.database.ProductDatabase;
import com.example.shoppinglist.entities.ProductEntity;
import com.example.shoppinglist.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.Objects;

public class NewProductActivity extends AppCompatActivity {

    private EditText productNameEditText;
    private EditText priceEditText;
    private EditText quantityEditText;
    private ChipGroup chipGroupCategories;
    private ProductDao productDao;
    private String productNameOriginal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_product_activity);

        productNameEditText = findViewById(R.id.edit_text_product_name);
        priceEditText = findViewById(R.id.edit_text_price);
        quantityEditText = findViewById(R.id.edit_text_quantity);
        chipGroupCategories = findViewById(R.id.chip_group_categories);

        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("EDIT_PRODUCT", false)) {
            String productName = intent.getStringExtra("PRODUCT_NAME");
            String productPrice = intent.getStringExtra("PRODUCT_PRICE");
            String productQuantity = intent.getStringExtra("PRODUCT_QUANTITY");
            String productCategory = intent.getStringExtra("PRODUCT_CATEGORY");

            productNameOriginal = productName;
            productNameEditText.setText(productName);
            assert productPrice != null;
            if (productPrice.equals("non specificato")){
                priceEditText.setText("");
            } else {
                priceEditText.setText(productPrice);
            }
            quantityEditText.setText(productQuantity);

            // imposta la categoria selezionata nel ChipGroup
            int chipCount = chipGroupCategories.getChildCount();
            for (int i = 0; i < chipCount; i++) {
                Chip chip = (Chip) chipGroupCategories.getChildAt(i);
                if (chip.getText().toString().equals(productCategory)) {
                    chip.setChecked(true);
                    break;
                }
            }
        } else {
            productNameOriginal = "";
        }

        ProductDatabase productDatabase = Room.databaseBuilder(getApplicationContext(),
                        ProductDatabase.class, "product_database")
                .build();

        productDao = productDatabase.productDao();
    }

    public void confirmProduct(View view) {
        String productName = productNameEditText.getText().toString().trim();
        String price = priceEditText.getText().toString();
        String quantity = quantityEditText.getText().toString();
        String category = getSelectedChipText();

        new Thread(() -> {
            String listName = getIntent().getStringExtra("LIST_NAME");
            if (!Objects.equals(productNameOriginal, productName)) {
                ProductEntity existingProduct = productDao.getProductByNameAndList(productName, listName);
                if (existingProduct != null) {
                    runOnUiThread(() -> Toast.makeText(this, "Il prodotto esiste già nella lista", Toast.LENGTH_SHORT).show());
                    return;
                }
            }

            runOnUiThread(() -> {
                if (!productName.isEmpty() && !quantity.isEmpty() && !category.isEmpty()) {
                    Intent intent = getIntent();
                    boolean isEdit = intent.getBooleanExtra("EDIT_PRODUCT", false);

                    // se si tratta di una modifica, aggiorna il prodotto esistente nel database
                    if (isEdit) {
                        int productId = intent.getIntExtra("PRODUCT_ID", -1);
                        // elimina il prodotto esistente
                        new Thread(() -> {
                            // crea un nuovo prodotto con le modifiche
                            ProductEntity productEntity;
                            if (!price.isEmpty()) {
                                productEntity = new ProductEntity(productId, productName, price, quantity, category, listName);
                            } else {
                                productEntity = new ProductEntity(productId, productName, "non specificato", quantity, category, listName);
                            }

                            productDao.updateProduct(productEntity);
                            Intent intent2 = new Intent(this, ListDetailActivity.class);
                            intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent2.putExtra("LIST_NAME", listName);
                            startActivity(intent2);
                            finish();
                        }).start();
                    } else {
                        // altrimenti, se non è una modifica, aggiungi un nuovo prodotto nel database
                        ProductEntity productEntity;
                        if (!price.isEmpty()) {
                            productEntity = new ProductEntity(productName, price, quantity, category, listName);
                        } else {
                            productEntity = new ProductEntity(productName, "non specificato", quantity, category, listName);
                        }

                        new Thread(() -> {
                            productDao.insertProduct(productEntity);
                            if (intent.getBooleanExtra("ADD_PRODUCT_FROM_DETAIL", true)) {
                                Intent intent2 = new Intent(this, ListDetailActivity.class);
                                intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent2.putExtra("LIST_NAME", listName);
                                startActivity(intent2);
                                finish();
                            } else {
                                navigateToCreationListActivity();
                            }
                        }).start();
                    }
                } else {
                    Toast.makeText(this, "Si prega di compilare tutti i campi obbligatori", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void navigateToCreationListActivity() {
        Intent intent = new Intent(this, CreationListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private String getSelectedChipText() {
        int selectedChipId = chipGroupCategories.getCheckedChipId();
        Chip selectedChip = findViewById(selectedChipId);
        if (selectedChip != null) {
            return selectedChip.getText().toString();
        }
        return "";
    }

    public void goBack(View view) {
        showDialog();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        showDialog();
    }

    public void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Desideri tornare indietro senza salvare il prodotto?");
        builder.setPositiveButton("Sì", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent();
                intent.putExtra("RESULT", "BACK_PRESSED");
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // non fare nulla
            }
        });
        builder.show();
    }
}