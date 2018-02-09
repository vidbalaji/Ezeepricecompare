package com.vidhyalearning.ezeepricecompare;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Vector;

import static java.security.AccessController.getContext;

/**
 * Created by user on 15-Aug-17.
 */

public class CustomDialog extends Activity {
    ArrayList<String> categoryList =null;
    private ListView lvCheckBox;
    private Button btnCheckAll, btnClearALl,btnOk;
    private String[] arr = {"One", "Two", "Three", "Four", "Five", "Six"};
    Spinner sortSpinner;
    private String strLevel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(categoryList==null){
            categoryList=new ArrayList<String>();
        }
        strLevel="";
        setContentView(R.layout.dialog);
        categoryList.clear();
        btnOk = (Button)findViewById(R.id.btnOk);
        lvCheckBox = (ListView)findViewById(R.id.lvCheckBox);
        sortSpinner = (Spinner) findViewById(R.id.sortSpinner);

        ArrayList<String> classlist = new ArrayList<String>();
        classlist.add("High To Low");
        classlist.add("Low To High");
        ArrayAdapter<String> staticAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, classlist);


        // Specify the layout to use when the list of choices appears
        staticAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        sortSpinner.setAdapter(staticAdapter);

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Log.v("item", (String) parent.getItemAtPosition(position));
                strLevel = (String) parent.getItemAtPosition(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
        lvCheckBox.clearChoices();
        btnCheckAll = (Button) findViewById(R.id.btnCheckAll);
        btnClearALl = (Button) findViewById(R.id.btnClearAll);
        ArrayList<String> categoryListAll = null;
        if (getIntent().hasExtra("CATEGORY_LIST_ALL")){
            categoryListAll = getIntent().getStringArrayListExtra("CATEGORY_LIST_ALL");
            lvCheckBox.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            btnCheckAll.setVisibility(View.VISIBLE);
            btnClearALl.setVisibility(View.VISIBLE);
    }
    else if(getIntent().hasExtra("CATEGORY_LIST_ALL_SINGLE")){
            categoryListAll = getIntent().getStringArrayListExtra("CATEGORY_LIST_ALL_SINGLE");
            lvCheckBox.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            btnCheckAll.setVisibility(View.INVISIBLE);
            btnClearALl.setVisibility(View.INVISIBLE);
        }
        lvCheckBox.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_multiple_choice, categoryListAll));


        btnCheckAll.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View arg0)
            {
                for(int i=0 ; i < lvCheckBox.getAdapter().getCount(); i++)
                {
                    lvCheckBox.setItemChecked(i, true);
                }

            }
        });

        btnClearALl.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                for(int i=0 ; i < lvCheckBox.getAdapter().getCount(); i++)
                {
                    lvCheckBox.setItemChecked(i, false);
                }
            }
        });


        btnOk.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View arg0)
            {
                for(int i=0 ; i < lvCheckBox.getAdapter().getCount(); i++)
                {
                    if(lvCheckBox.isItemChecked(i)){
                        String category = (String)lvCheckBox.getItemAtPosition(i);
                        categoryList.add(category);
                    }
                }
                Intent intent = new Intent();
                intent.putStringArrayListExtra("CATEGORY_LIST_SELECTED",categoryList);
                intent.putExtra("SORT_LIST_SELECTED",strLevel);
                setResult(103,intent);

                finish();

            }
        });
    }
}
