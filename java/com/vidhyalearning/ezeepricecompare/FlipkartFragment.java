package com.vidhyalearning.ezeepricecompare;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class FlipkartFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String FK_AFFILIATE_ID = "XXX";
    private static final String FK_AFFILIATE_TOKEN = "XXXX";

    private static final String ARG_PARAM1 = "param1";
    private static final String FK_SEARCHURL = "https://affiliate-api.flipkart.net/affiliate/1.0/search.xml?query=";
    private static final String FK_COUNTURL ="&resultCount=";


    ArrayList<product> productList ;
    ArrayList<String> categoryList ;
    ArrayList<product> permanentproductList ;
    private RecyclerView recyclerView;
    TextView txtCnt;
    Button filterView;
    private ProductListAdapter mAdapter;
    // TODO: Rename and change types of parameters

    private String mSearchItem="";

    static boolean isFromNewInstance =false;
    public FlipkartFragment() {
        // Required empty public constructor
        productList = new ArrayList<>();
        categoryList = new ArrayList<>();
        permanentproductList = new ArrayList<>();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.

     * @return A new instance of fragment FlipkartFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FlipkartFragment newInstance(String param1) {
        FlipkartFragment fragment = new FlipkartFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        isFromNewInstance = true;

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSearchItem = getArguments().getString(ARG_PARAM1);
        }
    }
    private void parentSearchFn() {
        try {
            searchFn();
            mAdapter = new ProductListAdapter(productList);

            recyclerView.setAdapter(mAdapter);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public ProductListAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_flipkart, container, false);
        view.setBackgroundColor(Color.parseColor("#ADE000"));
        txtCnt = (TextView)view.findViewById(R.id.textCount);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView1);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(llm);


        if(isFromNewInstance)
        {
            categoryList.clear();
            permanentproductList.clear();
            parentSearchFn();
            permanentproductList.addAll(productList);
            isFromNewInstance=false;

        }
        else{
            permanentproductList.clear();
            permanentproductList.addAll(productList);
        }

        Collections.sort(permanentproductList, product.priceCompare);
        mAdapter = new ProductListAdapter(permanentproductList);


        recyclerView.setAdapter(mAdapter);
        int count = mAdapter.getItemCount();
        String strCount = String.valueOf(count);
        filterView = (Button)view.findViewById(R.id.filterText);
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
                intent.setClass(getContext(),CustomDialog.class);
                filterView.setText("Apply Filter");
                startActivityForResult(intent,103);
            }
        });
        txtCnt.setText(strCount +  " products found");
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    public ArrayList<product> getProductList(){
        return productList;
    }

    public void setFilterCount(String filterCount) {
        txtCnt.setText(filterCount + " products found");
    }
    public void setFlipkartFilterCount(String filterCount) {
        txtCnt.setText(filterCount + " products found");
    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */

    public void searchFn(String s) throws ExecutionException, InterruptedException {


      mSearchItem = s;
        searchFn();

    }
    public void searchFn() throws ExecutionException, InterruptedException {


        String searchStr = mSearchItem;
        searchStr=searchStr.replace(" ","%20");

        String finalUrl = FK_SEARCHURL + searchStr+FK_COUNTURL+20;
        HttpGetRequest getRequest = new HttpGetRequest();
        //Perform the doInBackground method, passing in our url
        getRequest.execute(finalUrl).get();
        productList = getRequest.getXml();
        //mAdapter = new ProductListAdapter(productList);

       // recyclerView.setAdapter(mAdapter);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 103) { // Please, use a final int instead of hardcoded int value
            if (resultCode == 103) {
                ArrayList<String> lcalcategoryList =  data.getStringArrayListExtra("CATEGORY_LIST_SELECTED");
                String sort = "";
                sort = data.getStringExtra("SORT_LIST_SELECTED");
                getAdapter().setCategoryList (lcalcategoryList, sort);
                getAdapter().getFilter().filter("");
                if(lcalcategoryList.size()>0)
                filterView.setText("Modify Filter");
                getAdapter().setCustomEventListener(new ProductListAdapter.OnCustomEventListener(){
                    @Override
                    public void setFilterCount(String filterCount) {
                        setFlipkartFilterCount(filterCount);

                    }

                });
            }
        }
    }
static FlipkartFragment mFragment;
    public static FlipkartFragment getInstance(String newProduct) {
        if(mFragment==null) {
            FlipkartFragment fragment = new FlipkartFragment();
            Bundle args = new Bundle();
            args.putString(ARG_PARAM1, newProduct);
            fragment.setArguments(args);
            mFragment =fragment;
        }
        return mFragment;
    }

    public class HttpGetRequest extends AsyncTask<String, Void, String> {

        ArrayList<product> tempproductList = new ArrayList<product>();
        String result;
        @Override
        protected String doInBackground(String... params) {
            String stringUrl = params[0];


            try{
                Log.d("FLIPKART",stringUrl);
                URL url = new URL(stringUrl);
                HttpURLConnection con = null;
                try {
                    con = (HttpURLConnection) url.openConnection();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                try {
                    con.setRequestMethod("GET");
                } catch (ProtocolException e1) {
                    e1.printStackTrace();
                }
                con.setRequestProperty("Fk-Affiliate-Token",FK_AFFILIATE_TOKEN );
                con.setRequestProperty("Fk-Affiliate-Id", FK_AFFILIATE_ID);

                int status = 0;
                try {

                    status = con.getResponseCode();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                switch(status) {

                    case HttpURLConnection.HTTP_GONE:
                        // The timestamp is expired.
                        throw new AffiliateAPIException("URL expired");

                    case HttpURLConnection.HTTP_UNAUTHORIZED:
                        // The API Token or the Tracking ID is invalid.
                        throw new AffiliateAPIException("API Token or Affiliate Tracking ID invalid.");

                    case HttpURLConnection.HTTP_FORBIDDEN:
                        // Tampered URL, i.e., there is a signature mismatch.
                        // The URL contents are modified from the originally returned value.
                        throw new AffiliateAPIException("Tampered URL - The URL contents are modified from the originally returned value");

                    case HttpURLConnection.HTTP_OK:
                        parseUsingDOM(con.getInputStream());
                        break;
                    default:
                        throw new AffiliateAPIException("Connection error with the Affiliate API service: HTTP/" + status);
                }


            }  catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (AffiliateAPIException affiliateAPIException) {
                affiliateAPIException.printStackTrace();
            }

            return result;

        }

        private void parseUsingDOM(InputStream inputStream) throws IOException {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = null;
            try {
                dBuilder = dbFactory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
            Document doc = null;
            try {
                doc = dBuilder.parse(inputStream);
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Element element=doc.getDocumentElement();
            element.normalize();

            NodeList nList = doc.getElementsByTagName("productInfoList");
            product itemProduct =null;
            for (int i=0; i<nList.getLength(); i++) {
                itemProduct = new product();
                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element2 = (Element) node;
                    String feature = "";
                    feature= "Feature: \n" + getTitleValue("categorySpecificInfoV1", element2,"keySpecs");

                    String title = getTitleValue("productBaseInfoV1", element2,"title");
                    String tempSeatchItem = mSearchItem;
                    if(tempSeatchItem.isEmpty() == false &&tempSeatchItem.contains("\""))
                        tempSeatchItem.replaceAll("\""," ");
                    tempSeatchItem =tempSeatchItem.trim();
                    int firstSpace =tempSeatchItem.indexOf(" ");
                    String compulsaryWord=tempSeatchItem;
                    if(firstSpace >0)
                        compulsaryWord = compulsaryWord.substring(0,firstSpace);
                    if (title.toLowerCase().contains(compulsaryWord.toLowerCase()) == false)
                        continue;
                    itemProduct.setTitle(title);
                    String price = getTitleValue("productBaseInfoV1", element2,"flipkartSellingPrice");
                    float fPrice = 0;
                    if(price.isEmpty()==false) {
                        fPrice = Float.parseFloat(price);
                    }
                    itemProduct.setWithoutUnitPrice(fPrice);
                    String rupee;
                    rupee= "₹";
                    itemProduct.setPrice(rupee+price);
                    String mrp = getTitleValue("productBaseInfoV1", element2,"maximumRetailPrice");
                    float fMrpPrice = 0;
                    if(mrp.isEmpty()==false) {
                        fMrpPrice = Float.parseFloat(mrp);
                    }
                    itemProduct.setWithoutUnitMrpPrice(fMrpPrice);
                    Bitmap image =  getImageValue("productBaseInfoV1",element2,"small");
                    itemProduct.setImage(image);
                    Bitmap largeimage =  getImageValue("productBaseInfoV1",element2,"large");
                    itemProduct.setLargeImage(largeimage);
                    String url = getTitleValue("productBaseInfoV1",element2,"productUrl");
                    itemProduct.setUrl(url);
                    String reviewUrl = "";
                    reviewUrl=url.replace("/p/","/product-reviews/");
                    itemProduct.setReviewUrl(reviewUrl);
                    String asin = getUrlASINValue("productId",element2);
                    itemProduct.setAsin(asin);
                    itemProduct.setSeller("Flipkart");
                    String deliveryTime = getTitleValue("productShippingInfoV1",element2,"estimatedDeliveryTime");
                    feature =feature + deliveryTime;
                    itemProduct.setFeature(feature);
                    itemProduct.setDeliveryTime("");
                    String shipCharges = getTitleValue("productShippingInfoV1",element2,"shippingCharges");
                    itemProduct.setShipCharges(rupee+shipCharges);
                    String categoryName = getTitleValue("productBaseInfoV1", element2,"categoryPath");
                    itemProduct.setCategoryName(categoryName);
                    itemProduct.setMrp(rupee+mrp);

                    tempproductList.add(itemProduct);
                }
            }
        }

        private String getUrlASINValue(String tag, Element element) {

            String url ="";
            try {
                NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
                url = nodeList.item(0).getNodeValue();
            }
            catch(Exception e){
                Log.d("Ezee Compare",e.getMessage());
                e.printStackTrace();
            }
            return url;
        }

        private Bitmap getImageValue(String tag, Element element,String imgType) throws IOException {
            Bitmap bmp = null;
            try {
                NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
                String title = "";

                NodeList childNodeList = null;
                if (tag.equals("productBaseInfoV1")) {
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        String node = nodeList.item(i).getNodeName();
                        if (node.equals("imageUrls")) {
                            childNodeList = nodeList.item(i).getChildNodes();
                            break;
                        }
                    }
                    String node = "";
                    try {
                        int j = 0;
                        if (imgType.equals("small")) {
                            j = 0;
                        } else
                            j = 2;

                        // for (int i = 0; i < childNodeList.getLength(); i++) {
                        node = childNodeList.item(j).getChildNodes().item(1).getChildNodes().item(0).getNodeValue();
                        URL imageurl = new URL(node);
                        BitmapFactory.Options o = new BitmapFactory.Options();
                        o.inJustDecodeBounds = true;
                        InputStream stream1 = imageurl.openConnection().getInputStream();
                        bmp = BitmapFactory.decodeStream(stream1,null,o);
                        stream1.close();
                        int width_tmp = o.outWidth, height_tmp = o.outHeight;
                        int scale = 1;
                        while (true) {
                            if (width_tmp / 2 < 300
                                    || height_tmp / 2 < 300)
                                break;
                            width_tmp /= 2;
                            height_tmp /= 2;
                            scale *= 2;
                        }
                        // decode with inSampleSize
                        BitmapFactory.Options o2 = new BitmapFactory.Options();
                        o2.inSampleSize = scale;
                        InputStream stream2 = imageurl.openConnection().getInputStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, o2);
                        stream2.close();
                        bmp = bitmap;
                        // }
                    } catch (Exception ex) {
                        Log.d("Flipkart", ex.getMessage());
                        Log.d("Flipkart", node);
                    }


                }
            }
            catch(Exception e){
                Log.d("Ezee Compare",e.getMessage());
                e.printStackTrace();
            }
            return bmp;
        }
        private  String getTitleValue(String tag, Element element,String findElement) {
            NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
            String title="";
            int foundTitle=0;
            try {
                if (tag.equals("categorySpecificInfoV1")) {
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        String node = nodeList.item(i).getNodeName();
                        if (findElement.equals("keySpecs") && node.equals("keySpecs")) {
                            title = title + ">"+ nodeList.item(i).getChildNodes().item(0).getNodeValue() + "\n";
                            foundTitle = 1;
                        } else if (findElement.equals("keySpecs") && foundTitle == 1) {
                          break;
                        }
                    }
                }
                if (tag.equals("productBaseInfoV1")) {
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        String node = nodeList.item(i).getNodeName();
                        if (findElement.equals("productUrl") && node.equals("productUrl")) {
                            title = nodeList.item(i).getChildNodes().item(0).getNodeValue();
                            break;
                        } else if (findElement.equals("title") && node.equals("title")) {
                            title = nodeList.item(i).getChildNodes().item(0).getNodeValue();
                            break;
                        } else if (findElement.equals("flipkartSellingPrice") && node.equals("flipkartSellingPrice")) {
                            Node node1 = nodeList.item(i);
                            String title1 = node1.getChildNodes().item(0).getNodeName();
                            if (title1.equals("amount")) {
                                title = node1.getChildNodes().item(0).getChildNodes().item(0).getNodeValue();
                                break;
                            }

                        } else if (findElement.equals("maximumRetailPrice") && node.equals("maximumRetailPrice")) {
                            Node node1 = nodeList.item(i);
                            String title1 = node1.getChildNodes().item(0).getNodeName();
                            if (title1.equals("amount")) {
                                title = node1.getChildNodes().item(0).getChildNodes().item(0).getNodeValue();
                                break;
                            }

                        }
                        else if (findElement.equals("categoryPath") && node.equals("categoryPath")) {
                                title = nodeList.item(i).getChildNodes().item(0).getNodeValue();
                                break;


                        }

                    }
                }
                if (tag.equals("productShippingInfoV1")) {
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        String node = nodeList.item(i).getNodeName();
                        if (findElement.equals("estimatedDeliveryTime") && node.equals("estimatedDeliveryTime")) {
                            title = nodeList.item(i).getChildNodes().item(0).getNodeValue();
                            break;
                        } else if (findElement.equals("shippingCharges") && node.equals("shippingCharges")) {
                            Node node1 = nodeList.item(i);
                            String title1 = node1.getChildNodes().item(0).getNodeName();
                            if (title1.equals("amount")) {
                                title = node1.getChildNodes().item(0).getChildNodes().item(0).getNodeValue();
                                break;
                            }
                        }
                    }
                }
            }
            catch(Exception ex){
                    Log.d("flipkart",ex.getMessage());
                    ex.printStackTrace();
                }
            return title;
        }



        protected void onPostExecute(String result){
            super.onPostExecute(result);
        }

        public ArrayList<product> getXml() {
            return tempproductList;
        }
    }
}
