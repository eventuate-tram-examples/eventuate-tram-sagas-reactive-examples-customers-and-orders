package io.eventuate.examples.tram.sagas.ordersandcustomers.orders.service;

import io.eventuate.examples.tram.sagas.ordersandcustomers.commondomain.Money;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.api.messaging.common.OrderDetails;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.api.messaging.common.RejectionReason;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain.Order;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public class OrderService {

  @Autowired
  private OrderRepository orderRepository;

  public OrderService(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  public Mono<Order> createOrder(OrderDetails orderDetails) {
    if (!orderDetails.getOrderTotal().isGreaterThanOrEqual(new Money(new BigDecimal(1000000)))) {
      return orderRepository.save(Order.createOrder(orderDetails));
    } else return Mono.error(new RuntimeException("Order is too big"));
  }

  public Mono<Void> approveOrder(Long orderId) {
    return orderRepository
            .findById(orderId)
            .flatMap(o -> {
              o.approve();
              return orderRepository.save(o);
            })
            .then();
  }

  public Mono<Void> rejectOrder(Long orderId, RejectionReason rejectionReason) {
    return orderRepository
            .findById(orderId)
            .flatMap(o -> {
              o.reject(rejectionReason);
              return orderRepository.save(o);
            })
            .then();
  }
}
