package com.example.fetchrewardsapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        fetchItems();
    }

    private void fetchItems() {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<List<Item>> call = apiService.getItems();

        call.enqueue(new Callback<List<Item>>() {
            @Override
            public void onResponse(Call<List<Item>> call, Response<List<Item>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Item> itemList = response.body();


                    List<Item> filteredItems = new ArrayList<>();
                    for (Item item : itemList) {
                        if (item.getName() != null && !item.getName().trim().isEmpty()) {
                            filteredItems.add(item);
                        }
                    }


                    Collections.sort(filteredItems, new Comparator<Item>() {
                        @Override
                        public int compare(Item item1, Item item2) {
                            // First, sort by listId
                            int listIdComparison = Integer.compare(item1.getListId(), item2.getListId());
                            if (listIdComparison == 0) {
                                int num1 = extractItemNumber(item1.getName());
                                int num2 = extractItemNumber(item2.getName());
                                return Integer.compare(num1, num2);
                            } else {
                                return listIdComparison;
                            }
                        }


                        private int extractItemNumber(String itemName) {
                            try {
                                return Integer.parseInt(itemName.replaceAll("\\D+", ""));
                            } catch (NumberFormatException e) {
                                return Integer.MAX_VALUE; // Handle items without a valid number
                            }
                        }
                    });


                    itemAdapter = new ItemAdapter(filteredItems);
                    recyclerView.setAdapter(itemAdapter);
                } else {
                    Toast.makeText(MainActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                    Log.e("API_ERROR", "Response failed");
                }
            }

            @Override
            public void onFailure(Call<List<Item>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "API Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API_ERROR", t.getMessage());
            }
        });
    }
}