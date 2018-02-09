package com.vidhyalearning.ezeepricecompare;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class Dotd extends AppCompatActivity {


    private static final String AWS_ACCESS_KEY_ID = "xxxx";
    /*
     * Your AWS Secret Key corresponding to the above ID, as taken from the AWS
     * Your Account page.
     */
    private static final String AWS_SECRET_KEY = "XXXX";
    /*
     * Use the end-point according to the region you are interested in.
     */
    private static final String ENDPOINT = "webservices.amazon.in";
    ArrayList<product> productList;

    private RecyclerView recyclerView;
    private ProductListAdapter mAdapter;
    EditText searchText;
    private String mSearchItem = "";
    public String mProductTitle;
    TextView noResultsText;

    ArrayList<String> urlArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dotd);
        if(urlArray==null){
            urlArray = new ArrayList<>();
        }
        if(productList==null){
            productList = new ArrayList<product>();
        }
        String searchItem = "";
        if (getIntent().hasExtra("CATEGORY_TOP_SELLER")) {
            searchItem = getIntent().getStringExtra("CATEGORY_TOP_SELLER");
        }
        setTitle("Amazon Top Sellers for Category:");
        productList.clear();
        recyclerView = (RecyclerView) findViewById(R.id.topsellerrecyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(llm);

        searchFn(searchItem);
        Collections.sort(productList, product.priceCompare);
        mAdapter = new ProductListAdapter(productList);
        recyclerView.setAdapter(mAdapter);
        mAdapter.getFilter().filter("");
        noResultsText = (TextView)findViewById(R.id.noResultsText);
        searchText = (EditText)findViewById(R.id.searchText);
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

    public String searchFn(String barcode) {
        try {


            if (mSearchItem == null || mSearchItem.isEmpty())
                mSearchItem = barcode;

            SignedRequestsHelper helper;

            try {
                helper = SignedRequestsHelper.getInstance(ENDPOINT, AWS_ACCESS_KEY_ID, AWS_SECRET_KEY);
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }

            String requestUrl = "";

            Map<String, String> params = new HashMap<String, String>();

            params.put("Service", "AWSECommerceService");
            params.put("Operation", "BrowseNodeLookup");
            params.put("AWSAccessKeyId", "XXXX");
            params.put("AssociateTag", "XXX");
            params.put("BrowseNodeId", barcode);
            params.put("ResponseGroup", "NewReleases,TopSellers");
            requestUrl = helper.sign(params);
            parseUrl(requestUrl, "TopSeller");


        } catch (Exception e) {
            e.printStackTrace();

        }
        return mProductTitle;
    }

    private void parseUrl(String requestUrl, final String topSeller) {
        Log.d("Amazon top",requestUrl);
        final RequestQueue rq = Volley.newRequestQueue(this);
        StringRequest req = new StringRequest(StringRequest.Method.GET, requestUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        processData(response);
                        if (urlArray.size() > 0) {
                            parseASINUrl("ASIN");

                        }

                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // handle error response
                        Log.d("Error Volley", error.getMessage());
                    }
                }
        );
        rq.add(req);
    }

    private void parseASINUrl(final String topSeller) {

        for (int i = 0; i < urlArray.size(); i++) {
            String requestUrl = urlArray.get(i);
            Log.d("Amazon top",requestUrl);
            final RequestQueue rq = Volley.newRequestQueue(this);
            StringRequest req = new StringRequest(StringRequest.Method.GET, requestUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                parseUsingDOM(response);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // handle error response
                            Log.d("Error Volley", error.getMessage());
                        }
                    }
            );
            rq.add(req);
            RequestQueue rqTemp = null;
            if (i == urlArray.size() - 1) {
                rqTemp = rq;
            }
            final RequestQueue finalRqTemp = rqTemp;

            RequestQueue.RequestFinishedListener listener =
                    new RequestQueue.RequestFinishedListener() {
                        @Override
                        public void onRequestFinished(Request request) {
                            if (request.equals(finalRqTemp)) {

                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    };
            if (finalRqTemp != null)
                finalRqTemp.addRequestFinishedListener(listener);
        }
    }

    private void processData(String response) {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document doc = null;
        try {
            InputStream inputStream = new ByteArrayInputStream(response.getBytes());
            doc = dBuilder.parse(inputStream);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Element element = doc.getDocumentElement();
        element.normalize();
        try {
            NodeList nList = doc.getElementsByTagName("TopSellers");
            if (nList == null || nList.getLength()<=0) {
                Log.d("Amazo TOp Seller", "NList null");
                noResultsText.setText("No Results Found");
            } else {

                NodeList childnode = nList.item(0).getChildNodes();
                product itemProduct = null;
                String requestURL = "";
                for (int i = 0; i < childnode.getLength(); i++) {
                    //itemProduct = new product();
                    String titleASIN = childnode.item(i).getChildNodes().item(0).getChildNodes().item(0).getNodeValue();
                    Log.d("ASIN", i + " :" + titleASIN);
                    requestURL = getURLUsingASIN(titleASIN);
                    urlArray.add(requestURL);
                }

            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }


    private String getURLUsingASIN(String titleASIN) {
        SignedRequestsHelper helper;

        try {
            helper = SignedRequestsHelper.getInstance(ENDPOINT, AWS_ACCESS_KEY_ID, AWS_SECRET_KEY);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        String requestUrl = "";

        Map<String, String> params = new HashMap<String, String>();

        params.put("Service", "AWSECommerceService");
        params.put("Operation", "ItemLookup");
        params.put("AWSAccessKeyId", "XXX");
        params.put("AssociateTag", "XXXX");
        params.put("ItemId", titleASIN);
        params.put("IdType", "ASIN");
        params.put("ResponseGroup", "BrowseNodes,Images,ItemAttributes,Offers");
        requestUrl = helper.sign(params);
        // parseUrl(requestUrl);
        return requestUrl;

    }

    private void parseUsingDOM(String response) throws IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document doc = null;
        try {
            InputStream inputStream = new ByteArrayInputStream(response.getBytes());
            doc = dBuilder.parse(inputStream);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Element element = doc.getDocumentElement();
        element.normalize();
        NodeList nList = doc.getElementsByTagName("Item");
        product itemProduct = null;
        for (int i = 0; i < nList.getLength(); i++) {
            itemProduct = new product();
            Node node = nList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element2 = (Element) node;
                String titleTofind = "title";
                String title = getTitleValue("ItemAttributes", element2, titleTofind);

                itemProduct.setTitle(title);
                titleTofind = "mrp";
                String mrp = getTitleValue("ItemAttributes", element2, titleTofind);
                String formattedMrp = "0";
                if (mrp.isEmpty() == false) {
                    int delim1 = mrp.indexOf("+");
                    if (mrp.length() > delim1 + 1)
                        formattedMrp = mrp.substring(delim1 + 1, mrp.length() - 1);
                    mrp = mrp.substring(0, delim1);
                    float fMrp = Float.parseFloat(mrp);
                    fMrp = fMrp / 100;
                    itemProduct.setWithoutUnitMrpPrice(fMrp);
                }
                String price = getTitleValue("OfferSummary", element2, "");
                String formattedPrice = "";
                if (price.isEmpty() == false) {
                    int delim2 = price.indexOf("+");
                    formattedPrice = price.substring(delim2 + 1, price.length() - 1);
                    price = price.substring(0, delim2);
                    float fPrice = Float.parseFloat(price);
                    fPrice = fPrice / 100;
                    itemProduct.setWithoutUnitPrice(fPrice);
                }
                String rupee;

                rupee = "₹";
                if (formattedPrice.isEmpty() == false)
                    formattedPrice = formattedPrice.replace("INR", rupee);
                itemProduct.setPrice(formattedPrice);
                if (formattedMrp.isEmpty() == false)
                    formattedMrp = formattedMrp.replace("INR", rupee);
                itemProduct.setMrp(formattedMrp);
                getImageValue("MediumImage", element2, itemProduct);
                getLargeImageValue("LargeImage", element2, itemProduct);
                // itemProduct.setImage(image);
                String url = getUrlASINValue("DetailPageURL", element2);
                itemProduct.setUrl(url);
                String asin = getUrlASINValue("ASIN", element2);
                String deliveryTime = getTitleValue("Offers", element2, "");
                itemProduct.setAsin(url);
                itemProduct.setSeller("Amazon");
                itemProduct.setDeliveryTime(" ");
                String categoryId = "";
                categoryId = getCategoryValue("BrowseNodes", element2, "");
                int delim = categoryId.indexOf("+");
                String categoryName = "";
                if (delim >= 0) {
                    categoryName = categoryId.substring(delim + 1, categoryId.length() - 1);
                    if (categoryId.isEmpty() == false)
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
                productList.add(itemProduct);
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

        } catch (Exception ex) {
            Log.d("Category Exception", ex.getMessage());
        }
        return category;
    }

    private String getUrlASINValue(String tag, Element element) {
        String url = "";
        try {
            NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
            url = nodeList.item(0).getNodeValue();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return url;
    }

    private void getImageValue(String tag, Element element, final product itemProduct) throws IOException {
        Bitmap bmp = null;
        try {
            NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
            String nodeName = nodeList.item(0).getNodeName();

            if (nodeName.equals("URL")) {
                String url = nodeList.item(0).getChildNodes().item(0).getNodeValue();
                final RequestQueue rq1 = Volley.newRequestQueue(this);
                int height = 300, width = 300;

                final ImageRequest ir;
                ir = new ImageRequest(url, new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        itemProduct.setImage(response);
                    }
                }, width, height, ImageView.ScaleType.CENTER, null, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        return;
                    }
                });
                rq1.add(ir);

            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }


        return;

    }
    private void getLargeImageValue(String tag, Element element, final product itemProduct) throws IOException {

        try {
            NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
            String nodeName = nodeList.item(0).getNodeName();

            if (nodeName.equals("URL")) {
                String url = nodeList.item(0).getChildNodes().item(0).getNodeValue();

                final RequestQueue rq1 = Volley.newRequestQueue(this);
                int height = 300, width = 300;

                final ImageRequest ir;
                ir = new ImageRequest(url, new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        itemProduct.setLargeImage(response);
                    }
                }, width, height, ImageView.ScaleType.CENTER, null, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        return;
                    }
                });
                rq1.add(ir);

            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }


        return;

    }


    private String getTitleValue(String tag, Element element, String titleTofind) {
        String title = "";
        int foundTitle=0;
        try {
            NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
            if (nodeList == null) {
                Log.d("Amaaon Fragment", "Not available" + tag);
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
                        String unitPrice = "", formattedPrice = "";
                        String title2 = node1.getChildNodes().item(0).getNodeName();
                        if (title2.equals("Amount")) {
                            unitPrice = node1.getChildNodes().item(0).getChildNodes().item(0).getNodeValue();

                        }
                        String title1 = node1.getChildNodes().item(2).getNodeName();
                        if (title1.equals("FormattedPrice")) {
                            formattedPrice = node1.getChildNodes().item(2).getChildNodes().item(0).getNodeValue();

                        }
                        title = unitPrice + "+" + formattedPrice;
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
            } else if (tag.equals("OfferSummary")) {
                for (int i = 0; i < nodeList.getLength(); i++) {
                    String node = nodeList.item(i).getNodeName();
                    Node node1 = nodeList.item(i);
                    if (node.equals("LowestNewPrice")) {

                        String unitPrice = "", formattedPrice = "";
                        String title2 = node1.getChildNodes().item(0).getNodeName();
                        if (title2.equals("Amount")) {
                            unitPrice = node1.getChildNodes().item(0).getChildNodes().item(0).getNodeValue();

                        }
                        String title1 = node1.getChildNodes().item(2).getNodeName();
                        if (title1.equals("FormattedPrice")) {
                            formattedPrice = node1.getChildNodes().item(2).getChildNodes().item(0).getNodeValue();

                        }
                        title = unitPrice + "+" + formattedPrice;

                    }
                }
            }
            else if(tag.equals("ItemLinks")){
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
        }else if (tag.equals("Offers")) {
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
        } catch (Exception ex) {
            Log.d("Amazon", ex.getMessage());
        }

        return title;
    }


}
