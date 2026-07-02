package com.yn.homi.ui.profile.order;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.yn.homi.R;


public class MyOrdersActivity extends AppCompatActivity {

    private int[] TAB_TITLES_RES = {
            R.string.tab_all_orders,
            R.string.tab_pending,
            R.string.tab_processing,
            R.string.tab_partially_shipped,
            R.string.tab_shipped,
            R.string.tab_completed,
            R.string.tab_return
    };
    private static final String[] TAB_STATUS = {"ALL", "PENDING", "PROCESSING", "PARTIALLY_SHIPPED", "SHIPPED", "COMPLETED", "RETURNED"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // ViewPager + Tabs
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        viewPager.setAdapter(new OrderPagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) ->
                tab.setText(TAB_TITLES_RES[position])
        ).attach();

        // Handle target tab from Intent
        int targetTab = getIntent().getIntExtra("TARGET_TAB", 0);
        if (targetTab >= 0 && targetTab < TAB_TITLES_RES.length) {
            viewPager.setCurrentItem(targetTab, false);
        }
    }

    // Adapter cho ViewPager2
    private class OrderPagerAdapter extends FragmentStateAdapter {
        OrderPagerAdapter(FragmentActivity fa) { super(fa); }

        @Override
        public Fragment createFragment(int position) {
            return OrderListFragment.newInstance(TAB_STATUS[position]);
        }

        @Override
        public int getItemCount() { return TAB_TITLES_RES.length; }
    }
}
