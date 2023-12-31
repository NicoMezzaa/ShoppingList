package com.example.shoppinglist.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.shoppinglist.listener.OnListClickListener;
import com.example.shoppinglist.database.ProductDao;
import com.example.shoppinglist.database.ProductDatabase;
import com.example.shoppinglist.R;
import com.example.shoppinglist.database.ShoppingListDao;
import com.example.shoppinglist.entities.ShoppingListEntity;
import com.example.shoppinglist.adapter.ShoppingListAdapter;

import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnListClickListener {

    private RecyclerView recyclerViewLists;
    private ShoppingListDao shoppingListDao;
    private ProductDao productDao;
    private static final String SHARED_PREF_NAME = "MySharedPref";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerViewLists = findViewById(R.id.recycler_view_lists);
        ProductDatabase db = Room.databaseBuilder(getApplicationContext(),
                ProductDatabase.class, "product_database").build();

        recyclerViewLists.setLayoutManager(new LinearLayoutManager(this));

        new Thread(() -> {
            shoppingListDao = db.shoppingListDao();
            productDao = db.productDao();
            List<ShoppingListEntity> savedShoppingLists = shoppingListDao.getAllShoppingLists();
            runOnUiThread(() -> updateUI(savedShoppingLists));
        }).start();

        //checkAndDeleteOrphanedLists();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAndDeleteOrphanedLists();
    }

    public void deleteList(View view) {
        View parent = (View) view.getParent();
        TextView textViewListName = parent.findViewById(R.id.text_view_list_name);
        String listNameToDelete = textViewListName.getText().toString();

        if (!listNameToDelete.isEmpty()) {
            showDeleteConfirmationDialog(listNameToDelete);
        }
    }

    private void showDeleteConfirmationDialog(String listName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Attenzione");
        builder.setMessage("Sei sicuro di voler cancellare questa lista?");

        builder.setPositiveButton("Sì", (dialogInterface, i) -> {
            deleteSelectedList(listName);
        });

        builder.setNegativeButton("No", (dialogInterface, i) -> {
            dialogInterface.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteSelectedList(String listName) {
        ProductDatabase db = Room.databaseBuilder(getApplicationContext(),
                ProductDatabase.class, "product_database").build();

        new Thread(() -> {
            shoppingListDao = db.shoppingListDao();
            productDao = db.productDao();
            ShoppingListEntity listToDelete = shoppingListDao.getShoppingListByName(listName);

            if (listToDelete != null) {
                db.runInTransaction(() -> {
                    shoppingListDao.deleteShoppingList(listToDelete);
                    productDao.deleteProductsByListName(listToDelete.getListName());
                });

                runOnUiThread(this::getUpdatedShoppingLists);
            } else {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Lista non trovata", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void getUpdatedShoppingLists() {
        ProductDatabase db = Room.databaseBuilder(getApplicationContext(),
                ProductDatabase.class, "product_database").build();
        new Thread(() -> {
            List<ShoppingListEntity> updatedLists = db.shoppingListDao().getAllShoppingLists();
            runOnUiThread(() -> updateUI(updatedLists));
        }).start();
        Toast.makeText(MainActivity.this, "Lista eliminata correttamente", Toast.LENGTH_SHORT).show();

    }

    private void checkAndDeleteOrphanedLists() {
        ProductDatabase db = Room.databaseBuilder(getApplicationContext(),
                ProductDatabase.class, "product_database").build();

        new Thread(() -> {
            List<ShoppingListEntity> orphanedLists = db.shoppingListDao().getOrphanedLists();

            if (orphanedLists != null && !orphanedLists.isEmpty()) {
                // eliminazione delle liste che non hanno prodotti
                for (ShoppingListEntity list : orphanedLists) {
                    db.shoppingListDao().deleteShoppingList(list);
                }

                runOnUiThread(() -> {
                });
            } else {
                runOnUiThread(() -> {
                });
            }
        }).start();
    }

    private void updateUI(List<ShoppingListEntity> shoppingLists) {
        runOnUiThread(() -> {
            if (shoppingLists != null && !shoppingLists.isEmpty()) {
                ShoppingListAdapter adapter = new ShoppingListAdapter(shoppingLists, this);
                recyclerViewLists.setAdapter(adapter);
            } else {
                recyclerViewLists.setAdapter(null);
            }
        });
    }

    public void startCreationListActivity(View view){
        // crea un dialogo per inserire il nome della nuova lista
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Inserisci il nome della nuova lista");

        // aggiungi un campo di testo per l'inserimento del nome della lista nel dialogo
        final EditText input = new EditText(this);
        builder.setView(input);

        // aggiungi i pulsanti Salva e Annulla nel dialogo
        builder.setPositiveButton("Salva", (dialog, which) -> {
            String newListName = input.getText().toString().trim();
            if (!newListName.isEmpty()) {
                checkIfListExistsOrInsert(newListName);
            } else {
                Toast.makeText(MainActivity.this, "Inserisci un nome valido per la lista", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Annulla", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void checkIfListExistsOrInsert(String newListName) {
        ProductDatabase db = Room.databaseBuilder(getApplicationContext(),
                ProductDatabase.class, "product_database").build();

        new Thread(() -> {
            ShoppingListEntity existingList = db.shoppingListDao().getShoppingListByName(newListName);

            runOnUiThread(() -> {
                if (existingList != null) {
                    Toast.makeText(MainActivity.this, "Il nome della lista è già utilizzato", Toast.LENGTH_SHORT).show();
                } else {
                    // salva la nuova lista
                    saveNewList(newListName);
                }
            });
        }).start();
    }

    private void saveNewList(String listName) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("LIST_NAME", listName);
        editor.apply();
        Intent intent = new Intent(this, CreationListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onListClick(String listName) {
        Intent intent = new Intent(this, ListDetailActivity.class);
        intent.putExtra("LIST_NAME", listName);     // passa il nome della lista nell'intent
        startActivity(intent);
    }

    public void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_toolbar, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // azioni da eseguire quando un elemento del menu viene selezionato
                int id = item.getItemId();
                if (id == R.id.action_credits) {
                    Intent intent = new Intent(MainActivity.this, CreditsActivity.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.action_exit) {
                    // esci dall'applicazione
                    exitApp();
                    return true;
                } else if (id == R.id.action_sort) {
                    // codice per ordinare la lista delle spese
                    sortShoppingListsAlphabetically();
                    return true;
                }
                return false;
            }
        });

        popupMenu.show();
    }

    private void exitApp() {
        finishAffinity();

    }

    @SuppressLint("NotifyDataSetChanged")
    private void sortShoppingListsAlphabetically() {
        ShoppingListAdapter adapter = (ShoppingListAdapter) recyclerViewLists.getAdapter();
        if (adapter != null) {
            List<ShoppingListEntity> shoppingLists = adapter.getShoppingLists();
            if (shoppingLists != null && !shoppingLists.isEmpty()) {
                shoppingLists.sort(new Comparator<ShoppingListEntity>() {
                    @Override
                    public int compare(ShoppingListEntity list1, ShoppingListEntity list2) {
                        return list1.getListName().compareToIgnoreCase(list2.getListName());
                    }
                });
                adapter.setShoppingLists(shoppingLists);
                adapter.notifyDataSetChanged();
                updateUI(shoppingLists);
                Toast.makeText(this, "Liste ordinate", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "La lista delle spese è vuota", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "La lista delle spese è vuota", Toast.LENGTH_SHORT).show();
        }
    }
}