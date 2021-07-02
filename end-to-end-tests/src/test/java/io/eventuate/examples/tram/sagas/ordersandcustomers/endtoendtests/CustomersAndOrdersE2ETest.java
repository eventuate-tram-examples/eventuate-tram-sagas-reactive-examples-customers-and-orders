package io.eventuate.examples.tram.sagas.ordersandcustomers.endtoendtests;

import io.eventuate.examples.tram.sagas.ordersandcustomers.commondomain.Money;
import io.eventuate.examples.tram.sagas.ordersandcustomers.apigateway.api.web.GetCustomerHistoryResponse;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.api.web.CreateCustomerRequest;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.api.web.CreateCustomerResponse;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.api.messaging.common.OrderState;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.api.messaging.common.RejectionReason;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.api.web.CreateOrderRequest;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.api.web.CreateOrderResponse;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.api.web.GetOrderResponse;
import io.eventuate.util.test.async.Eventually;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CustomersAndOrdersE2ETestConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class CustomersAndOrdersE2ETest {

  private static final String CUSTOMER_NAME = "John";

  @Value("${host.name}")
  private String hostName;

  private String baseUrl(String path, String... pathElements) {
    assertNotNull("host", hostName);

    StringBuilder sb = new StringBuilder("http://");
    sb.append(hostName);
    sb.append(":");
    sb.append(8083);
    sb.append("/");
    sb.append(path);

    for (String pe : pathElements) {
      sb.append("/");
      sb.append(pe);
    }
    String s = sb.toString();
    return s;
  }

  @Autowired
  RestTemplate restTemplate;

  @Test
  public void shouldApprove() {
    CreateCustomerResponse createCustomerResponse = restTemplate.postForObject(baseUrl("customers"),
            new CreateCustomerRequest(CUSTOMER_NAME, new Money("15.00")), CreateCustomerResponse.class);

    CreateOrderResponse createOrderResponse = restTemplate.postForObject(baseUrl("orders"),
            new CreateOrderRequest(createCustomerResponse.getCustomerId(), new Money("12.34")), CreateOrderResponse.class);

    assertOrderState(createOrderResponse.getOrderId(), OrderState.APPROVED, null);
  }

  private void assertOrderState(Long id, OrderState expectedState, RejectionReason expectedRejectionReason) {
    Eventually.eventually(60, 500, TimeUnit.MILLISECONDS, () -> {
      ResponseEntity<GetOrderResponse> getOrderResponseEntity = restTemplate.getForEntity(baseUrl("orders/" + id), GetOrderResponse.class);
      assertEquals(HttpStatus.OK, getOrderResponseEntity.getStatusCode());
      GetOrderResponse order = getOrderResponseEntity.getBody();
      assertEquals(expectedState, order.getOrderState());
      assertEquals(expectedRejectionReason, order.getRejectionReason());
    });
  }

  @Test(expected = HttpClientErrorException.NotFound.class)
  public void shouldHandleOrderHistoryQueryForUnknownCustomer() {
    restTemplate.getForEntity(baseUrl("customers", Long.toString(System.currentTimeMillis()), "orderhistory"),
            GetCustomerHistoryResponse.class);
  }
}