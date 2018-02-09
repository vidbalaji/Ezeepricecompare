package com.vidhyalearning.ezeepricecompare;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

public class Main2Activity extends AppCompatActivity {
    product compareproduct = null;
    private RecyclerView recyclerView;
    private CompareProductLisAdapter mAdapter;
    EditText searchText;
    ArrayList<String> categoryList = new ArrayList<String>();
    TextView filterView,searchTextView,countText;
    ArrayList<product> productList = new ArrayList<product>();
    //ArrayList<productList> productList;
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main2);
        String seller="",productTitle="";
        filterView = (TextView)findViewById(R.id.filterText);
        filterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryList.clear();
                Intent intent = new Intent();
                if(productList.isEmpty()==false){
                    for(int i =0 ;i<productList.size();i++){
                        if(categoryList.contains(productList.get(i).categoryName)==false)
                            categoryList.add(productList.get(i).categoryName);
                    }
                }
                intent.putStringArrayListExtra("CATEGORY_LIST_ALL",categoryList);
                intent.setClass(getApplicationContext(),CustomDialog.class);
                filterView.setText("Apply Filter");
                startActivityForResult(intent,103);
            }
        });
        searchTextView=(TextView)findViewById(R.id.searchTextView);
        countText=(TextView)findViewById(R.id.countText);
        compareproduct = new product();
        if(getIntent().hasExtra("byteArray")) {

            Bitmap _bitmap = BitmapFactory.decodeByteArray(
                    getIntent().getByteArrayExtra("byteArray"),0,getIntent().getByteArrayExtra("byteArray").length);
            if(_bitmap!=null) {
                compareproduct.setImage(_bitmap);
                compareproduct.setLargeImage(_bitmap);
            }
        }
         if(getIntent().hasExtra("ProductTitle")) {
             productTitle=getIntent().getStringExtra("ProductTitle");
            compareproduct.setTitle( productTitle);
        }
        else if(getIntent().hasExtra("SearchProductTitle")) {
             productTitle=getIntent().getStringExtra("SearchProductTitle");
        }
         if(getIntent().hasExtra("ProductPrice")) {
            compareproduct.setPrice( getIntent().getStringExtra("ProductPrice"));
        }
        if( getIntent().hasExtra("ProductSeller")) {
            seller = getIntent().getStringExtra("ProductSeller");
            compareproduct.setSeller( seller);

        }
         if( getIntent().hasExtra("ProductURL")) {
            compareproduct.setUrl(getIntent().getStringExtra("ProductURL"));
        }
        if( getIntent().hasExtra("ProductMrp")){
            compareproduct.setMrp(getIntent().getStringExtra("ProductMrp"));
        }
        if( getIntent().hasExtra("ProductCategory")){
            compareproduct.setCategoryName(getIntent().getStringExtra("ProductCategory"));

        }
        if(getIntent().hasExtra("ProductFeature")){
            compareproduct.setFeature(getIntent().getStringExtra("ProductFeature"));
        }
        if(getIntent().hasExtra("ProductReviewUrl")){
            compareproduct.setReviewUrl(getIntent().getStringExtra("ProductReviewUrl"));
        }

        if( getIntent().hasExtra("ProductWOMrp")){
            float floatMrp = getIntent().getFloatExtra("ProductWOMrp",0);

            compareproduct.setWithoutUnitPrice(floatMrp);
            compareproduct.setWithoutUnitMrpPrice(floatMrp);
        }
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView4);
        //recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        searchText = (EditText)findViewById(R.id.searchText);
        String newProduct ="";
        productTitle = productTitle.replaceAll("\\(" ," ");
        productTitle = productTitle.replaceAll("\\)" ," ");
        productTitle = productTitle.trim();
        StringTokenizer st = new StringTokenizer(productTitle);

        int i =0 ;
        while (st.hasMoreTokens() ) {
            String str =  st.nextToken();
            String n = ".*[0-9].*";

            if(i<=3) {
                newProduct = newProduct + str + " ";
            }
            else if (str.matches(n) ){

                newProduct = newProduct + str + " " ;

            }
            i++;
        }
        newProduct =  newProduct.trim();
        if(getIntent().hasExtra("SearchProductTitle") == false) {
            productList.add(compareproduct);
        }
        searchTextView.setText("Searched for " +newProduct);
        if(seller.equals("Amazon")==false) {

            AmazonFragment fragment = AmazonFragment.getInstance("", newProduct);
            fragment.searchFn(newProduct,false);
            productList.addAll(fragment.getProductList());
        }
        if(seller.equals("Ebay")==false){
            EbayFragment fragment = EbayFragment.getInstance(newProduct);
            try {
                fragment.searchFn(newProduct);
                productList.addAll(fragment.getProductList());
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }
        if(seller.equals("Flipkart")==false){
            FlipkartFragment fragment = FlipkartFragment.getInstance(newProduct);
            try {
                fragment.searchFn(newProduct);
                productList.addAll(fragment.getProductList());
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }
        orderProductsByPrice();
        mAdapter = new CompareProductLisAdapter(productList);
        setCompareFilterCount(String.valueOf(productList.size()));
        recyclerView.setAdapter(mAdapter);
        if(mAdapter!=null) {
            mAdapter.setCustomEventListener(new CompareProductLisAdapter.OnCompareCustomEventListener() {
                @Override
                public void setFilterCount(String filterCount) {
                    setCompareFilterCount(filterCount);

                }

            });
        }
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                mAdapter.getFilter().filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

        });

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 103) { // Please, use a final int instead of hardcoded int value
            if (resultCode == 103) {
                ArrayList<String> lcalcategoryList =  data.getStringArrayListExtra("CATEGORY_LIST_SELECTED");
                String sort = "";
                sort = data.getStringExtra("SORT_LIST_SELECTED");
                mAdapter.setCategoryList (lcalcategoryList,sort);
                if(lcalcategoryList.size()>0)
                    filterView.setText("Modify Filter");
                mAdapter.getFilter().filter("");
            }
        }
    }
    private void orderProductsByPrice() {

        Collections.sort(productList, product.priceCompare);
    }

    public void setCompareFilterCount(String compareFilterCount) {
        countText.setText(compareFilterCount +" products found");
    }
}
