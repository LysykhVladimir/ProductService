package com.appsdeveloperblog.estore.ProductService.query;

import com.appsdeveloperblog.estore.ProductService.core.data.ProductEntity;
import com.appsdeveloperblog.estore.ProductService.core.data.ProductsRepository;
import com.appsdeveloperblog.estore.ProductService.core.events.ProductCreatedEvent;
import com.appsdeveloperblogs.estore.core.events.ProductReservedEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.interceptors.ExceptionHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ProcessingGroup("product-group")
public class ProductEventsHandler {

    private final ProductsRepository productsRepository;

    @Autowired
    public ProductEventsHandler(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
    }

    @ExceptionHandler(resultType = Exception.class)
    public void handle(Exception exception) throws Exception {
        throw exception;
    }

    @ExceptionHandler(resultType = IllegalArgumentException.class)
    public void handle(IllegalArgumentException exception) {
        //Log error message
    }

    @EventHandler
    public void on(ProductCreatedEvent event) {
        ProductEntity productEntity = new ProductEntity();
        BeanUtils.copyProperties(event, productEntity);
        try {
            productsRepository.save(productEntity);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void on(ProductReservedEvent productReservedEvent) {
        ProductEntity productEntity = productsRepository.findByProductId(productReservedEvent.getProductId());
        productEntity.setQuantity(productEntity.getQuantity() - productReservedEvent.getQuantity());
        productsRepository.save(productEntity);
        log.info("ProductReservedEvent is called for productId:{} and orderId:{}",
                productReservedEvent.getProductId(), productReservedEvent.getOrderId());
    }
}
