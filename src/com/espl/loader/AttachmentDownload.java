/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.espl.loader;

import com.salesforce.dataloader.process.ProcessConfig;
import com.sforce.soap.enterprise.QueryResult;
import com.sforce.soap.enterprise.sobject.Attachment;
import com.sforce.soap.enterprise.sobject.SObject;
import com.sforce.ws.ConnectionException;
import java.io.FileOutputStream;
import java.io.IOException;
import org.springframework.beans.factory.xml.XmlBeanFactory;

/**
 *
 * @author Siddharaj Atodaria
 */
public class AttachmentDownload {

    public static XmlBeanFactory beanfactory;
    public static LoaderBean beanObj;

    public static void readyForDownload(String recordId) throws ConnectionException {
        System.setProperty("salesforce.config.dir", "conf");
        beanfactory = ProcessConfig.getBeanFactory();
        beanObj = ((LoaderBean) beanfactory.getBean("hrprocBean"));
        for (String attachId : recordId.split(",")) {

            QueryResult queryResults = SFConnector.sfdcConncetion().query("Select Id, ParentId, Name, ContentType, Body "
                    + "From Attachment WHERE id in('" + attachId + "')");

            if (queryResults.getSize() > 0) {
                for (SObject record : queryResults.getRecords()) {
                    Attachment a = (Attachment) record;
                    writeOnDisk(beanObj.downloadpath, a.getName(), a.getBody());
                    System.out.println( a.getName()+" is downloaded");
                }
            }
        }

    }

    private static void writeOnDisk(String path, String fileName, byte[] bdy) {
        try {
            String filePath = path + fileName;
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(bdy);
            fos.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

}
