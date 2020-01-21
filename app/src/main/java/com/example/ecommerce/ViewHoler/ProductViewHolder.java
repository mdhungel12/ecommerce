package com.example.ecommerce.ViewHoler;

import android.media.Image;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerce.Interface.ItemClickListner;
import com.example.ecommerce.R;

public class ProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txtProductName,txtProductDescription,txtProductPrice;

    public ImageView imageView;

    public ItemClickListner listner;

    public ProductViewHolder( View itemView)
    {
        super(itemView);
        txtProductName = (TextView) itemView.findViewById(R.id.product_namee);
        imageView = (ImageView) itemView.findViewById(R.id.product_image1);
        txtProductPrice = (TextView) itemView.findViewById(R.id.product_price);
        txtProductDescription = (TextView) itemView.findViewById(R.id.product_description1);



    }

    public void setItemClickListner(ItemClickListner listner)
    {
        this.listner = listner;

    }

    @Override
    public void onClick(View view)
    {
        listner.onClick(view, getAdapterPosition(),false);
    }
}
