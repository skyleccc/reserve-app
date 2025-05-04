package com.reserve.app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.graphics.Rect;
import android.view.View;

public class HomepageActivity extends AppCompatActivity {

    RecyclerView bottomMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        bottomMenu = findViewById(R.id.bottom_menu);
        bottomMenu.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<BottomMenuItem> menuItems = Arrays.asList(
                new BottomMenuItem(R.drawable.ic_explore, "Explore"),
                new BottomMenuItem(R.drawable.ic_saved, "Saved"),
                new BottomMenuItem(R.drawable.ic_updates, "Updates"),
                new BottomMenuItem(R.drawable.ic_add, "Add")
        );

        RecyclerView recyclerView = findViewById(R.id.parking_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<ParkingSpot> spots = new ArrayList<>();
        spots.add(new ParkingSpot(
                "Near Lechon House",
                "Azalla's Lechon House, Saint Jude Street, Hippodromo, Cebu City, 6000 Cebu",
                R.drawable.ic_map_placeholder,
                "₱30.00", "₱15.00", "₱345.00"
        ));

        spots.add(new ParkingSpot(
                "SM Seaside Entrance",
                "SRP-Mambaling Rd, Cebu City, 6000 Cebu",
                R.drawable.ic_map_placeholder,
                "₱40.00", "₱20.00", "₱400.00"
        ));

        spots.add(new ParkingSpot(
                "IT Park Basement",
                "Geonzon St, Apas, Cebu City, 6000 Cebu",
                R.drawable.ic_map_placeholder,
                "₱35.00", "₱18.00", "₱380.00"
        ));

        spots.add(new ParkingSpot(
                "Near Ayala Center",
                "Cebu Business Park, Archbishop Reyes Ave, Cebu City",
                R.drawable.ic_map_placeholder,
                "₱45.00", "₱25.00", "₱450.00"
        ));

        spots.add(new ParkingSpot(
                "Robinsons Galleria",
                "General Maxilom Ave Ext, Cebu City, Cebu",
                R.drawable.ic_map_placeholder,
                "₱32.00", "₱17.00", "₱365.00"
        ));

        spots.add(new ParkingSpot(
                "Cebu South Bus Terminal",
                "N. Bacalso Ave, Cebu City, Cebu",
                R.drawable.ic_map_placeholder,
                "₱28.00", "₱14.00", "₱310.00"
        ));

        spots.add(new ParkingSpot(
                "Mango Square Lot",
                "Gen. Maxilom Ave, Cebu City, Cebu",
                R.drawable.ic_map_placeholder,
                "₱25.00", "₱12.00", "₱290.00"
        ));

        spots.add(new ParkingSpot(
                "Parkmall Area",
                "Ouano Ave, Mandaue City, Cebu",
                R.drawable.ic_map_placeholder,
                "₱30.00", "₱16.00", "₱335.00"
        ));

        ParkingSpotAdapter adapter = new ParkingSpotAdapter(this, spots);
        recyclerView.setAdapter(adapter);


        BottomMenuAdapter adapter2 = new BottomMenuAdapter(this, menuItems);
        bottomMenu.setAdapter(adapter2);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.menu_item_spacing);
        bottomMenu.addItemDecoration(new HorizontalSpacingItemDecoration(spacingInPixels));

    }

    public class HorizontalSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private final int spacing;

        public HorizontalSpacingItemDecoration(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int itemCount = state.getItemCount();

            // Even spacing between all items
            if (position == 0) {
                outRect.left = spacing;
                outRect.right = spacing / 2;
            } else if (position == itemCount - 1) {
                outRect.left = spacing / 2;
                outRect.right = spacing;
            } else {
                outRect.left = spacing / 2;
                outRect.right = spacing / 2;
            }
        }
    }

}
