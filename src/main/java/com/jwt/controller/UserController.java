package com.jwt.controller;

import com.jwt.requestdto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.jwt.entity.User;
import com.jwt.repository.UserRepository;
import com.jwt.requestdto.RegisterRequest;
import com.jwt.service.UserDetailsServiceImpl;
import com.jwt.utility.JwtUtil;


@RestController
@Slf4j
public class UserController {
	
	    @Autowired
	    private AuthenticationManager authenticationManager;
	    @Autowired
	    private UserDetailsServiceImpl userDetailsService;
	    @Autowired
	    private UserRepository userRepository;
	    @Autowired
	    private JwtUtil jwtUtil;

	    @PostMapping("/register")
	    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
	        User user = new User();
	        user.setUsername(registerRequest.getUsername());
	        user.setPassword(new BCryptPasswordEncoder().encode(registerRequest.getPassword()));
	        //user.setRole("USER");
	        user.setRole(registerRequest.getRole());
	        userRepository.save(user);
	        return ResponseEntity.ok("User registered successfully");
	    }

	    @PostMapping("/login")
	    public ResponseEntity<?> authenticateUser(@RequestBody RegisterRequest loginRequest) throws Exception {
	        try {
	            authenticationManager.authenticate(
	                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
	        } catch (BadCredentialsException e) {
	            throw new Exception("Invalid credentials", e);
	        }

	        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
	        final String token = jwtUtil.generateToken(userDetails.getUsername());

	        return ResponseEntity.ok(token);
	    }

	    @GetMapping("/user/{username}")
		//@PreAuthorize( "hasRole('ADMIN')")
	    public ResponseEntity<?> getUserDetails(@PathVariable String username) {
	        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
			UserDTO userDTO = new UserDTO(user.getId(),user.getUsername());
	        return ResponseEntity.ok(userDTO);
	    }

		@DeleteMapping("/user/{password}")
		public String removeUserByPassword(@PathVariable String password) {
				try {
					userRepository.deleteByPassword(password);
					return "User deleted successfully";
				}catch (Exception e) {
					e.getMessage();
					return "User not deleted";
				}
		}
}
