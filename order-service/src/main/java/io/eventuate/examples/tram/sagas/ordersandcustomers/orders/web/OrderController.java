package io.eventuate.examples.tram.sagas.ordersandcustomers.orders.web;

import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.api.messaging.common.OrderDetails;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain.OrderRepository;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.service.OrderSagaService;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.api.web.CreateOrderRequest;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.api.web.CreateOrderResponse;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.api.web.GetOrderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class OrderController {

  private OrderSagaService orderSagaService;
  private OrderRepository orderRepository;

  @Autowired
  public OrderController(OrderSagaService orderSagaService, OrderRepository orderRepository) {
    this.orderSagaService = orderSagaService;
    this.orderRepository = orderRepository;
  }

  @RequestMapping(value = "/orders", method = RequestMethod.POST)
  public Mono<CreateOrderResponse> createOrder(@RequestBody CreateOrderRequest createOrderRequest) {
    return orderSagaService
            .createOrder(new OrderDetails(createOrderRequest.getCustomerId(), createOrderRequest.getOrderTotal()))
            .map(order -> new CreateOrderResponse(order.getId()));
  }

  @RequestMapping(value="/orders/{orderId}", method= RequestMethod.GET)
  public Mono<ResponseEntity<GetOrderResponse>> getOrder(@PathVariable Long orderId) {
    return orderRepository
            .findById(orderId)
            .map(o -> new ResponseEntity<GetOrderResponse>(new GetOrderResponse(o.getId(), o.getState(), o.getRejectionReason()), HttpStatus.OK))
            .switchIfEmpty(Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
  }

  @RequestMapping(value="/orders/customer/{customerId}", method= RequestMethod.GET)
  public Flux<GetOrderResponse> getOrdersByCustomerId(@PathVariable Long customerId) {
    return orderRepository
            .findAllByCustomerId(customerId)
            .map(o -> new GetOrderResponse(o.getId(), o.getState(), o.getRejectionReason()));
  }
}
