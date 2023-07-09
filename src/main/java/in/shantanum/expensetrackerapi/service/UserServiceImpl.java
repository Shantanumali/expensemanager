package in.shantanum.expensetrackerapi.service;

import java.time.Duration;
import java.time.LocalDateTime;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import in.shantanum.expensetrackerapi.entity.User;
import in.shantanum.expensetrackerapi.entity.UserModel;
import in.shantanum.expensetrackerapi.exceptions.ItemExistsException;
import in.shantanum.expensetrackerapi.exceptions.ResourceNotFoundException;
import in.shantanum.expensetrackerapi.repository.UserRepository;
import in.shantanum.expensetrackerapi.util.EmailUtil;
import net.bytebuddy.utility.RandomString;

@Service
public class UserServiceImpl implements UserService {
	
	@Autowired
	private PasswordEncoder bcryptEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private EmailUtil emailUtil;
	
	@Override
	@Transactional
	public User createUser(UserModel user, String contextPath) {
		if (userRepository.existsByEmail(user.getEmail())) {
			throw new ItemExistsException("User is already register with email:"+user.getEmail());
		}
		
		String token = RandomString.make(30);
	    try {
	      //emailUtil.sendVeifyTokenEmail(token, user);
	      emailUtil.sendOtpEmail(user.getEmail(), token, contextPath);
	    } catch (MessagingException e) {
	      throw new RuntimeException("Unable to send otp please try again");
	    }
		
		User newUser = new User();
		newUser.setEmail(user.getEmail());
		newUser.setEnabled(false);
		newUser.setName(user.getName());
		newUser.setRoles(user.getRoles());
		newUser.setVerificationTokenTime(LocalDateTime.now());
		newUser.setVerificationToken(token);
		//BeanUtils.copyProperties(user, newUser);
		newUser.setPassword(bcryptEncoder.encode(user.getPassword()));
		return userRepository.save(newUser);
	}
	
@Override
@Transactional
	  public String verifyAccount(String email, String otp) {
		    User user = userRepository.findByEmail(email)
		        .orElseThrow(() -> new RuntimeException("User not found with this email: " + email));
		    if (user.getVerificationToken().equals(otp) && Duration.between(user.getVerificationTokenTime(),
		        LocalDateTime.now()).getSeconds() < (1 * 60)) {
		      user.setEnabled(true);
		      userRepository.save(user);
		      return "OTP verified you can login";
		    }
		    return "Please regenerate otp and try again";
		  }

	@Override
	public User readUser() {
		Long userId = getLoggedInUser().getId();
		return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found for the id:"+userId));
	}

	@Override
	public User updateUser(UserModel user) {
		User existingUser = readUser();
		existingUser.setName(user.getName() != null ? user.getName() : existingUser.getName());
		existingUser.setEmail(user.getEmail() != null ? user.getEmail() : existingUser.getEmail());
		existingUser.setPassword(user.getPassword() != null ? bcryptEncoder.encode(user.getPassword()) : existingUser.getPassword());
		return userRepository.save(existingUser);
	}

	@Override
	public void deleteUser() {
		User existingUser = readUser();
		userRepository.delete(existingUser);
	}

	@Override
	public User getLoggedInUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		String email = authentication.getName();
		
		return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found for the email"+email));
	}
	
	@Override
	@Transactional
	public String regenerateOtp(String email, String contextPath) {
	    User user = userRepository.findByEmail(email)
	        .orElseThrow(() -> new RuntimeException("User not found with this email: " + email));
	    if(user.isEnabled()) {
	    	throw new ItemExistsException("User already verified, please login!!!");
	    }
	    UserModel userModel = new UserModel(user.getName(), user.getEmail(), user.getPassword(), user.getRoles());
	    String otp = RandomString.make(30);
	    try {
	      emailUtil.sendOtpEmail(userModel.getEmail(), otp, contextPath);
	    } catch (MessagingException e) {
	      throw new RuntimeException("Unable to send otp please try again");
	    }
	    user.setVerificationToken(otp);
	    user.setVerificationTokenTime(LocalDateTime.now());
	    userRepository.save(user);
	    return "Email sent... please verify account within 1 minute";
	  }

}

























