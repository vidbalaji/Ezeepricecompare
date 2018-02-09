package com.vidhyalearning.ezeepricecompare;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by user on 03-07-2017.
 */

class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.MyViewHolder>{
    private List<product> productList;
    private ProductListAdapter.CustomFilter mFilter;
    private List<product> dictionaryWords ;
    private List<product> filteredList ;
    OnCustomEventListener mCustomListener;
    public void setCustomEventListener(OnCustomEventListener eventListener) {
        mCustomListener = eventListener;
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView itemTitle, pricetext,mrpText,categoryText,deliveryText;
        public ImageView image;
        public View view1;
        public final Button shopButton,compareButton,reviewButton,featureText;
        public product product1;
        public MyViewHolder(View view) {
            super(view);
            view1 = view;
            itemTitle = (TextView) view.findViewById(R.id.itemTitle);
            pricetext = (TextView) view.findViewById(R.id.pricetext);
            image = (ImageView) view.findViewById(R.id.imageView);
            shopButton = (Button) view.findViewById(R.id.shopButton);
            mrpText = (TextView) view.findViewById(R.id.mrpText);
            categoryText = (TextView) view.findViewById(R.id.categoryText);
            deliveryText = (TextView) view.findViewById(R.id.deliveryText);
            featureText = (Button)view.findViewById(R.id.featureButton);
            reviewButton = (Button)view.findViewById(R.id.reviewButton);
            compareButton = (Button) view.findViewById(R.id.textComparePrice);
        }
    }


    public ProductListAdapter(List<product> productList) {
        this.productList = productList;
        dictionaryWords = new ArrayList<product>();
        filteredList = productList;
        dictionaryWords.addAll(productList);
        mFilter = new ProductListAdapter.CustomFilter(ProductListAdapter.this);
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_product_display, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        product product = productList.get(position);
        final product finalProduct = product;
        if(dictionaryWords.size()<=0){
            dictionaryWords.addAll(productList);
        }
        holder.product1=product;
        if(product.getSeller().equals("Ebay")){
            holder.reviewButton.setVisibility(View.INVISIBLE);
        }
        holder.itemTitle.setText(product.getTitle());
        holder.pricetext.setText(product.getPrice());

        holder.image.setImageBitmap(product.getImage());
        holder.image.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
              Dialog d = new Dialog(v.getContext());
                d.setContentView(R.layout.imagelayout);
                ImageView image2 = (ImageView) d.findViewById(R.id.imageView2);
                image2.setImageBitmap(finalProduct.getLargeImage());
                d.show();
            }


        });
        holder.deliveryText.setText(product.getDeliveryTime());
        holder.categoryText.setText(product.getCategoryName());
        holder.mrpText.setText(product.getMrp());
        holder.featureText.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                showFeatureScreen(holder);
            }


        });
        holder.compareButton.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                compareScreen(holder);
            }


        });
        holder.shopButton.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                displayScreen(holder);
            }


        });

        holder.reviewButton.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                reviewScreen(holder);
            }


        });
    }

    private void showFeatureScreen(MyViewHolder holder) {
        String feature = holder.product1.getFeature();
        AlertDialog.Builder alert = new AlertDialog.Builder(holder.view1.getContext());
        alert.setTitle("Product Features");
        alert.setMessage(feature);
        alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    private void reviewScreen(MyViewHolder holder) {
        String url = holder.product1.getReviewUrl();
        if(holder.product1.getSeller().equals("Amazon")) {
            AlertDialog.Builder alert = new AlertDialog.Builder(holder.view1.getContext());
            alert.setTitle("Product Reviews");

            WebView wv = new WebView(holder.view1.getContext());
            wv.loadUrl(url);
            wv.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);

                    return true;
                }
            });

            alert.setView(wv);
            alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            alert.show();
        }
        else
        {
            Uri uriUrl = Uri.parse(url);
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
            holder.view1.getContext().startActivity(launchBrowser);
        }
    }

    private void compareScreen(MyViewHolder holder) {
        Intent i = new Intent();
        i.setClass(holder.view1.getContext(),Main2Activity.class);

        ArrayList<product> tempProductList = new ArrayList<product>();
        //tempProductList.add(holder.product1);

        i.putExtra("ProductTitle",holder.product1.getTitle());
        i.putExtra("ProductPrice",holder.product1.getPrice());
        i.putExtra("ProductMrp",holder.product1.getMrp());
        i.putExtra("ProductSeller",holder.product1.getSeller());
        i.putExtra("ProductURL",holder.product1.getUrl());
        i.putExtra("ProductWOMrp",holder.product1.getAvailablePrice());
        i.putExtra("ProductFeature",holder.product1.getFeature());
        i.putExtra("ProductReviewUrl",holder.product1.getReviewUrl());
        i.putExtra("ProductCategory",holder.product1.getCategoryName());
        if(holder.image.getDrawable()!=null) {
            Bitmap _bitmap =  holder.product1.getImage(); // your bitmap
            if(_bitmap !=null) {
                ByteArrayOutputStream _bs = new ByteArrayOutputStream();
                _bitmap.compress(Bitmap.CompressFormat.PNG, 50, _bs);
                i.putExtra("byteArray", _bs.toByteArray());
            }
        }
        Toast.makeText( holder.view1.getContext(),"Searching may take a while.Please Wait",Toast.LENGTH_LONG).show();
        holder.view1.getContext().startActivity(i);
    }

    private void displayScreen(MyViewHolder holder) {
        String url = holder.product1.getUrl();
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        holder.view1.getContext().startActivity(launchBrowser);
    }


    @Override
    public int getItemCount() {
        return productList.size();
    }
    public Filter getFilter() {
        return mFilter;
    }
    public void setCategoryList(ArrayList<String> categoryList, String sort){
        mFilter.setCategoryList(categoryList,sort);
    }
    public String getFilterCount(){

        String strCount = "0";
        if(getFilter()!=null)
        String.valueOf(((CustomFilter)getFilter()).getFilterCount());
        return strCount;
    }
    public class CustomFilter extends Filter {
        protected ProductListAdapter mAdapter;
        public int filterCount;
        ArrayList<String> categoryList = new ArrayList<String>();
        private String sort="";

        protected CustomFilter(ProductListAdapter mAdapter) {
            super();
            this.mAdapter = mAdapter;
        }
        public String getFilterCount(){

            String strCount = "0";
                String.valueOf(filterCount);
            return strCount;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            filteredList.clear();
            final FilterResults results = new FilterResults();
            if (constraint.length() == 0) {
                filteredList.addAll(dictionaryWords);
            } else {
                final String filterPattern = constraint.toString().toLowerCase().trim();
                for (final product mWords : dictionaryWords) {
                    if (mWords.getTitle().toLowerCase().contains(filterPattern)
                            ) {
                        filteredList.add(mWords);
                    }
                }
            }
            System.out.println("Count Number " + filteredList.size());




            if(categoryList.size()>0){
                List<product> categoryProductList = new ArrayList<product>() ;
                categoryProductList.addAll(filteredList);
                filteredList.clear();
                for (int i =0;i<categoryProductList.size();i++) {
                    if(categoryList.contains(categoryProductList.get(i).getCategoryName())){
                        filteredList.add(categoryProductList.get(i));
                    }
                }

            }
            if(sort.equals("Descending")){
                Collections.sort(filteredList, product.priceCompareDesc);
            }
            else if(sort.equals("Ascending")){
                Collections.sort(filteredList, product.priceCompare);
            }
            results.values = filteredList;
            results.count = filteredList.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            System.out.println("Count Number 2 " + ((List<product>) results.values).size());
            String strCount = "0";
            strCount= String.valueOf( results.count);
            if(mCustomListener!=null)
            mCustomListener.setFilterCount(strCount);
            this.mAdapter.notifyDataSetChanged();
        }




        public void setCategoryList(ArrayList<String> localcategoryList,String sort) {
            categoryList.clear();
            this.categoryList = localcategoryList;
            if(sort.equals("High To Low")){
                this.sort ="Descending";
            }
            else if(sort.equals("Low To High")){
                this.sort ="Ascending";
            }
        }
    }
    public interface OnCustomEventListener {
        void setFilterCount(String filterCount);
    }
}
