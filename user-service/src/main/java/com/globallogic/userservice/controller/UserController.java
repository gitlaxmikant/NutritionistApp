package com.globallogic.userservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.globallogic.userservice.model.User;
import com.globallogic.userservice.service.JwtTokenUtil;
import com.globallogic.userservice.service.UserService;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("api/v1")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@PostMapping(value = "/register")
	public ResponseEntity<EntityModel<?>> registerUser(@RequestBody User user) {
		User newUser = userService.saveUser(user);
		EntityModel<User> model = EntityModel.of(newUser);
		WebMvcLinkBuilder linkToUsers = linkTo(methodOn(this.getClass()).loginUser(user));
		model.add(linkToUsers.withRel("login"));
		return new ResponseEntity<>(model, HttpStatus.CREATED);

	}

	@PostMapping("/login")
	public ResponseEntity<?> loginUser(@RequestBody User user) {
		try {
			if (user == null || user.getUserName() == null || user.getPassword() == null)
				throw new RuntimeException("Username or Password can not be empty");
		} catch (RuntimeException e) {
			return new ResponseEntity<String>("Message" + e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		String userName = user.getUserName();
		String password = user.getPassword();
		User reqUser = userService.findByUserNameAndPassword(userName, password);
		if (reqUser == null)
			return new ResponseEntity<>("Sorry, " + userName + "'s profile does not exist. First Register to login.",
					HttpStatus.NOT_FOUND);
		String token = jwtTokenUtil.generateToken(reqUser);
		return new ResponseEntity<>(token, HttpStatus.OK);
	}

	@GetMapping("/logout")
	public ResponseEntity<?> logout() {
		return new ResponseEntity<>("User logged out successfully", HttpStatus.OK);
	}

	@PutMapping(value = "/users")
	public ResponseEntity<?> updateUser(@RequestBody User user) {
		User updatedUser = userService.updateUser(user);
		if (updatedUser == null)
			return new ResponseEntity<>("User with ID : " + user.getUserId() + " is not found", HttpStatus.NOT_FOUND);
		return new ResponseEntity<>(updatedUser, HttpStatus.OK);
	}
}
