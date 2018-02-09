package com.vidhyalearning.ezeepricecompare;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import static android.R.attr.id;

public class MainActivity extends FragmentActivity {
    /** Called when the activity is first created. */
    ImageButton amazonTab,flipkartTab,ebayTab,Tab4,Tab5;
    TabHost tabHost;
    public static String Tab="";
    String searchItem;
    EditText searchText;
    TextView textSearch;
    AmazonFragment amazonFragment;
    EbayFragment ebayFragment;
    ProductListAdapter mAdapter =null;
    FlipkartFragment flipkartFragment;
    ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchItem = getIntent().getStringExtra("SearchItem");
        textSearch = (TextView)findViewById(R.id.textSearch);
        searchText = (EditText)findViewById(R.id.searchText);
        textSearch.setText("Search Results for : " + searchItem);

        tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup();



        /** Setting tabchangelistener for the tab */
  //      tabHost.setOnClickListener(tabChangeListener);
        amazonTab=(ImageButton)findViewById(R.id.amazonTab);
        flipkartTab=(ImageButton)findViewById(R.id.flipkartTab);
        ebayTab=(ImageButton)findViewById(R.id.ebayTab);
        /** Defining tab builder for Andriod tab */
        TabHost.TabSpec tSpecAmazon = tabHost.newTabSpec("amazonTab");
        tSpecAmazon.setIndicator("amazonTab") ;
        tSpecAmazon.setContent(new DummyTabContent(getBaseContext()));

        TabHost.TabSpec tSpecFlipkart = tabHost.newTabSpec("flipkartTab");
        tSpecFlipkart.setIndicator("flipkartTab") ;
        tSpecFlipkart.setContent(new DummyTabContent(getBaseContext()));

        TabHost.TabSpec tSpecEbay = tabHost.newTabSpec("ebayTab");
        tSpecEbay.setIndicator("ebayTab") ;
        tSpecEbay.setContent(new DummyTabContent(getBaseContext()));
        tabHost.addTab(tSpecFlipkart);
        tabHost.addTab(tSpecAmazon);
        tabHost.addTab(tSpecEbay);
        View v = (View) findViewById(android.R.id.tabhost);
        v.setId(R.id.amazonTab);
        tabHostHandler(v);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(flipkartFragment !=null && flipkartFragment.getAdapter()!=null) {
                    flipkartFragment.getAdapter().setCustomEventListener(new ProductListAdapter.OnCustomEventListener(){
                        @Override
                        public void setFilterCount(String filterCount) {
                            flipkartFragment.setFilterCount(filterCount);

                        }

                    });
                    flipkartFragment.getAdapter().getFilter().filter(s.toString());

                   // String strCount = flipkartFragment.getAdapter().getFilterCount();

                }
                if(amazonFragment !=null && amazonFragment.getAdapter()!=null) {
                    amazonFragment.getAdapter().setCustomEventListener(new ProductListAdapter.OnCustomEventListener(){
                        @Override
                        public void setFilterCount(String filterCount) {
                            amazonFragment.setFilterCount(filterCount);

                        }

                    });
                    amazonFragment.getAdapter().getFilter().filter(s.toString());
                   // String strCount = amazonFragment.getAdapter().getFilterCount();
                  //  amazonFragment.setFilterCount(strCount);
                }
                if(ebayFragment !=null && ebayFragment.getAdapter()!=null) {
                    ebayFragment.getAdapter().setCustomEventListener(new ProductListAdapter.OnCustomEventListener(){
                        @Override
                        public void setFilterCount(String filterCount) {
                            ebayFragment.setFilterCount(filterCount);

                        }

                    });
                    ebayFragment.getAdapter().getFilter().filter(s.toString());
                    String strCount = ebayFragment.getAdapter().getFilterCount();
                    ebayFragment.setFilterCount(strCount);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

        });
    }
    /** Defining Tab Change Listener event. This is invoked when tab is changed */



        public void tabHostHandler(View v) {
            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();
            Toast.makeText(this,"Searching may take a while.Please Wait",Toast.LENGTH_LONG).show();
            if(v.getId()==R.id.amazonTab) {

                 amazonFragment = (AmazonFragment) fm.findFragmentByTag("amazonTab");
                if (amazonFragment != null)
                    ft.detach(amazonFragment);
                if (amazonFragment == null) {
                    /** Create AndroidFragment and adding to fragmenttransaction */
                    AmazonFragment fragment = AmazonFragment.newInstance(searchItem, "");
                    ft.add(android.R.id.tabcontent, fragment, "amazonTab");
                    amazonFragment = fragment;
                } else {
                    /** Bring to the front, if already exists in the fragmenttransaction */
                    ft.attach(amazonFragment);
                }

                tabHost.setCurrentTab(0);
            }
            else if(v.getId()==R.id.flipkartTab){
                 flipkartFragment = (FlipkartFragment) fm.findFragmentByTag("flipkartTab");
                if (flipkartFragment != null)
                    ft.detach(flipkartFragment);
                if (flipkartFragment == null) {
                    FlipkartFragment fragment = FlipkartFragment.newInstance(searchItem);
                    ft.add(android.R.id.tabcontent, fragment, "flipkartTab");
                    flipkartFragment = fragment;
                } else {
                    ft.attach(flipkartFragment);
                }

                tabHost.setCurrentTab(1);
            }
            else if(v.getId()==R.id.ebayTab){
                 ebayFragment = (EbayFragment) fm.findFragmentByTag("ebayTab");

                if (ebayFragment != null)
                    ft.detach(ebayFragment);
                if (ebayFragment == null) {
                    EbayFragment fragment = EbayFragment.newInstance(searchItem);
                    ft.add(android.R.id.tabcontent, fragment, "ebayTab");
                    ebayFragment = fragment;
                } else {
                    ft.attach(ebayFragment);
                }

                tabHost.setCurrentTab(2);
            }
            ft.commit();


        }


}

