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

    return customerService
            .reserveCredit(cmd.getOrderId(), cmd.getCustomerId(), cmd.getOrderTotal())
            .flatMap(m -> withSuccess(new CustomerCreditReserved()))
            .onErrorResume(CustomerNotFoundException.class, e -> withFailure(new CustomerNotFound()))
            .onErrorResume(CustomerCreditLimitExceededException.class, e -> withFailure(new CustomerCreditLimitExceeded()));
  }

}
