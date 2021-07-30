package io.eventuate.examples.tram.sagas.ordersandcustomers.orders.service;

import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.api.messaging.common.OrderDetails;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain.Order;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain.OrderRepository;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.api.messaging.sagas.createorder.CreateOrderSagaData;
import io.eventuate.tram.sagas.reactive.orchestration.ReactiveSagaInstanceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

public class OrderSagaService {

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private ReactiveSagaInstanceFactory sagaInstanceFactory;

  @Autowired
  private CreateOrderSaga createOrderSaga;

  @Autowired
  private TransactionalOperator transactionalOperator;

  public OrderSagaService(OrderRepository orderRepository,
                          ReactiveSagaInstanceFactory sagaInstanceFactory,
                          CreateOrderSaga createOrderSaga) {
    this.orderRepository = orderRepository;
    this.sagaInstanceFactory = sagaInstanceFactory;
    this.createOrderSaga = createOrderSaga;
  }

  public Mono<Order> createOrder(OrderDetails orderDetails) {
    CreateOrderSagaData data = new CreateOrderSagaData(orderDetails);
    return sagaInstanceFactory
            .create(createOrderSaga, data)
            .then(orderRepository.findById(data.getOrderId()))
            .as(transactionalOperator::transactional);
  }
}
