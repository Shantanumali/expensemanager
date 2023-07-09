package in.shantanum.expensetrackerapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import in.shantanum.expensetrackerapi.entity.PasswordResetToken;
import in.shantanum.expensetrackerapi.entity.User;

@Repository
public interface PasswordTokenRepository extends JpaRepository<PasswordResetToken, Long> {
	  PasswordResetToken findByToken(String token);

	Optional<PasswordResetToken> findByUserAndToken(User user, String resetasswordToken);

}
