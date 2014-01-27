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

    static String uploadurl
            = "http://mariodb-uvaproject.rhcloud.com/uploads/";
    static String downloadurl
            = "http://mariodb-uvaproject.rhcloud.com/uploads/trainingfile.arff";
    static String message = "change this";

    public String download;
    public String upload;

    public void downloadData() {
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            //StringEntity clientEntity = new StringEntity(message,
            //        ContentType.create("plain/text", Consts.UTF_8));

            HttpGet httpget = new HttpGet(downloadurl);
            //httppost.setEntity(clientEntity);

            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                response = httpclient.execute(httpget);
                HttpEntity serverEntity = response.getEntity();
                if (serverEntity != null) {
                    long len = serverEntity.getContentLength();
                    if (len != -1 && len < 2048) {
                        download = EntityUtils.toString(serverEntity);
                        System.out.println(download);
                    } else {
                        // Stream content out
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(MainSendRequest.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParseException ex) {
                Logger.getLogger(MainSendRequest.class.getName()).log(Level.SEVERE, null, ex);
            } finally {

                response.close();
            }
            httpclient.close();
        } catch (IOException ex) {
            Logger.getLogger(MainSendRequest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void uploadData() {
        message = upload;
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            //Instantiate an HttpClient
            StringEntity cliententity = new StringEntity(upload,
                    ContentType.create("plain/text", Consts.UTF_8));

            HttpPost httppost = new HttpPost(uploadurl);
            httppost.setEntity(cliententity);

            CloseableHttpResponse response = httpclient.execute(httppost);
            try {
                response = httpclient.execute(httppost);
                HttpEntity serverentity = response.getEntity();
                if (serverentity != null) {
                    long len = serverentity.getContentLength();
                    if (len != -1 && len < 2048) {
                        System.out.println(EntityUtils.toString(serverentity));
                    } else {
                        // Stream content out
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(MainSendRequest.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParseException ex) {
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
        req.upload = "see if this is uploaded, 1, 2, 3,4 ,5, 67";
        req.uploadData();
        req.downloadData();
    }
}
