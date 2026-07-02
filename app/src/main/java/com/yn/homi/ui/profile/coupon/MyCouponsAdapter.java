package com.yn.homi.ui.profile.coupon;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.yn.homi.R;
import com.yn.homi.data.model.Coupon;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MyCouponsAdapter extends RecyclerView.Adapter<MyCouponsAdapter.ViewHolder> {
    private final List<Coupon> coupons;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.US);

    public MyCouponsAdapter(List<Coupon> coupons) {
        this.coupons = coupons;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_coupon, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Coupon coupon = coupons.get(position);

        String discountValue;
        if ("percent".equals(coupon.getDiscountType())) {
            discountValue = (int) coupon.getDiscountValue() + "%";
        } else {
            discountValue = "$" + (int) coupon.getDiscountValue();
        }
        holder.tvDiscountValue.setText(discountValue);

        String type = coupon.getType();
        String title = (type != null && !type.isEmpty()) 
                ? type.substring(0, 1).toUpperCase() + type.substring(1) + " Coupon"
                : "Coupon";
        holder.tvCouponTitle.setText(title);
        holder.tvCouponCode.setText("Code: " + coupon.getCode());

        if (coupon.getExpiryDate() != null) {
            holder.tvExpiry.setText("Expires: " + dateFormat.format(coupon.getExpiryDate().toDate()));
        } else {
            holder.tvExpiry.setText("No expiration");
        }

        if (coupon.isUsed()) {
            holder.tvUsedStatus.setVisibility(View.VISIBLE);
            holder.layoutDiscount.setBackgroundColor(Color.parseColor("#9CA3AF")); // Gray
        } else {
            holder.tvUsedStatus.setVisibility(View.GONE);
            holder.layoutDiscount.setBackgroundColor(Color.parseColor("#685047")); // Primary brown
        }
    }

    @Override
    public int getItemCount() {
        return coupons.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDiscountValue, tvDiscountLabel, tvCouponTitle, tvCouponCode, tvExpiry, tvUsedStatus;
        View layoutDiscount;

        ViewHolder(View v) {
            super(v);
            tvDiscountValue = v.findViewById(R.id.tvDiscountValue);
            tvDiscountLabel = v.findViewById(R.id.tvDiscountLabel);
            tvCouponTitle = v.findViewById(R.id.tvCouponTitle);
            tvCouponCode = v.findViewById(R.id.tvCouponCode);
            tvExpiry = v.findViewById(R.id.tvExpiry);
            tvUsedStatus = v.findViewById(R.id.tvUsedStatus);
            layoutDiscount = v.findViewById(R.id.layoutDiscount);
        }
    }
}
