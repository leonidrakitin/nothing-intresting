package ru.sushi.delivery.kds.domain.controller.dto.starter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class StarterOrderData {

    private long starterId;
    private String globalId;
    private List<StarterOrderItemData> orderItems;
    private double bonuses;
    private double price;
    private double discountPrice;
    private double deliveryPrice;
    private double changeFrom;
    private double totalPrice;
    private StarterAddressData address;
    private int flatwareAmount;
    private String deliveryType;
    private String paymentType;
    private String paymentStatus;
    private ZonedDateTime submittedDatetime;
    private ZonedDateTime deliveryDatetime;
    private int deliveryDuration;
    private long userId;
    private String username;
    private String userPhone;
    private String userLang;
    private String comment;
    private String status;
    private long shopId;
    private boolean notCall;
    private boolean isPreorder;
    private String source;
    private List<StarterDiscountData> discounts;
    private StarterDeliveryProductData deliveryProduct;
    private String timezone;
    private String terminalId;
}
