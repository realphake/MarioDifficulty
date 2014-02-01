/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Onlinedata;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class MainSendRequest {

    public static String baseurl
            = "http://mario-uva2.rhcloud.com/uploads/";
    public static String upload = "";
    public static String uploadurl = "";
    public static String downloadurl = "";
    public String download = "";


    public void downloadData(String filename) {
        downloadurl = baseurl+filename;
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpget = new HttpGet(downloadurl);
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                response = httpclient.execute(httpget);
                HttpEntity serverEntity = response.getEntity();
                if (serverEntity != null) {
                    System.out.println("Found file at adress: "+downloadurl);
                    long len = serverEntity.getContentLength();
                    System.out.println("Length is "+len);
                    download = EntityUtils.toString(serverEntity);
                    System.out.println(download);
                }
            } catch (    IOException | ParseException ex) {
                Logger.getLogger(MainSendRequest.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                response.close();
            }
            httpclient.close();
        } catch (IOException ex) {
            Logger.getLogger(MainSendRequest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void uploadData(String filename, String toBeUploaded) {
        upload = filename + ";" + toBeUploaded;
        uploadurl = baseurl;
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            StringEntity cliententity = new StringEntity(upload,
                    ContentType.create("plain/text", Consts.UTF_8));
            HttpPost httppost = new HttpPost(uploadurl);
            httppost.setEntity(cliententity);
            CloseableHttpResponse response = httpclient.execute(httppost);
            try {
                HttpEntity serverentity = response.getEntity();
                if (serverentity != null) {
                    long len = serverentity.getContentLength();
                    if (len != -1 && len < 2048) {
                        System.out.println(EntityUtils.toString(serverentity));
                    } else {
                        // Stream content out
                    }
                }
            } catch (    IOException | ParseException ex) {
                Logger.getLogger(MainSendRequest.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                response.close();
            }
            httpclient.close();
        } catch (IOException ex) {
            Logger.getLogger(MainSendRequest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void main(String[] args) {
        MainSendRequest req = new MainSendRequest();
        //req.downloadData("trainingfile.arff");
        // use this to send specific data to the server
        
    }
}
