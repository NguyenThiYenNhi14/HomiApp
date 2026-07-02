package com.yn.homi.ui.checkout;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yn.homi.R;
import com.yn.homi.data.model.Coupon;
import java.util.ArrayList;
import java.util.List;
public class VoucherBottomSheet extends BottomSheetDialogFragment {
public interface OnVoucherSelectedListener {
    void onSelected(Coupon coupon);
}

private OnVoucherSelectedListener listener;
public void setOnVoucherSelectedListener(OnVoucherSelectedListener l) { this.listener = l; }

@Nullable
@Override
public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.bottomsheet_voucher, container, false);
}

@Override
public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    RecyclerView rv = view.findViewById(R.id.rvVouchers);
    TextView tvEmpty = view.findViewById(R.id.tvVoucherEmpty);
    rv.setLayoutManager(new LinearLayoutManager(requireContext()));

    String uid = FirebaseAuth.getInstance().getUid();
    if (uid == null) return;

    FirebaseFirestore.getInstance()
            .collection("users").document(uid).collection("coupons")
            .whereEqualTo("isUsed", false)
            .get()
            .addOnSuccessListener(snapshot -> {
                List<Coupon> validCoupons = new ArrayList<>();
                com.google.firebase.Timestamp now = com.google.firebase.Timestamp.now();
                for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                    Coupon c = doc.toObject(Coupon.class);
                    if (c != null) {
                        c.setId(doc.getId());
                        if (c.getExpiryDate() == null || c.getExpiryDate().compareTo(now) > 0) {
                            validCoupons.add(c);
                        }
                    }
                }
                if (validCoupons.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    rv.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    rv.setVisibility(View.VISIBLE);
                    rv.setAdapter(new VoucherAdapter(validCoupons, coupon -> {
                        if (listener != null) listener.onSelected(coupon);
                        dismiss();
                    }));
                }
            });
}

static class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VH> {
    interface OnPick { void onPick(Coupon c); }
    private final List<Coupon> data;
    private final OnPick onPick;
    VoucherAdapter(List<Coupon> data, OnPick onPick) { this.data = data; this.onPick = onPick; }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Coupon c = data.get(position);
        String discountStr = "percent".equals(c.getDiscountType())
                ? "-" + (int) c.getDiscountValue() + "%"
                : "-$" + c.getDiscountValue();
        holder.line1.setText(c.getCode() + "  (" + discountStr + ")");
        holder.line2.setText(c.getType());
        holder.itemView.setOnClickListener(v -> onPick.onPick(c));
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView line1, line2;
        VH(View v) {
            super(v);
            line1 = v.findViewById(android.R.id.text1);
            line2 = v.findViewById(android.R.id.text2);
        }
    }
}
}
