package com.portfolio.stockpricefeed.service;

import com.portfolio.stockpricefeed.config.RabbitMQConfig;
import com.portfolio.stockpricefeed.dto.StockPriceAlert;
import com.portfolio.stockpricefeed.entities.Portfolio;
import com.portfolio.stockpricefeed.entities.User;
import com.portfolio.stockpricefeed.repository.PortFolioRepository;
import com.portfolio.stockpricefeed.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AlertConsumer {

    private static final Logger logger = LoggerFactory.getLogger(AlertConsumer.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PortFolioRepository portFolioRepository;

    @Autowired
    private UserRepository userRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void consumeAlert(StockPriceAlert alert) {
        logger.info("Received Alert from RabbitMQ Queue for Symbol: {}", alert.getSymbol());

        // 0. Fetch Actual User Email
        Optional<User> optUser = userRepository.findById(alert.getUserId());
        if (optUser.isEmpty()) {
            logger.warn("User ID {} not found. Aborting email dispatch.", alert.getUserId());
            return;
        }
        
        String userEmail = optUser.get().getEmail();

        // 1. Send Email Notification
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(userEmail); 
        message.setSubject("Stock Price Alert: " + alert.getSymbol());
        message.setText(alert.getMessage() + "\n\nCurrent Price: ₹" + alert.getCurrentPrice() + 
                        "\n" + alert.getAlertType() + " Limit: ₹" + alert.getLimitCrossed());
        
        // This will strictly execute and block database update if SMTP networking/auth fails.
        mailSender.send(message);
        logger.info("Email Alert Dispatched successfully to real inbox: {}", userEmail);

        // 2. Update Database flag to indicate Mail Sent Indicator ONLY if email sent completely.
        List<Portfolio> holdings = portFolioRepository.findByStockSymbol(alert.getSymbol());
        for (Portfolio p : holdings) {
            if (p.getUserId().equals(alert.getUserId())) {
                if ("UPPER".equals(alert.getAlertType())) {
                    p.setUpperAlertSent(true);
                } else if ("LOWER".equals(alert.getAlertType())) {
                    p.setLowerAlertSent(true);
                }
                portFolioRepository.save(p);
                break;
            }
        }
    }
}
