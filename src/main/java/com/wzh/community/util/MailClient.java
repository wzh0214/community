package com.wzh.community.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * @author wzh
 * @data 2022/7/30 -19:51
 */
@Component
public class MailClient {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from; // 发送人
    // to:发给谁，subject：邮件主题，context:邮件内容
    public void sendMail(String to, String subject, String content) {
        try {
            // 创建的是空的，需要MimeMessageHelper来构建更详细的内容
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true); // true，支持html
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            e.getMessage();
        }


    }
}
