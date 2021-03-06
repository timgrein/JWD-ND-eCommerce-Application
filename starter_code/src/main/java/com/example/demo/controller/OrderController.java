package com.example.demo.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.User;
import com.example.demo.entity.UserOrder;
import com.example.demo.repository.UserOrderRepository;
import com.example.demo.repository.UserRepository;

@RestController
@RequestMapping("/api/order")
public class OrderController {

	private static final Logger LOG = LoggerFactory.getLogger(OrderController.class.getSimpleName());

	private static final String USER_NOT_FOUND = "User with username {} was not found.";
	private static final String PLACED_ORDER = "Placed order for user {}";

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private UserOrderRepository userOrderRepository;

	@PostMapping("/submit/{username}")
	public ResponseEntity<UserOrder> submit(@PathVariable String username) {
		User user = userRepository.findByUsername(username);
		if(user == null) {
			LOG.warn(USER_NOT_FOUND, username);

			return ResponseEntity.notFound().build();
		}
		UserOrder order = UserOrder.createFromCart(user.getCart());
		userOrderRepository.save(order);

		LOG.info(PLACED_ORDER, username);

		return ResponseEntity.ok(order);
	}

	@GetMapping("/history/{username}")
	public ResponseEntity<List<UserOrder>> getOrdersForUser(@PathVariable String username) {
		User user = userRepository.findByUsername(username);

		if(user == null) {
			LOG.warn(USER_NOT_FOUND, username);

			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok(userOrderRepository.findByUser(user));
	}
}
