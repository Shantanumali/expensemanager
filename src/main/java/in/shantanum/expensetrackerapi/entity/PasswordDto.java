package in.shantanum.expensetrackerapi.entity;

import lombok.Data;

@Data
public class PasswordDto {
	private String email;
	private String token;
	private String newPassword;
}
