package com.vidhyalearning.ezeepricecompare;
import android.content.Context;
import android.view.View;
import android.widget.TabHost.TabContentFactory;
/**
 * Created by user on 7/23/2017.
 */



public class DummyTabContent implements TabContentFactory{
        private Context mContext;

        public DummyTabContent(Context context){
            mContext = context;
        }

        @Override
        public View createTabContent(String tag) {
            View v = new View(mContext);
            return v;
        }


}
