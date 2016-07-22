/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.espl.loader;

import com.salesforce.dataloader.process.ProcessConfig;
import com.sforce.soap.enterprise.Connector;
import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.xml.XmlBeanFactory;

/**
 *
 * @author Siddharaj Atodaria
 */
public class SFConnector {

    public static String recordIds;
    static EnterpriseConnection connection;
    public static XmlBeanFactory beanfactory;
    public static LoaderBean beanObj;

    public static EnterpriseConnection sfdcConncetion() {
        System.setProperty("salesforce.config.dir", "conf");
        beanfactory = ProcessConfig.getBeanFactory();
        beanObj = ((LoaderBean) beanfactory.getBean("hrprocBean"));

        ConnectorConfig config = new ConnectorConfig();
        config.setUsername(beanObj.username);
        config.setPassword(beanObj.password);
        try {
            connection = Connector.newConnection(config);

        } catch (ConnectionException e1) {
            e1.printStackTrace();
        }
       
        return connection;
    }

    public static HttpResponse getResponseConncetion() {
        beanfactory = ProcessConfig.getBeanFactory();
        beanObj = ((LoaderBean) beanfactory.getBean("hrprocBean"));
        HttpClient httpclient = HttpClientBuilder.create().build();
        String loginURL = beanObj.loginurl
                + beanObj.grantservice
                + "&client_id=" + beanObj.clientid
                + "&client_secret=" + beanObj.clientsecret
                + "&username=" + beanObj.username
                + "&password=" + beanObj.password;

        HttpPost httpPost = new HttpPost(loginURL);
        HttpResponse response = null;

        try {
            response = httpclient.execute(httpPost);
        } catch (ClientProtocolException cpException) {
            cpException.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return response;
    }

}
