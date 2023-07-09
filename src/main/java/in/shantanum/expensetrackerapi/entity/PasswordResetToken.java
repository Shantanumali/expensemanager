package in.shantanum.expensetrackerapi.entity;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "password_reset_token")
public class PasswordResetToken{
	
	public PasswordResetToken( User user, String token) {
		setUser(user);
		setToken(token);
		setExpiryDate(LocalDateTime.now().plusMinutes(EXPIRATION_IN_MINUTES));
	}

	private static final int EXPIRATION_IN_MINUTES = 1;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    private String token;
 
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;
 
    private LocalDateTime expiryDate;
}
