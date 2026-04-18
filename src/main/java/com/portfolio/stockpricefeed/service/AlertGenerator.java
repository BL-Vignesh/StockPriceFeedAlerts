package com.portfolio.stockpricefeed.service;

import com.portfolio.stockpricefeed.config.RabbitMQConfig;
import com.portfolio.stockpricefeed.dto.StockPriceAlert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlertGenerator {

    private static final Logger logger = LoggerFactory.getLogger(AlertGenerator.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void processAndSendAlert(StockPriceAlert alert) {
        logger.info("Generating alert for {} to RabbitMQ...", alert.getSymbol());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, alert);
        logger.info("Alert successfully dispatched to Queue for User: {}", alert.getUserId());
    }
}
