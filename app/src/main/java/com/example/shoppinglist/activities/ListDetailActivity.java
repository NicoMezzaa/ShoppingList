package com.example.shoppinglist.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;
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

import java.util.List;

public class ListDetailActivity extends AppCompatActivity {

    private TextView listNameTextView;
    private RecyclerView productsRecyclerView;
    private ProductDao productDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_list);

        listNameTextView = findViewById(R.id.listNameTextView);
        productsRecyclerView = findViewById(R.id.productsRecyclerView);
        productsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // recupera il nome della lista passato dall'intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("LIST_NAME")) {
            String listName = intent.getStringExtra("LIST_NAME");

            // aggiorna l'UI con il nome della lista
            listNameTextView.setText(listName);

            // recupera i prodotti associati al nome della lista
            ProductDatabase db = Room.databaseBuilder(getApplicationContext(),
                    ProductDatabase.class, "product_database").build();
            productDao = db.productDao();

            new Thread(() -> {
                List<ProductEntity> products = productDao.getProductsByListName(listName);

                // aggiorna l'UI con l'elenco dei prodotti
                runOnUiThread(() -> {
                    ProductAdapter adapter = new ProductAdapter(products, true);
                    productsRecyclerView.setAdapter(adapter);
                });
            }).start();
        }
    }

    public void addProduct(View view) {
        String listName = listNameTextView.getText().toString().trim();

        new Thread(() -> {
            // inizializza il ProductDao
            ProductDatabase productDatabase = Room.databaseBuilder(getApplicationContext(),
                            ProductDatabase.class, "product_database")
                    .build();

            ShoppingListDao shoppingListDao = productDatabase.shoppingListDao();
            ShoppingListEntity existingList = shoppingListDao.getShoppingListByName(listName);

            runOnUiThread(() -> {
                Intent intent = new Intent(this, NewProductActivity.class);
                intent.putExtra("LIST_NAME", existingList.getListName());
                intent.putExtra("ADD_PRODUCT_FROM_DETAIL", true);
                startActivity(intent);
            });
        }).start();
    }

    public void deleteProduct(View view) {
        new Thread(() -> {
            ProductDatabase productDatabase = Room.databaseBuilder(getApplicationContext(),
                            ProductDatabase.class, "product_database")
                    .build();
            ProductDao productDao = productDatabase.productDao();

            View parentView = (View) view.getParent();
            RecyclerView recyclerView = (RecyclerView) parentView.getParent();
            int position = recyclerView.getChildAdapterPosition(parentView);

            ProductAdapter adapter = (ProductAdapter) recyclerView.getAdapter();
            assert adapter != null;

            List<ProductEntity> products = adapter.getProductList();
            String listName = listNameTextView.getText().toString().trim();

            List<ProductEntity> productsList = productDao.getProductsByListName(listName);

            runOnUiThread(() -> {
                if (productsList.size() <= 1 && products.size() <= 1) {
                    Toast.makeText(this, "Devi mantenere almeno un prodotto nella lista", Toast.LENGTH_SHORT).show();
                    return;
                }

                showDeleteProductConfirmationDialog(position, products, adapter);
            });
        }).start();
    }

    private void showDeleteProductConfirmationDialog(int position, List<ProductEntity> products, ProductAdapter adapter) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Attenzione");
        builder.setMessage("Sei sicuro di voler eliminare questo prodotto?");

        builder.setPositiveButton("Sì", (dialogInterface, i) -> {
            // esegui l'eliminazione del prodotto
            deleteSelectedProduct(position, products, adapter);
        });

        builder.setNegativeButton("No", (dialogInterface, i) -> {
            // l'utente ha scelto di non eliminare il prodotto, quindi chiudi il dialog
            dialogInterface.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteSelectedProduct(int position, List<ProductEntity> products, ProductAdapter adapter) {
        // rimuovi il prodotto dalla lista in base alla posizione
        ProductEntity productToDelete = products.get(position);

        // esegui l'eliminazione del prodotto in un thread separato
        new Thread(() -> {
            productDao.deleteProduct(productToDelete);
            runOnUiThread(() -> {
                products.remove(position);  // rimuovi il prodotto dalla lista visualizzata
                adapter.notifyItemRemoved(position);    // notifica all'adapter dell'eliminazione
            });
        }).start();

        Toast.makeText(ListDetailActivity.this, "Prodotto eliminato correttamente", Toast.LENGTH_SHORT).show();
    }

    public void editProduct(View view) {
        View parentView = (View) view.getParent();
        RecyclerView recyclerView = (RecyclerView) parentView.getParent();
        int position = recyclerView.getChildAdapterPosition(parentView);

        ProductAdapter adapter = (ProductAdapter) recyclerView.getAdapter();
        assert adapter != null;
        List<ProductEntity> products = adapter.getProductList();

        if (position != RecyclerView.NO_POSITION) {
            ProductEntity productToEdit = products.get(position);

            Intent intent = new Intent(this, NewProductActivity.class);
            intent.putExtra("EDIT_PRODUCT", true);
            intent.putExtra("LIST_NAME", listNameTextView.getText());
            intent.putExtra("PRODUCT_ID", productToEdit.getId());
            intent.putExtra("PRODUCT_NAME", productToEdit.getName());
            intent.putExtra("PRODUCT_PRICE", productToEdit.getPrice());
            intent.putExtra("PRODUCT_QUANTITY", productToEdit.getQuantity());
            intent.putExtra("PRODUCT_CATEGORY", productToEdit.getCategory());
            startActivity(intent);
        }
    }

    public void goBack(View view){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    public void filter(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.filter_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.option_all) {
                eseguiFiltro("Mostra tutti");
            } else if (itemId == R.id.option_fruits_vegetables) {
                eseguiFiltro("Frutta e verdura");
                return true;
            } else if (itemId == R.id.option_delicatessen_dairy) {
                eseguiFiltro("Salumi e latticini");
                return true;
            } else if (itemId == R.id.option_meat_fish) {
                eseguiFiltro("Carne e pesce");
                return true;
            } else if (itemId == R.id.option_pasta_rice_legumes) {
                eseguiFiltro("Pasta, riso e legumi");
                return true;
            } else if (itemId == R.id.option_bread_sweets) {
                eseguiFiltro("Pane e dolci");
                return true;
            } else if (itemId == R.id.option_drinks) {
                eseguiFiltro("Bere");
                return true;
            } else if (itemId == R.id.option_other) {
                eseguiFiltro("Altro");
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private void eseguiFiltro(String categoria) {
        String listName = listNameTextView.getText().toString().trim();

        new Thread(() -> {
            List<ProductEntity> filteredProducts;

            if (categoria.equals("Mostra tutti")) {
                filteredProducts = productDao.getProductsByListName(listName);
            } else {
                filteredProducts = productDao.getProductsByListNameAndCategory(listName, categoria);
            }

            runOnUiThread(() -> {
                if (filteredProducts.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Nessun prodotto");
                    builder.setMessage("Nessun prodotto per questa categoria");
                    builder.setPositiveButton("OK", (dialog, which) -> {
                        dialog.dismiss();   // chiudi il dialog
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }

                ProductAdapter adapter = new ProductAdapter(filteredProducts, true);
                productsRecyclerView.setAdapter(adapter);
            });
        }).start();
    }
}