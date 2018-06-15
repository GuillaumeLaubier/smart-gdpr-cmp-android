package com.smartadserver.android.smartcmp.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.smartadserver.android.smartcmp.R;
import com.smartadserver.android.smartcmp.consentstring.ConsentString;
import com.smartadserver.android.smartcmp.manager.ConsentManager;
import com.smartadserver.android.smartcmp.model.Vendor;
import com.smartadserver.android.smartcmp.model.VendorList;

/**
 * Vendor list activity.
 */

public class VendorListActivity extends AppCompatActivity {

    ConsentString consentString;
    VendorList vendorList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vendor_list_activity_layout);

        // Setup the actionBar if any
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(ConsentManager.getSharedInstance().getConsentToolConfiguration().getConsentManagementVendorsListTitle());
            // Display the back button
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        consentString = getIntent().getParcelableExtra("consent_string");
        vendorList = getIntent().getParcelableExtra("vendor_list");

        // Setup the recycler view
        RecyclerView recyclerView = findViewById(R.id.vendor_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ListLayoutAdapter());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent result = new Intent();
        result.putExtra("consent_string", consentString);
        setResult(RESULT_OK, result);
        finish();
    }

    private class VendorViewHolder extends RecyclerView.ViewHolder {

        private TextView vendorNameTextView;
        private Switch vendorStatusSwitch;

        private Vendor vendor;

        VendorViewHolder(View itemView) {
            super(itemView);

            vendorNameTextView = itemView.findViewById(R.id.vendor_name_textview);
            vendorStatusSwitch = itemView.findViewById(R.id.vendor_status_switch);

            // Make the clickable zone bigger to ease the click
            RelativeLayout clickableZone = itemView.findViewById(R.id.clickable_zone_switch);
            clickableZone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    vendorStatusSwitch.setChecked(!vendorStatusSwitch.isChecked());
                }
            });

            vendorStatusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    // Create a new consentString by adding or removing the vendor
                    consentString = checked ? ConsentString.consentStringByAddingVendorConsent(vendor.getId(), consentString) : ConsentString.consentStringByRemovingVendorConsent(vendor.getId(), consentString);
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Start the VendorActivity
                    Intent intent = new Intent(getApplicationContext(), VendorActivity.class);
                    intent.putExtra("vendor", vendor);
                    intent.putExtra("vendor_list", vendorList);
                    startActivity(intent);
                }
            });
        }

        public void setVendor(@NonNull Vendor vendor) {
            this.vendor = vendor;

            vendorNameTextView.setText(vendor.getName());
            vendorStatusSwitch.setChecked(consentString.isVendorAllowed(vendor.getId()));
        }
    }

    /**
     * Custom layout adapter for the recycler view.
     */
    private class ListLayoutAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        @NonNull
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.vendor_cell, parent, false);
            return new VendorViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
            Vendor vendor = vendorList.getActivatedVendor().get(position);
            VendorViewHolder holder = (VendorViewHolder) viewHolder;
            holder.setVendor(vendor);
        }

        @Override
        public int getItemCount() {
            return vendorList.getActivatedVendor().size();
        }
    }
}
