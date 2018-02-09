package com.vidhyalearning.ezeepricecompare;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by user on 03-07-2017.
 */

public class product implements Serializable
{
    Bitmap image,largeImage;
    String title,seller,reviewUrl;
    String listPrice,deliveryTime="NA",shipCharges="NA",categoryId,categoryName;
String url,asin,mrp="NA",feature="NA";
float withoutUnitMrpPrice,withoutUnitPrice;
    public void setTitle(String title) {
        this.title = title;
    }

    public void setPrice(String price) {
        this.listPrice = price;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public String getPrice() {
        return listPrice;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setAsin(String asin) {
        this.asin = asin;
    }

    public String getUrl() {
        return url;
    }

    public void setMrp(String mrp) {
        this.mrp = mrp;
    }

    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public void setShipCharges(String shipCharges) {
        this.shipCharges = shipCharges;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getMrp() {
        return mrp;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }
    public float getAvailablePrice() {
       float floatPrice;
        if(this.getPrice().isEmpty()==false)
            floatPrice = withoutUnitPrice;
        else if(this.getMrp().isEmpty()==false)
            floatPrice = withoutUnitMrpPrice;
        else
            floatPrice = 0;

        return floatPrice;
    }
    public void setWithoutUnitPrice(float withoutUnitPrice) {
        this.withoutUnitPrice = withoutUnitPrice;
    }

    public void setWithoutUnitMrpPrice(float withoutUnitMrpPrice) {
        this.withoutUnitMrpPrice = withoutUnitMrpPrice;
    }
    public String getSeller() {
        return seller;
    }
    /*Comparator for sorting the list by roll no*/
    public static Comparator<product> priceCompare = new Comparator<product>() {

        public int compare(product s1, product s2) {

            float availPrice1 = s1.getAvailablePrice();
            float availPrice2 = s2.getAvailablePrice();
            int intPrice = (int) (availPrice1-availPrice2);
	   /*For ascending order*/
            return intPrice;

	   /*For descending order*/
            //rollno2-rollno1;
        }};
    public static Comparator<product> priceCompareDesc = new Comparator<product>() {

        public int compare(product s1, product s2) {

            float availPrice1 = s1.getAvailablePrice();
            float availPrice2 = s2.getAvailablePrice();
            int intPrice = (int) (availPrice2-availPrice1);
	   /*For ascending order*/
            return intPrice;

	   /*For descending order*/
            //rollno2-rollno1;
        }};


    public void setLargeImage(Bitmap largeImage) {
        this.largeImage = largeImage;
    }

    public Bitmap getLargeImage() {
        return largeImage;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public String getFeature() {
        return feature;
    }

    public void setReviewUrl(String reviewUrl) {
        this.reviewUrl = reviewUrl;
    }

    public String getReviewUrl() {
        return reviewUrl;
    }
}
