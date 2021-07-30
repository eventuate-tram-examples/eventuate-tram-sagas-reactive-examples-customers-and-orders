package io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain;


import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.api.messaging.common.OrderDetails;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.api.messaging.common.OrderState;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.api.messaging.common.RejectionReason;
import org.springframework.data.domain.Persistable;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name="orders")
@Access(AccessType.FIELD)
public class Order implements Persistable<Long> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  private OrderState state;

  private Long customerId;

  private BigDecimal orderTotal;

  @Enumerated(EnumType.STRING)
  private RejectionReason rejectionReason;

  @org.springframework.data.annotation.Transient
  private boolean newOrder = false;

  public Order() {
  }

  public Order(OrderDetails orderDetails) {
    this.customerId = orderDetails.getCustomerId();
    this.orderTotal = orderDetails.getOrderTotal().getAmount();
    this.state = OrderState.PENDING;
  }

  public static Order createOrder(OrderDetails orderDetails) {
    return new Order(orderDetails);
  }

  public Long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public Long getCustomerId() {
    return customerId;
  }

  public BigDecimal getOrderTotal() {
    return orderTotal;
  }

  public void approve() {
    this.state = OrderState.APPROVED;
  }

  public void reject(RejectionReason rejectionReason) {
    this.state = OrderState.REJECTED;
    this.rejectionReason = rejectionReason;
  }

  public OrderState getState() {
    return state;
  }

  public RejectionReason getRejectionReason() {
    return rejectionReason;
  }

  @Override
  public boolean isNew() {
    return newOrder;
  }
}
