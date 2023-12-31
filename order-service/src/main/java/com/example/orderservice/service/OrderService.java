package com.example.orderservice.service;

import com.example.orderservice.dto.InventoryResponse;
import com.example.orderservice.dto.OrderLineItemDto;
import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderLineItem;
import com.example.orderservice.repository.OrderRepository;
import jakarta.transaction.Transactional;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

  private final OrderRepository orderRepository;
  private final WebClient.Builder webClientBuilder;

  public void placeOrder(OrderRequest orderRequest) {
    Order order = new Order();
    order.setOrderNumber(UUID.randomUUID().toString());

    List<OrderLineItem> orderLineItems = orderRequest.getOrderLineItemDtoList().stream()
        .map(this::mapToObject).toList();
    order.setOrderLineItemList(orderLineItems);

    List<String> skuCodes = order.getOrderLineItemList()
        .stream()
        .map(OrderLineItem::getSkuCode)
        .toList();

//        call inventory service , and place order if product is in stock
    InventoryResponse[] inventoryResponsesArray = webClientBuilder.build().get()
        .uri("http://inventory-service/api/inventory" ,
            uriBuilder -> uriBuilder
                .queryParam("skuCode" , skuCodes)
                .build()
        )
        .retrieve()
        .bodyToMono(InventoryResponse[].class).block();

    boolean allProductsInStock = Arrays.stream(inventoryResponsesArray).allMatch(InventoryResponse::isInStock);

    if (allProductsInStock) {
      orderRepository.save(order);
    } else {
      throw new IllegalArgumentException("Product is not in stock , please try again later");
    }

  }

  private OrderLineItem mapToObject(OrderLineItemDto orderLineItemDto) {
    OrderLineItem orderLineItem = new OrderLineItem();
    orderLineItem.setPrice(orderLineItemDto.getPrice());
    orderLineItem.setQuantity(orderLineItemDto.getQuantity());
    orderLineItem.setSkuCode(orderLineItemDto.getSkuCode());
    return orderLineItem;
  }

  public OrderResponse getOrders() {
    return null;
  }
}
