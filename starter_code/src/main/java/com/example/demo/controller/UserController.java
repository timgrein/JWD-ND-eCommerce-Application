package com.example.demo.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Cart;
import com.example.demo.entity.User;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.controller.requests.CreateUserRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@RestController
@RequestMapping("/api/user")
public class UserController {

	private static final Logger LOG = LoggerFactory.getLogger(UserController.class.getSimpleName());

	private static final String USER_CREATION_FAILED = "User (name: {}) creation failed due to insufficient password length or wrong password confirmation";
	private static final String USER_CREATION_SUCCESSFUL = "User (name: {}) creation successful";

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@GetMapping("/id/{id}")
	public ResponseEntity<User> findById(@PathVariable Long id) {
		return ResponseEntity.of(userRepository.findById(id));
	}
	
	@GetMapping("/{username}")
	public ResponseEntity<User> findByUserName(@PathVariable String username) {
		User user = userRepository.findByUsername(username);
		return user == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(user);
	}
	
	@PostMapping("/create")
	public ResponseEntity<User> createUser(@RequestBody CreateUserRequest createUserRequest) {
		User user = new User();
		user.setUsername(createUserRequest.getUsername());
		Cart cart = new Cart();
		cartRepository.save(cart);
		user.setCart(cart);

		boolean userCreationRequestNotMeetingRequirements = createUserRequest.getPassword().length() < 7 ||
				!createUserRequest.getPassword().equals(createUserRequest.getConfirmPassword());

		if(userCreationRequestNotMeetingRequirements){
			LOG.warn(USER_CREATION_FAILED, createUserRequest.getUsername());

			return ResponseEntity.badRequest().build();
		}
		user.setPassword(bCryptPasswordEncoder.encode(createUserRequest.getPassword()));
		userRepository.save(user);

		LOG.info(USER_CREATION_SUCCESSFUL, createUserRequest.getUsername());

		return ResponseEntity.ok(user);
	}
	
}
