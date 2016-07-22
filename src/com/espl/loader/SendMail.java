/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.espl.loader;

import com.salesforce.dataloader.process.ProcessConfig;
import com.sforce.soap.enterprise.sobject.Candidate__c;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.springframework.beans.factory.xml.XmlBeanFactory;

/**
 *
 * @author Siddharaj Atodaria
 */
public class SendMail {

    static public Session session;
    public static XmlBeanFactory beanfactory;
    public static MailBean beanObj;

    SendMail() {
        System.setProperty("salesforce.config.dir", "conf");
        beanfactory = ProcessConfig.getBeanFactory();
        beanObj = ((MailBean) beanfactory.getBean("mailBean"));

        Properties props = new Properties();
        props.put("mail.smtp.auth", beanObj.auth);
        props.put("mail.smtp.starttls.enable", beanObj.starttls);
        props.put("mail.smtp.host", beanObj.host);
        props.put("mail.smtp.port", beanObj.port);

        session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(beanObj.username, beanObj.password);
                    }
                });

    }

    
    public void sendMailService(Candidate__c cobj) {

        try {
            StringBuilder body = new StringBuilder();
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(beanObj.username));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(cobj.getEmail__c()));
                    
            message.setSubject(beanObj.subject);
            
            body.append("Dear ");
            body.append(cobj.getApplicant_Name__c()+",");
            body.append("\n\n");
            body.append("\t"+beanObj.body);
            body.append("\n\nVenue: 701 NSGIT Park,Above Croma");
            body.append("\n\t    Near Sarjaa Restaurant,");
            body.append("\n\t    Aundh,Pune, Maharashtra- 411007");
            body.append("\n\nJob Location: Pune");
            body.append("\n\nYou are requested to carry following:-");
            body.append("\n1. CV/updated resume.");
            body.append("\n2. Print out of this mail.");
            body.append("\n2. Print out of this mail.");
            body.append("\n\n"+beanObj.skill);
            body.append("\n\nRegards,");
            body.append("\n"+beanObj.regards);
            message.setText(body.toString());
            //message.setText(body.toString());
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

}
