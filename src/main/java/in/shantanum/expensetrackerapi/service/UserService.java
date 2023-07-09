package in.shantanum.expensetrackerapi.service;

import javax.validation.Valid;

import in.shantanum.expensetrackerapi.entity.PasswordDto;
import in.shantanum.expensetrackerapi.entity.User;
import in.shantanum.expensetrackerapi.entity.UserModel;

public interface UserService {
	
	User createUser(UserModel user, String string);
	
	User readUser();
	
	User updateUser(UserModel user);
	
	void deleteUser();
	
	User getLoggedInUser();

	String regenerateOtp(String email, String string);

	String verifyAccount(String email, String otp);

	String generateForgotPasswordToken(String userEmail, String appUrl);

	String validatePasswordResetToken(@Valid PasswordDto passwordDto);	
}
