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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EbayFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EbayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EbayFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private String mSearchItem="";
    private static final String EBAY_KEY = "XXXX";
    /*
     * Your AWS Secret Key corresponding to the above ID, as taken from the AWS
     * Your Account page.
     */


    /*
     * Use the end-point according to the region you are interested in.
     */
    //  private static final String FK_ENDPOINT = "https://affiliate-api.flipkart.net/";
    private static final String EBAY_SEARCHURL = "http://svcs.ebay.com/services/search/FindingService/v1?OPERATION-NAME=findItemsByKeywords&SERVICE-VERSION=1.12.0&GLOBAL-ID=EBAY-IN&affiliate.trackingId=122&affiliate.networkId=9&affiliate.customId=234&SECURITY-APPNAME="+EBAY_KEY+"&keywords=";
    private static final String EBAY_COUNTURL ="&RESPONSE-DATA-FORMAT=XML&REST-PAYLOAD&paginationInput.entriesPerPage=50&outputSelector(0)=shippingInfo";


    ArrayList<product> productList;
    ArrayList<String> categoryList;
    ArrayList<product> permanentproductList;
    private RecyclerView recyclerView;
    private ProductListAdapter mAdapter;
    TextView txtCnt;
    Button filterView;
    static boolean isFromNewInstance =false;
    public EbayFragment() {
        // Required empty public constructor
        productList = new ArrayList<>();
        categoryList = new ArrayList<>();
         permanentproductList = new ArrayList<>();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *

     * @return A new instance of fragment EbayFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EbayFragment newInstance(String searchItem) {
        EbayFragment fragment = new EbayFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, searchItem);
        isFromNewInstance = true;
        fragment.setArguments(args);
        return fragment;
    }
    static EbayFragment mFragment;
    public static EbayFragment getInstance(String newProduct) {
        if(mFragment==null) {
            EbayFragment fragment = new EbayFragment();
            Bundle args = new Bundle();
            args.putString(ARG_PARAM1, newProduct);
            fragment.setArguments(args);
            mFragment =fragment;
        }
        return mFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSearchItem = getArguments().getString(ARG_PARAM1);

        }
    }
    public void setFilterCount(String filterCount) {
        txtCnt.setText(filterCount + " products found");
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
                        setEbayFilterCount(filterCount);

                    }

                });

            }
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_ebay, container, false);

        view.setBackgroundColor(Color.parseColor("#CD1000"));
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView2);
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
        txtCnt = (TextView)view.findViewById(R.id.textCount);
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

    private void parentSearchFn() {
        try {
            searchFn();

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void searchFn(String s) throws ExecutionException, InterruptedException {

        mSearchItem = s;
        searchFn();

    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    public void setEbayFilterCount(String ebayFilterCount) {
        txtCnt.setText(ebayFilterCount + " products found");
    }


    public ArrayList<product> getProductList(){
        return productList;
    }
    public void searchFn() throws ExecutionException, InterruptedException {


        String searchStr = mSearchItem;
        searchStr= searchStr.replace(" ","%20");
        String finalUrl = EBAY_SEARCHURL + searchStr+EBAY_COUNTURL;
        HttpGetRequest getRequest = new HttpGetRequest();
        //Perform the doInBackground method, passing in our url
        getRequest.execute(finalUrl).get();
        productList = getRequest.getXml();

    }
    public ProductListAdapter getAdapter() {
        return mAdapter;
    }

    public class HttpGetRequest extends AsyncTask<String, Void, String> {

        ArrayList<product> tempproductList = new ArrayList<product>();
        String result;
        @Override
        protected String doInBackground(String... params) {
            String stringUrl = params[0];


            try{
                Log.d("EBAY",stringUrl);
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

            NodeList nList = doc.getElementsByTagName("item");
            product itemProduct =null;
            for (int i=0; i<nList.getLength(); i++) {
                itemProduct = new product();
                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    //  Element element2 = (Element) node;
                    String title = getTitleValue(node,"title");
                    String tempSeatchItem = mSearchItem;
                    if(tempSeatchItem.isEmpty() == false && tempSeatchItem.contains("\""))
                        tempSeatchItem.replaceAll("\""," ");
                    tempSeatchItem =tempSeatchItem.trim();
                    int firstSpace =tempSeatchItem.indexOf(" ");
                    String compulsaryWord=tempSeatchItem;
                    if(firstSpace >0)
                        compulsaryWord = compulsaryWord.substring(0,firstSpace);
                    if (title.toLowerCase().contains(compulsaryWord.toLowerCase()) == false)
                        continue;
                    itemProduct.setTitle(title);
                    String price =  getTitleValue( node,"sellingStatus");
                    float fMrpPrice = 0;
                    if(price.isEmpty()==false) {
                        fMrpPrice = Float.parseFloat(price);
                    }
                    itemProduct.setWithoutUnitPrice(fMrpPrice);
                    String rupee;
                    if(getContext()!=null)
                        rupee = getResources().getString(R.string.Rs);
                    else
                        rupee= "₹";
                    itemProduct.setPrice(rupee+price);
                    Bitmap image =  getImageValue(node,"galleryURL");
                    itemProduct.setImage(image);
                    Bitmap largeimage =  getImageValue(node,"galleryPlusPictureURL");
                    if(largeimage!=null)
                    itemProduct.setLargeImage(largeimage);
                    else
                        itemProduct.setLargeImage(image);
                    String url =  getTitleValue(node,"viewItemURL");
                    itemProduct.setUrl(url);
                    String feature =  "" ;
                    feature = "Feature: \n" + getTitleValue(node,"subtitle");
                    itemProduct.setFeature(feature);
                    String asin = getTitleValue(node,"itemId");
                    itemProduct.setAsin(asin);
                    itemProduct.setSeller("Ebay");
                    String serviceCost = getTitleValue(node,"shippingInfo");
                    itemProduct.setShipCharges(rupee+serviceCost);
                    String categoryId = getTitleValue(node,"primaryCategory");
                    int delim  = categoryId.indexOf("+");
                    String categoryName = categoryId.substring(delim+1,categoryId.length()-1);
                    categoryId = categoryId.substring(0,delim);

                    itemProduct.setCategoryId(categoryId);
                    itemProduct.setCategoryName(categoryName);
                    itemProduct.setDeliveryTime("");
                    tempproductList.add(itemProduct);
                }
            }
        }


        private Bitmap getImageValue(Node node,String findElement) throws IOException {

            String title="";
            Bitmap bmp = null;
            try {
                NodeList nodeList = null;
                Element element = (Element)node;
               NodeList tempNodeList= element.getElementsByTagName(findElement);
                if(tempNodeList==null)
                    return bmp;
                if(tempNodeList!=null && tempNodeList.getLength()>0) {
                    nodeList = tempNodeList.item(0).getChildNodes();
                    if (nodeList != null) {
                        String node1 = nodeList.item(0).getNodeValue();
                        title = node1;
                    }

                    URL imageurl = new URL(title);

                    BitmapFactory.Options o = new BitmapFactory.Options();
                    o.inJustDecodeBounds = true;
                    InputStream stream1 = imageurl.openConnection().getInputStream();
                    bmp = BitmapFactory.decodeStream(stream1,null,o);
                    stream1.close();
                    int width_tmp = o.outWidth, height_tmp = o.outHeight;
                    int scale = 1;
                    while (true) {
                        if (width_tmp /2 < 300
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
        private  String getTitleValue( Node node,String findElement) {

            String title = "";
            try{
            NodeList nodeList = null;
            if (findElement.equals("title"))
                title = node.getChildNodes().item(1).getChildNodes().item(0).getNodeValue();
            else if (findElement.equals("itemId")) {
                title = node.getChildNodes().item(0).getChildNodes().item(0).getNodeValue();
            } else if (findElement.equals("viewItemURL")) {
                Element element = (Element) node;
                nodeList = element.getElementsByTagName(findElement).item(0).getChildNodes();
                if (nodeList != null) {
                    String node1 = nodeList.item(0).getNodeValue();
                    title = node1;
                }
            } else if (findElement.equals("sellingStatus")) {
                Element element = (Element) node;
                nodeList = element.getElementsByTagName("sellingStatus").item(0).getChildNodes();
                String node1 = nodeList.item(0).getChildNodes().item(0).getNodeValue();
                return node1;
            } else if (findElement.equals("shippingInfo")) {
                Element element = (Element) node;
                nodeList = element.getElementsByTagName("shippingInfo").item(0).getChildNodes();
                String node1 = nodeList.item(0).getChildNodes().item(0).getNodeValue();
                return node1;
            } else if (findElement.equals("primaryCategory")) {
                Element element = (Element) node;
                nodeList = element.getElementsByTagName("primaryCategory").item(0).getChildNodes();
                String node1 = nodeList.item(0).getChildNodes().item(0).getNodeValue();
                String node2 = nodeList.item(1).getChildNodes().item(0).getNodeValue();
                node1 = node1 + "+" + node2;
                return node1;
            }
            else if(findElement.equals("subtitle")){
                Element element = (Element) node;
                nodeList = element.getElementsByTagName(findElement).item(0).getChildNodes();
                if (nodeList != null) {
                    String node1 = nodeList.item(0).getNodeValue();
                    title = node1;
                }
            }
        }catch(Exception ex){
                Log.d("Flipkart Exception",ex.getMessage());
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
