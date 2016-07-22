/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.espl.loader;

import com.salesforce.dataloader.process.ProcessConfig;
import com.sforce.soap.enterprise.QueryResult;
import com.sforce.soap.enterprise.sobject.SObject;
import com.sforce.ws.ConnectionException;
import java.io.IOException;
import java.util.Iterator;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import com.sforce.soap.enterprise.sobject.Candidate__c;

/**
 *
 * @author Siddharaj Atodaria
 */
public class ReportMetadata {

    private static String baseUri;
    private static Header oauthHeader;
    private static Header prettyPrintHeader = new BasicHeader("X-PrettyPrint", "1");
    public static XmlBeanFactory beanfactory;
    public static LoaderBean beanObj;

    public static void main(String args[]) throws ConnectionException {
        System.out.println("Configuration file is loaded successfully.");
        System.setProperty("salesforce.config.dir", "conf");
        beanfactory = ProcessConfig.getBeanFactory();
        beanObj = ((LoaderBean) beanfactory.getBean("hrprocBean"));

        oAuthURL();
        String ids = getReportsDetails().replaceAll("\"", "'");
        String attachIds = Attachments(ids.substring(0, ids.length() - 1));

       if (args[1].equalsIgnoreCase("attachment-yes")) {
            System.out.println("Attachment are ready for download process.");
            AttachmentDownload.readyForDownload(attachIds.substring(0, attachIds.length() - 1));
       }
        if (args[0].equalsIgnoreCase("mail-yes")) {
            sendMailTocandidate(ids.substring(0, ids.length() - 1));
        }

    }

    public static void oAuthURL() {
        HttpResponse response = SFConnector.getResponseConncetion();
        final int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
            System.out.println("Error authenticating to Force.com: " + statusCode);
            return;
        }

        String getResult = null;
        try {
            getResult = EntityUtils.toString(response.getEntity());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        JSONObject jsonObject = null;
        String loginAccessToken = null;
        String loginInstanceUrl = null;

        try {
            jsonObject = (JSONObject) new JSONTokener(getResult).nextValue();
            loginAccessToken = jsonObject.getString("access_token");
            loginInstanceUrl = jsonObject.getString("instance_url");
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }

        baseUri = loginInstanceUrl + beanObj.restendpoint + beanObj.apiversion;
        oauthHeader = new BasicHeader("Authorization", "OAuth " + loginAccessToken);
        System.out.println("SFDC oauthHeader : " + oauthHeader);
        //System.out.println("\n" + response.getStatusLine());
        System.out.println("SFDC Successful login");
        System.out.println("SFDC instance URL : " + loginInstanceUrl);
        System.out.println("SFDC access token/session Id: " + loginAccessToken);
        System.out.println("SFDC baseUri: " + baseUri);

    }

    public static String getReportsDetails() {

        String ids = "";
        try {

            HttpClient httpClient = HttpClientBuilder.create().build();
            String uri = baseUri + "/analytics/reports/" + beanObj.reportid;
            //String uri = baseUri + "/sobjects/";

            //System.out.println("baseUri : " + uri);
            HttpGet httpGet = new HttpGet(uri);
           // System.out.println("oauthHeader : " + oauthHeader);
            httpGet.addHeader(oauthHeader);
            httpGet.addHeader(prettyPrintHeader);
            
            
            
            

            HttpResponse response = httpClient.execute(httpGet);
            
            

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String response_string = EntityUtils.toString(response.getEntity());
                try {
                    JSONObject json = new JSONObject(response_string);
                    ids = getAttachementId(json.toString(1));
                } catch (JSONException je) {
                }
            } else {

                System.exit(-1);
            }

        } catch (IOException ioe) {
        } catch (NullPointerException npe) {
        }
        return ids;
    }

    

    public static String getAttachementId(String jsonData) {

        String report = "";
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readTree(jsonData);
            JsonNode msgNode = rootNode.path("factMap");
            Iterator<JsonNode> ite = msgNode.getElements();

            while (ite.hasNext()) {

                JsonNode jNodeStart = ite.next();
                for (JsonNode jNodeInner : jNodeStart) {
                    for (JsonNode jNodeInner1 : jNodeInner) {
                        for (JsonNode jNodeInner2 : jNodeInner1) {
                            int i = 0;
                            for (JsonNode jNodeInner3 : jNodeInner2) {
                                if (i == 1) {
                                    continue;
                                }
                                report += "" + jNodeInner3.get("value") + ",";
                                i++;
                            }
                        }
                    }
                }

            }

        } catch (JsonGenerationException e) {

            e.printStackTrace();

        } catch (JsonMappingException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }
        return report;
    }

    private static String Attachments(String Ids) {
        String returnIds = "";
        try {
            for (String recid : Ids.split(",")) {
                QueryResult queryResults = SFConnector.sfdcConncetion().query("Select Id, ParentId, Name, ContentType, Body "
                        + "From Attachment WHERE ParentId in (" + recid + ")");

                if (queryResults.getSize() > 0) {
                    for (SObject record : queryResults.getRecords()) {
                        returnIds += "" + record.getId() + ",";

                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnIds;

    }

    private static String sendMailTocandidate(String Ids) {
        String returnIds = "";
        SendMail mailObj = new SendMail();
        try {
            for (String recid : Ids.split(",")) {
                QueryResult queryResults = SFConnector.sfdcConncetion().query("Select Id, Applicant_Name__c, Email__c "
                        + "From Candidate__c WHERE id in (" + recid + ")");

                if (queryResults.getSize() > 0) {
                    for (SObject record : queryResults.getRecords()) {
                        Candidate__c cObj = (Candidate__c) record;
                        mailObj.sendMailService(cObj);
                        System.out.println("Mail Successfully Sent to "+cObj.getEmail__c()+".");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnIds;

    }

}
