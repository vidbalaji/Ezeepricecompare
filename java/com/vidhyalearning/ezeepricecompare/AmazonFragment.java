package com.vidhyalearning.ezeepricecompare;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.StreamHandler;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static java.lang.Thread.sleep;



public class AmazonFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String AWS_ACCESS_KEY_ID = "X";
    /*
     * Your AWS Secret Key corresponding to the above ID, as taken from the AWS
     * Your Account page.
     */
    private static final String AWS_SECRET_KEY = "Xxxx";


    private static final String ENDPOINT = "webservices.amazon.in";
    static boolean isFromNewInstance =false;
    public static AmazonFragment mFragment;
    ArrayList<product> productList ;
    ArrayList<product> permanentproductList ;
    ArrayList<String> categoryList ;
    Map<String,String> categoryKeyValue ;
    Boolean mIsBarcode = false;
   private RecyclerView recyclerView;
    private ProductListAdapter mAdapter;
    ProgressDialog progressDialog;
    //BackgroundThread backgroundThread;
    Button filterView;

    // TODO: Rename and change types of parameters
    private String mSearchItem="";

    public String mProductTitle;

    TextView txtCnt;
    public AmazonFragment() {
        // Required empty public constructor
        productList = new ArrayList<>();
        permanentproductList = new ArrayList<>();
        categoryList = new ArrayList<>();
        categoryKeyValue = new HashMap<>();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AmazonFragment.
     */
    public static AmazonFragment getInstance(String param1, String param2) {
        if(mFragment==null) {
            AmazonFragment fragment = new AmazonFragment();
            Bundle args = new Bundle();
            args.putString(ARG_PARAM1, param1);
            args.putString(ARG_PARAM2, param2);
            fragment.setArguments(args);
            mFragment =fragment;
        }
        return mFragment;

    }
    // TODO: Rename and change types and number of parameters
    public static AmazonFragment newInstance(String param1, String param2) {
        AmazonFragment fragment = new AmazonFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        isFromNewInstance=true;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSearchItem = getArguments().getString(ARG_PARAM1);


        }
    }
    public void onResume() {
       super.onResume();
    }

    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_amazon, container, false);
        view.setBackgroundColor(Color.parseColor("#FFE100"));
                filterView = (Button)view.findViewById(R.id.filterText);
        filterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryList.clear();
                Intent intent = new Intent();
                if(productList.isEmpty()==false){
                    for(int i =0 ;i<productList.size();i++){
                        if(categoryList.contains(productList.get(i).categoryName)==false) {
                            categoryList.add(productList.get(i).categoryName);
                        }
                    }
                }
                intent.putStringArrayListExtra("CATEGORY_LIST_ALL",categoryList);
                intent.setClass(getContext(),CustomDialog.class);
                filterView.setText("Apply Filter");
                startActivityForResult(intent,103);
            }
        });
        final Button topSeller = (Button)view.findViewById(R.id.topSellertext);
        topSeller.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                PopupMenu popup = new PopupMenu(getContext(), topSeller);
                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId()==R.id.one) {
                            Intent intent = new Intent();
                            ArrayList<String> lcategoryList = new ArrayList<String>();
                            if (permanentproductList.isEmpty() == false) {

                                for (int i = 0; i < permanentproductList.size(); i++) {

                                    if (lcategoryList.contains(permanentproductList.get(i).categoryName) == false) {

                                        categoryKeyValue.put(permanentproductList.get(i).categoryId, permanentproductList.get(i).categoryName);
                                        lcategoryList.add(permanentproductList.get(i).categoryName);
                                    }
                                }

                                intent.putStringArrayListExtra("CATEGORY_LIST_ALL_SINGLE", lcategoryList);
                                intent.setClass(getContext(), CustomDialog.class);

                                startActivityForResult(intent, 105);
                            }
                        }
                            else if(item.getItemId()==R.id.two){

                                String url="http://amzn.to/2vNBYwY";

                                Uri uriUrl = Uri.parse(url);
                                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                                startActivity(launchBrowser);
                            }
                            else if(item.getItemId()==R.id.three){

                                String url="http://amzn.to/2eXg0ED";
                                Uri uriUrl = Uri.parse(url);
                                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                                startActivity(launchBrowser);
                            }

                        return true;
                    }
                });

                popup.show();//showing popup menu
            }
        });
        txtCnt = (TextView)view.findViewById(R.id.textCount);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(llm);

        if(isFromNewInstance)
        {
            categoryList.clear();
            categoryKeyValue.clear();
            permanentproductList.clear();
            searchFn(mSearchItem, false);
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
        txtCnt.setText(strCount +  " products found");

        return view;
    }

    public void setAmazonFilterCount(String filterCount) {
        txtCnt.setText(filterCount + " products found");
    }
    public void setFilterCount(String filterCount) {
        txtCnt.setText(filterCount + " products found");
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 103) {
            if (resultCode == 103) {
                ArrayList<String> lcalcategoryList =  data.getStringArrayListExtra("CATEGORY_LIST_SELECTED");
                String sort = "";
                sort = data.getStringExtra("SORT_LIST_SELECTED");
                getAdapter().setCategoryList (lcalcategoryList,sort);
                if(lcalcategoryList.size()>0)
                    filterView.setText("Modify Filter");
                getAdapter().getFilter().filter("");
                getAdapter().setCustomEventListener(new ProductListAdapter.OnCustomEventListener(){
                    @Override
                    public void setFilterCount(String filterCount) {
                        setAmazonFilterCount(filterCount);

                    }

                });
            }
        }
        else if(requestCode == 105) {
            if (resultCode == 103) {
                ArrayList<String> lcalcategoryList = data.getStringArrayListExtra("CATEGORY_LIST_SELECTED");

                if (lcalcategoryList.size() > 0) {
                    String lCategory = lcalcategoryList.get(0);
                    String lCategoryValue = "";
                    Intent intent = new Intent();
                    if (categoryKeyValue.containsValue(lCategory)) {
                        for (String o : categoryKeyValue.keySet()) {
                            if (categoryKeyValue.get(o).equals(lCategory)) {
                                lCategoryValue = o;
                                break;
                            }
                        }
                        intent.putExtra("CATEGORY_TOP_SELLER", lCategoryValue);
                        intent.setClass(getContext(), Dotd.class);
                        startActivity(intent);
                    }
                }
            }


        }
        }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    public ProductListAdapter getAdapter() {
        return mAdapter;
    }



    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.N)
    public String searchFn(String barcode, Boolean isBarcode) {
        try {
            String searchStr = barcode;
            mIsBarcode=isBarcode;
            if(mSearchItem==null || mSearchItem.isEmpty())
            mSearchItem = barcode;
            SignedRequestsHelper sh = new SignedRequestsHelper();

            SignedRequestsHelper helper;

            try {
                helper = SignedRequestsHelper.getInstance(ENDPOINT, AWS_ACCESS_KEY_ID, AWS_SECRET_KEY);
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }

            String[] requestUrl = new String[5];

                Map<String, String> params = new HashMap<String, String>();

                params.put("Service", "AWSECommerceService");
                params.put("Operation", "ItemSearch");
                params.put("AWSAccessKeyId", "XXXX");
                params.put("AssociateTag", "xxx");
                params.put("SearchIndex", "All");

                params.put("ResponseGroup", "BrowseNodes,Images,ItemAttributes,Offers,OfferSummary");
                params.put("Availability", "Available");
                params.put("Keywords", searchStr);
            for(int i=0;i<=4;i++) {
                String strPage = String.valueOf(i+1);
                params.put("ItemPage", strPage);
                requestUrl[i] = helper.sign(params);
                Log.d("Amazon", requestUrl[i]);
            }
            parseUrl(requestUrl);


        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();

        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return mProductTitle;
    }
    public ArrayList<product> getProductList(){
        return productList;
    }
    HttpGetRequest m_request=null;
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void parseUrl(String[] requestUrl) throws XmlPullParserException {

        try {

            //  final URL url = new URL(requestUrl);


            productList.clear();
            // We will get the XML from an input stream
            final InputStream[] consInpt = {null};
            final String[] finalRequestUrl = requestUrl;

            final HttpGetRequest getRequest = new HttpGetRequest(getContext());
            m_request = getRequest;
            //Perform the doInBackground method, passing in our url
            getRequest.execute(finalRequestUrl).get();

            int i=0;
            productList = getRequest.getTempproductList();
            mProductTitle=getRequest.getTitle();


        }

        catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
public Boolean getFinishedStatus(){
    return (m_request != null && m_request.getStatus().equals(AsyncTask.Status.FINISHED));
}
    public InputStream getInputStream(URL url) {
        try {
            // return url.openConnection().getInputStream();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            return conn.getInputStream();


        } catch (IOException e) {
            return null;
        }
    }

    public class HttpGetRequest extends AsyncTask<String, Integer, String> {
        public static final String REQUEST_METHOD = "GET";
        public static final int READ_TIMEOUT = 15000;
        public static final int CONNECTION_TIMEOUT = 15000;
        String mTitle;
        ArrayList<product> tempproductList = new ArrayList<product>();
        String result;
        Boolean moreResults=true;
        boolean isCompleted =false;

        public HttpGetRequest(Context context) {


        }

        @Override
        protected String doInBackground(String... params) {
            String[] stringUrl = params;

            moreResults=true;
            try {

                for(int i =0;i<=4;i++) {
                    if(!moreResults)
                        break;
                    //Create a URL object holding our url
                    URL myUrl = new URL(stringUrl[i]);
                    //Create a connection
                    HttpURLConnection connection = (HttpURLConnection)
                            myUrl.openConnection();
                    //Set methods and timeouts
                    connection.setRequestMethod(REQUEST_METHOD);
                    connection.setReadTimeout(READ_TIMEOUT);
                    connection.setConnectTimeout(CONNECTION_TIMEOUT);

                    //Connect to our url
                    connection.connect();

                    parseUsingDOM(connection.getInputStream());
                }
                isCompleted=true;
            }   catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            isCompleted=true;
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

            NodeList nList = doc.getElementsByTagName("Item");
            if(nList==null || (nList!=null && nList.getLength()==0)){
                moreResults=false;
            }
            product itemProduct =null;
            for (int i=0; i<nList.getLength(); i++) {

                itemProduct = new product();
                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {

                    Element element2 = (Element) node;
                    String titleTofind="title";
                    String title = getTitleValue("ItemAttributes", element2,titleTofind);
                    if(!mIsBarcode) {
                        String tempSeatchItem = mSearchItem;
                        if (tempSeatchItem.isEmpty() == false && tempSeatchItem.contains("\""))
                            tempSeatchItem.replaceAll("\"", " ");
                        tempSeatchItem = tempSeatchItem.trim();
                        int firstSpace = tempSeatchItem.indexOf(" ");
                        String compulsaryWord = tempSeatchItem;
                        if (firstSpace > 0)
                            compulsaryWord = compulsaryWord.substring(0, firstSpace);
                        if (title.toLowerCase().contains(compulsaryWord.toLowerCase()) == false)
                            continue;
                    }
                    itemProduct.setTitle(title);
                    mTitle=title;
                    titleTofind = "mrp";
                    String mrp = getTitleValue("ItemAttributes", element2,titleTofind);
                    String formattedMrp = "0";
                    if(mrp.isEmpty()==false) {
                        int delim1 = mrp.indexOf("+");
                        if (mrp.length() > delim1 + 1)
                            formattedMrp = mrp.substring(delim1 + 1, mrp.length() - 1);
                        mrp = mrp.substring(0, delim1);
                        float fMrp = Float.parseFloat(mrp);
                        fMrp = fMrp / 100;
                        itemProduct.setWithoutUnitMrpPrice(fMrp);
                    }
                    String price = getTitleValue("OfferSummary", element2,"");
                    String formattedPrice="";
                    if(price.isEmpty()==false) {
                        int delim2 = price.indexOf("+");
                         formattedPrice = price.substring(delim2 + 1, price.length() - 1);
                        price = price.substring(0, delim2);
                        float fPrice = Float.parseFloat(price);
                        fPrice = fPrice / 100;
                        itemProduct.setWithoutUnitPrice(fPrice);
                    }
                    String rupee ;
                    if(getContext()!=null)
                     rupee = getResources().getString(R.string.Rs);
                    else
                        rupee= "₹";
                    if(formattedPrice.isEmpty()==false)
                        formattedPrice = formattedPrice.replace("INR",rupee);
                    itemProduct.setPrice(formattedPrice);
                    if(formattedMrp.isEmpty()==false)
                        formattedMrp = formattedMrp.replace("INR",rupee);
                    itemProduct.setMrp(formattedMrp);
                    Bitmap image = getImageValue("MediumImage",element2);
                    itemProduct.setImage(image);
                    Bitmap largeimage = getImageValue("LargeImage",element2);
                    itemProduct.setLargeImage(largeimage);
                    String url = getUrlASINValue("DetailPageURL",element2);
                    itemProduct.setUrl(url);
                    url = getUrlASINValue("ASIN",element2);
                    String deliveryTime = getTitleValue("Offers", element2,"");
                    itemProduct.setAsin(url);
                    itemProduct.setSeller("Amazon");
                    itemProduct.setDeliveryTime("");
                    String categoryId = "";
                    categoryId=getCategoryValue("BrowseNodes", element2,"");
                    int delim  = categoryId.indexOf("+");
                    String categoryName = "";
                    if(delim>=0 ) {
                        categoryName = categoryId.substring(delim + 1, categoryId.length() - 1);
                        if(categoryId.isEmpty()==false)
                        categoryId = categoryId.substring(0, delim);
                    }
                    itemProduct.setCategoryId(categoryId);
                    itemProduct.setCategoryName(categoryName);
                    String feature="";
                    titleTofind = "Feature";
                     feature = "Features: \n" + getTitleValue("ItemAttributes", element2,titleTofind);
                    feature=feature +deliveryTime;
                    itemProduct.setFeature(feature);
                    String reviewUrl = getTitleValue("ItemLinks", element2,"");
                    itemProduct.setReviewUrl(reviewUrl);
                    tempproductList.add(itemProduct);
                }
            }
        }

        private String getCategoryValue(String browseNodes, Element element2, String s) {
            String category = "";

            try {
                NodeList nodeList = element2.getElementsByTagName(browseNodes).item(0).getChildNodes();

                if (nodeList != null) {
                    NodeList firstNode = nodeList.item(0).getChildNodes();
                    String BrowseNodeId = firstNode.item(0).getChildNodes().item(0).getNodeValue();

                    String BrowseNodeName = firstNode.item(1).getChildNodes().item(0).getNodeValue();
                    category = BrowseNodeId + "+" + BrowseNodeName;
                }

            }catch(Exception ex){
                Log.d("Category Exception",ex.getMessage());
            }
            return category;
        }

        private String getUrlASINValue(String tag, Element element) {
            String url="";
            try {
                NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
                 url = nodeList.item(0).getNodeValue();
            }
            catch(Exception ex){
                ex.printStackTrace();
            }

            return url;
        }

        private Bitmap getImageValue(String tag, Element element) throws IOException {
            Bitmap bmp = null;
            try {
                NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
                String nodeName = nodeList.item(0).getNodeName();

                if (nodeName.equals("URL")) {
                    String url = nodeList.item(0).getChildNodes().item(0).getNodeValue();
                    URL imageurl = new URL(url);
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

                }

            }
            catch(Exception ex){
                ex.printStackTrace();
            }


            return bmp;

        }

        private  String getTitleValue(String tag, Element element,String titleTofind)
        {
            String title = "";
            int foundTitle=0;
            try {
                NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
                if (nodeList == null) {
                    Log.d("Amazaon Fragment", "Not available" + tag);
                    return "NA";
                }
                if (tag.equals("ItemAttributes")) {
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        String node = nodeList.item(i).getNodeName();

                        if (titleTofind.equals("title") && node.equals("Title")) {
                            title = nodeList.item(i).getChildNodes().item(0).getNodeValue();
                            break;
                        }
                        if (titleTofind.equals("mrp") && node.equals("ListPrice")) {
                            Node node1 = nodeList.item(i);
                            String unitPrice="",formattedPrice="";
                            String title2 = node1.getChildNodes().item(0).getNodeName();
                            if (title2.equals("Amount")) {
                                unitPrice = node1.getChildNodes().item(0).getChildNodes().item(0).getNodeValue();

                            }
                            String title1 = node1.getChildNodes().item(2).getNodeName();
                             if (title1.equals("FormattedPrice")) {
                                 formattedPrice = node1.getChildNodes().item(2).getChildNodes().item(0).getNodeValue();

                            }
                            title = unitPrice +"+" +formattedPrice;
                            break;
                        }
                        if (titleTofind.equals("Feature") && node.equals("Feature")) {
                            title = title + ">" + nodeList.item(i).getChildNodes().item(0).getNodeValue() + "\n";
                            foundTitle=1;
                        }
                        else if(titleTofind.equals("Feature") && foundTitle==1 ){
                            break;
                        }

                    }
                }else if(tag.equals("ItemLinks")){
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        String node = nodeList.item(i).getNodeName();
                        Node node1 = nodeList.item(i);
                        if (node.equals("ItemLink")) {
                            String description = node1.getChildNodes().item(0).getChildNodes().item(0).getNodeValue();
                            if(description.contains("Reviews")){
                                String url =  node1.getChildNodes().item(1).getChildNodes().item(0).getNodeValue();
                                return url;
                            }
                        }

                    }
                }

                else if (tag.equals("OfferSummary")) {
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        String node = nodeList.item(i).getNodeName();
                        Node node1 = nodeList.item(i);
                        if (node.equals("LowestNewPrice")) {

                            String unitPrice="",formattedPrice="";
                            String title2 = node1.getChildNodes().item(0).getNodeName();
                            if (title2.equals("Amount")) {
                                unitPrice = node1.getChildNodes().item(0).getChildNodes().item(0).getNodeValue();

                            }
                            String title1 = node1.getChildNodes().item(2).getNodeName();
                            if (title1.equals("FormattedPrice")) {
                                formattedPrice = node1.getChildNodes().item(2).getChildNodes().item(0).getNodeValue();

                            }
                            title = unitPrice +"+" +formattedPrice;

                        }
                    }
                } else if (tag.equals("Offers")) {
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        String node = nodeList.item(i).getNodeName();
                        Node node1 = nodeList.item(i);
                        if (node.equals("Offer")) {
                            String title1 = node1.getChildNodes().item(1).getNodeName();
                            if (title1.equals("OfferListing")) {
                                title = node1.getChildNodes().item(1).getChildNodes().item(4).getChildNodes().item(0).getNodeValue();
                                break;
                            }

                        }
                    }
                }
                return title;
            }catch(Exception ex){
               Log.d("Amazzon",ex.getMessage());
            }

            return title;
        }

        protected void onPostExecute(String result){

            super.onPostExecute(result);
        }

        public ArrayList<product> getTempproductList() {
            return tempproductList;
        }
        public String getTitle(){return mTitle;}


    }
}
