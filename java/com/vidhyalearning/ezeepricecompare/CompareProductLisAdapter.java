package com.vidhyalearning.ezeepricecompare;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by user on 04-Aug-17.
 */

public class CompareProductLisAdapter extends RecyclerView.Adapter<CompareProductLisAdapter.MyViewHolder>{
    private List<product> productList;
    private CustomFilter mFilter;
    private List<product> dictionaryWords ;
    private List<product> filteredList ;
    OnCompareCustomEventListener mCustomListener;
    public void setCustomEventListener(OnCompareCustomEventListener eventListener) {
        mCustomListener = eventListener;
    }
        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView itemTitle, pricetext,mrpText,categoryText,deliveryText;
            Button featureText,reviewButton;
            public ImageView productImage,sellerImage;
            public View view1;
            public final Button shopButton;
            public product product1;
            public MyViewHolder(View view) {
                super(view);
                view1 = view;
                itemTitle = (TextView) view.findViewById(R.id.titleText);
                pricetext = (TextView) view.findViewById(R.id.textPrice);
                productImage = (ImageView) view.findViewById(R.id.productImage);
                sellerImage = (ImageView) view.findViewById(R.id.sellerImage);
                shopButton = (Button) view.findViewById(R.id.shopButton);
                mrpText = (TextView) view.findViewById(R.id.mrpText);
                categoryText = (TextView) view.findViewById(R.id.categoryText);
                featureText = (Button) view.findViewById(R.id.featureText);
                reviewButton = (Button)view.findViewById(R.id.reviewButton);
            }
        }


    public CompareProductLisAdapter(List<product> productList) {
            this.productList = productList;
            dictionaryWords = new ArrayList<product>();
            filteredList = productList;
            dictionaryWords.addAll(productList);
            mFilter = new CustomFilter(CompareProductLisAdapter.this);
        }

        @Override
        public CompareProductLisAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.activity_compare_product, parent, false);

            return new CompareProductLisAdapter.MyViewHolder(itemView);
        }



    @Override
        public void onBindViewHolder(final CompareProductLisAdapter.MyViewHolder holder, int position) {
            product product = productList.get(position);
        final product finalproduct=product;
            holder.product1=product;
            holder.itemTitle.setText(product.getTitle());
        if(product.getPrice().isEmpty()==false) {
            holder.pricetext.setText(product.getPrice());
        }
        else if(product.getMrp().isEmpty() == false) {
            holder.pricetext.setText(product.getMrp());
        }
        else
            holder.pricetext.setText("NA");

            holder.productImage.setImageBitmap(product.getImage());
        holder.productImage.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                Dialog d = new Dialog(v.getContext());
                d.setContentView(R.layout.imagelayout);
                ImageView image2 = (ImageView) d.findViewById(R.id.imageView2);
                image2.setImageBitmap(finalproduct.getLargeImage());
                d.show();
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
        holder.featureText.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) { String feature = holder.product1.getFeature();

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
                }}
            );
        holder.categoryText.setText(product.getCategoryName());
        if(product.getSeller().equals("Amazon")) {
            holder.sellerImage.setImageResource(R.mipmap.amazon);
        }
        else if(product.getSeller().equals("Ebay")) {
            holder.sellerImage.setImageResource(R.mipmap.ebay);
            holder.reviewButton.setVisibility(View.INVISIBLE);

        }
        if(product.getSeller().equals("Flipkart")) {
            holder.sellerImage.setImageResource(R.mipmap.flipkart);
        }
            holder.shopButton.setOnClickListener(new View.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onClick(View v) {
                    displayScreen(holder);
                }


            });

        }



    private void displayScreen(CompareProductLisAdapter.MyViewHolder holder) {
        String url = holder.product1.getUrl();
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        holder.view1.getContext().startActivity(launchBrowser);
    }

    private void reviewScreen(CompareProductLisAdapter.MyViewHolder holder) {
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

    public class CustomFilter extends Filter {
        protected CompareProductLisAdapter mAdapter;
        public int filterCount;
        ArrayList<String> categoryList = new ArrayList<String>();
        private String sort="";

        protected CustomFilter(CompareProductLisAdapter mAdapter) {
            super();
            this.mAdapter = mAdapter;
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
            if(sort.equals("Descending")){
                Collections.sort(filteredList, product.priceCompareDesc);
            }
            else if(sort.equals("Ascending")){
                Collections.sort(filteredList, product.priceCompare);
            }
            results.values = filteredList;
            results.count = filteredList.size();


            if(categoryList.size()>0){
                List<product> categoryProductList = new ArrayList<product>() ;
                categoryProductList.addAll(filteredList);
                filteredList.clear();
                for (int i =0;i<categoryProductList.size();i++) {
                    if(categoryList.contains(categoryProductList.get(i).getCategoryName())){
                        filteredList.add(categoryProductList.get(i));
                    }
                }
                results.values = filteredList;
                results.count = filteredList.size();

            }
            mCustomListener.setFilterCount(String.valueOf(results.count));

            return results;
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //System.out.println("Count Number 2 " + ((List<product>) results.values).size());
            this.mAdapter.notifyDataSetChanged();
        }
        public void setCategoryList(ArrayList<String> localcategoryList, String sort) {
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
    public interface OnCompareCustomEventListener {
        void setFilterCount(String filterCount);
    }

}


