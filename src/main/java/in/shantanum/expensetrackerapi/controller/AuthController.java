package in.shantanum.expensetrackerapi.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.shantanum.expensetrackerapi.entity.AuthModel;
import in.shantanum.expensetrackerapi.entity.JwtResponse;
import in.shantanum.expensetrackerapi.entity.PasswordDto;
import in.shantanum.expensetrackerapi.entity.User;
import in.shantanum.expensetrackerapi.entity.UserModel;
import in.shantanum.expensetrackerapi.security.CustomUserDetailsService;
import in.shantanum.expensetrackerapi.service.UserService;
import in.shantanum.expensetrackerapi.util.JwtTokenUtil;

@RestController
public class AuthController {
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private CustomUserDetailsService userDetailsService;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;
	
	@PostMapping("/login")
	public ResponseEntity<JwtResponse> login(@RequestBody AuthModel authModel) throws Exception {
		System.out.println(authModel);
		authenticate(authModel.getEmail(), authModel.getPassword());
		
		//we need to generate the jwt token
		final UserDetails userDetails = userDetailsService.loadUserByUsername(authModel.getEmail());
		
		final String token = jwtTokenUtil.generateToken(userDetails);
		
		return new ResponseEntity<JwtResponse>(new JwtResponse(token), HttpStatus.OK);
	}
	
	public void authenticate(String email, String password) throws Exception {
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
		} catch (DisabledException e) {
			throw new Exception("User disabled");
		} catch (BadCredentialsException e) {
			throw new Exception("Bad Credentials");
		}
	}

	@PostMapping("/register")
	public ResponseEntity<User> save(@Valid @RequestBody UserModel user, HttpServletRequest request) {
		return new ResponseEntity<User>(userService.createUser(user, getAppUrl(request)), HttpStatus.CREATED);
	}

	@GetMapping("/verify-account")
	  public ResponseEntity<String> verifyAccount(@RequestParam("email") String email,
	      @RequestParam("token") String token) {
	    return new ResponseEntity<>(userService.verifyAccount(email, token), HttpStatus.OK);
	  }
	
	@PutMapping("/regenerate-otp")
	  public ResponseEntity<String> regenerateOtp(@RequestParam("email") String email, HttpServletRequest request) {
	    return new ResponseEntity<>(userService.regenerateOtp(email, getAppUrl(request)), HttpStatus.OK);
	  }
	
	@PutMapping("/forgot-password")
	public ResponseEntity<String> processForgotPassword(HttpServletRequest request, @RequestParam String email) {
		return new ResponseEntity<>(userService.generateForgotPasswordToken(email, getAppUrl(request)), HttpStatus.OK);
	}
	
	@PostMapping("/reset-password/token-validate")
	public ResponseEntity<String> processResetPassword(@RequestBody @Valid PasswordDto passwordDto) {
		return new ResponseEntity<>(userService.validatePasswordResetToken(passwordDto), HttpStatus.OK);
	}

	
	public String getAppUrl(HttpServletRequest request) {
		String url = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
		return url;
	}
}


















