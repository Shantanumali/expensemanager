package in.shantanum.expensetrackerapi.service;

import java.time.Duration;
import java.time.LocalDateTime;

import javax.mail.MessagingException;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import in.shantanum.expensetrackerapi.entity.PasswordDto;
import in.shantanum.expensetrackerapi.entity.PasswordResetToken;
import in.shantanum.expensetrackerapi.entity.User;
import in.shantanum.expensetrackerapi.entity.UserModel;
import in.shantanum.expensetrackerapi.exceptions.ItemExistsException;
import in.shantanum.expensetrackerapi.exceptions.ResourceNotFoundException;
import in.shantanum.expensetrackerapi.repository.PasswordTokenRepository;
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
	private PasswordTokenRepository passwordTokenRepository;

	@Autowired
	private EmailUtil emailUtil;

	@Override
	@Transactional
	public User createUser(UserModel user, String contextPath) {
		if (userRepository.existsByEmail(user.getEmail())) {
			throw new ItemExistsException("User is already register with email:" + user.getEmail());
		}

		String token = RandomString.make(30);
		try {
			// emailUtil.sendVeifyTokenEmail(token, user);
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
		// BeanUtils.copyProperties(user, newUser);
		newUser.setPassword(bcryptEncoder.encode(user.getPassword()));
		return userRepository.save(newUser);
	}

	@Override
	@Transactional
	public String verifyAccount(String email, String otp) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("User not found with this email: " + email));
		if (user.getVerificationToken().equals(otp)
				&& Duration.between(user.getVerificationTokenTime(), LocalDateTime.now()).getSeconds() < (1 * 60)) {
			user.setEnabled(true);
			userRepository.save(user);
			return "OTP verified you can login";
		}
		return "Please regenerate otp and try again";
	}

	@Override
	public User readUser() {
		Long userId = getLoggedInUser().getId();
		return userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found for the id:" + userId));
	}

	@Override
	public User updateUser(UserModel user) {
		User existingUser = readUser();
		existingUser.setName(user.getName() != null ? user.getName() : existingUser.getName());
		existingUser.setEmail(user.getEmail() != null ? user.getEmail() : existingUser.getEmail());
		existingUser.setPassword(
				user.getPassword() != null ? bcryptEncoder.encode(user.getPassword()) : existingUser.getPassword());
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

		return userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("User not found for the email" + email));
	}

	@Override
	@Transactional
	public String regenerateOtp(String email, String contextPath) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("User not found with this email: " + email));
		if (user.isEnabled()) {
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

	@Override
	public String generateForgotPasswordToken(String email, String appUrl) {
		try {
			User user = userRepository.findByEmail(email.toLowerCase())
					.orElseThrow(() -> new RuntimeException("User not found with this email: " + email));
			String token = RandomString.make(30);
			createPasswordResetTokenForUser(user, token);
			emailUtil.sendResetTokenEmail(appUrl, token, user);
		} catch (MailException e) {
			throw new RuntimeException("Error while sending email");
		} catch (UsernameNotFoundException ex) {
			throw new RuntimeException(ex.getMessage());
		} catch (Exception ex) {
			throw new RuntimeException("User acesss restricted!!!");
		}
		return "We have sent a reset password link to your email. Please check.";
	}
	
	public void createPasswordResetTokenForUser(User user, String token) {
		PasswordResetToken myToken = new PasswordResetToken(user, token);
		passwordTokenRepository.save(myToken);
	}

	@Override
	public String validatePasswordResetToken(PasswordDto passwordDto) {
		User user = userRepository.findByEmail(passwordDto.getEmail().toLowerCase())
				.orElseThrow(() -> new RuntimeException("User not found with this email: " + passwordDto.getEmail()));
		
		PasswordResetToken passToken = passwordTokenRepository.
				findByUserAndToken(user, passwordDto.getResetasswordToken()).orElseThrow(
						() -> new RuntimeException("Invalid User or token for: " + passwordDto.getEmail()));

		if(isTokenExpired(passToken)){
			throw new RuntimeException("Token expired, please regenerate token and try again!!!");
		}
		
	        changeUserPassword(user, passwordDto.getNewPassword());
	        deletePasswordResetTokenForUser(user, passwordDto.getResetasswordToken());
	        return "Your password is changed successfully!!!";
	}
	
	private void deletePasswordResetTokenForUser(User user, String resetasswordToken) {
		passwordTokenRepository.deleteByUserAndToken(user, resetasswordToken);
	}

	private void changeUserPassword(User user, String newPassword) {
			User existingUser = userRepository.findByEmail(user.getEmail()).orElseThrow(
					()-> new RuntimeException("User not found!!!"));
			existingUser.setPassword(bcryptEncoder.encode(user.getPassword()));
			userRepository.save(existingUser);
	}

	private boolean isTokenExpired(PasswordResetToken passToken) {
	    return passToken.getExpiryDate().isBefore(LocalDateTime.now());
	}

}
