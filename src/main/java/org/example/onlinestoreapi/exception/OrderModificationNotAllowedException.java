package org.example.onlinestoreapi.exception;

import org.example.onlinestoreapi.ENUM.OrderStatus;

public class OrderModificationNotAllowedException extends RuntimeException {
  public OrderModificationNotAllowedException(Long id , OrderStatus status) {
    super("The order with te id of: " +  id + "Is already " + status);
  }
}
