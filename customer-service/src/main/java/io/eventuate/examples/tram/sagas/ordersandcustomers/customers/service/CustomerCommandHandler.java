package io.eventuate.examples.tram.sagas.ordersandcustomers.customers.service;

import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.api.messaging.commands.ReserveCreditCommand;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.api.messaging.replies.CustomerCreditLimitExceeded;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.api.messaging.replies.CustomerCreditReserved;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.api.messaging.replies.CustomerNotFound;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain.CustomerCreditLimitExceededException;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain.CustomerNotFoundException;
import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandHandlers;
import io.eventuate.tram.sagas.participant.SagaCommandHandlersBuilder;
import io.eventuate.tram.sagas.reactive.participant.ReactiveSagaCommandHandlersBuilder;
import reactor.core.publisher.Mono;

import static io.eventuate.tram.reactive.commands.consumer.ReactiveCommandHandlerReplyBuilder.withFailure;
import static io.eventuate.tram.reactive.commands.consumer.ReactiveCommandHandlerReplyBuilder.withSuccess;

public class CustomerCommandHandler {

  private CustomerService customerService;

  public CustomerCommandHandler(CustomerService customerService) {
    this.customerService = customerService;
  }

  public ReactiveCommandHandlers commandHandlerDefinitions() {
    return ReactiveSagaCommandHandlersBuilder
            .fromChannel("customerService")
            .onMessage(ReserveCreditCommand.class, this::reserveCredit)
            .build();
  }

  public Mono<Message> reserveCredit(CommandMessage<ReserveCreditCommand> cm) {
    ReserveCreditCommand cmd = cm.getCommand();
    try {
      customerService.reserveCredit(cmd.getOrderId(), cmd.getCustomerId(), cmd.getOrderTotal());
      return withSuccess(new CustomerCreditReserved());
    } catch (CustomerNotFoundException e) {
      return withFailure(new CustomerNotFound());
    } catch (CustomerCreditLimitExceededException e) {
      return withFailure(new CustomerCreditLimitExceeded());
    }
  }

}
