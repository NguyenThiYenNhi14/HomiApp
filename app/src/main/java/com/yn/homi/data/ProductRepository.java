package com.yn.homi.data;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yn.homi.models.Product;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {
    private static List<Product> cachedProducts = null;

    public static List<Product> getProducts(Context context) {
        if (cachedProducts != null) return cachedProducts;
        try {
            InputStream is = context.getAssets().open("products.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
            cachedProducts = new Gson().fromJson(json, new TypeToken<List<Product>>(){}.getType());
        } catch (Exception e) {
            return new ArrayList<>();
        }
        return cachedProducts;
    }

    public static Product getProductById(Context context, String id) {
        for (Product p : getProducts(context)) {
            if (p.getId().equals(id)) return p;
        }
        return null;
    }
}