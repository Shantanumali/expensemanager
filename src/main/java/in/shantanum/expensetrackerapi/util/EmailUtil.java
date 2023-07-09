package in.shantanum.expensetrackerapi.util;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;

import in.shantanum.expensetrackerapi.entity.UserModel;

@Component
public class EmailUtil {
	
	@Autowired
	private JavaMailSender mailSender;

	@Value("${mail.from-email-id}")
	private String fromEmailId;
	
	@Value("${aplication.baseuri}")
	private String baseUri;

	public void sendOtpEmail(String email, String otp, String contextPath) throws MessagingException {
	    MimeMessage mimeMessage = mailSender.createMimeMessage();
	    MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
	    mimeMessageHelper.setTo(email);
	    mimeMessageHelper.setSubject("Verify OTP");
	    mimeMessageHelper.setText("<div>"
	          +"<a href=\""+contextPath+"/verify-account?email="+email+"&token="+otp+"\" target=\"_blank\">click link to verify</a>"
	        +"</div>", true);

	    mailSender.send(mimeMessage);
	  }
	
	/*
	public void sendVeifyTokenEmail(String token, UserModel user) {
		String url = baseUri + "/verify-account?email="+user.getEmail()+"&token=" + token;
		String subject = "Here's the link to reset your password";
		String content = "<p>Hello " + user.getName() + "</p>" + "<p>Please verify your account.</p>"
				+ "<p>Click the link below to verify your account:</p>" + "<p><a href=\"" + url
				+ "\">verify my account</a></p>";
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
			
			@Override
			public void prepare(MimeMessage mimeMessage) throws Exception {
				mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
				mimeMessage.setFrom(new InternetAddress(fromEmailId));
				mimeMessage.setSubject(subject);
				MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
				helper.setText(content, true);
			}
		};
			mailSender.send(preparator);
	}
	*/
}
