package io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain;


import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.api.messaging.common.OrderDetails;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.api.messaging.common.OrderState;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.api.messaging.common.RejectionReason;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("orders")
public class Order implements Persistable<Long> {

  @Id
  private Long id;

  private String state;

  private Long customerId;

  private BigDecimal orderTotal;

  private String rejectionReason;

  @org.springframework.data.annotation.Transient
  private boolean newOrder = false;

  public Order() {
  }

  public Order(OrderDetails orderDetails) {
    this.customerId = orderDetails.getCustomerId();
    this.orderTotal = orderDetails.getOrderTotal().getAmount();
    this.state = OrderState.PENDING.name();
  }

  public static Order createOrder(OrderDetails orderDetails) {
    Order o = new Order(orderDetails);
    o.newOrder = true;
    return o;
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
    this.state = OrderState.APPROVED.name();
  }

  public void reject(RejectionReason rejectionReason) {
    this.state = OrderState.REJECTED.name();
    this.rejectionReason = rejectionReason.name();
  }

  public String getState() {
    return state;
  }

  public String getRejectionReason() {
    return rejectionReason;
  }

  @Override
  public boolean isNew() {
    return newOrder;
  }
}
