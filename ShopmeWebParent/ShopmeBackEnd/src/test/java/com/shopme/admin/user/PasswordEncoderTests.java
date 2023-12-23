package com.shopme.admin.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderTests {
	
	@Test
	public void testEncodePassword() {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		String rawPwd = "12345678";
		String encodedPwd = encoder.encode(rawPwd);
		System.out.println(encodedPwd);
		
		boolean match = encoder.matches(rawPwd, encodedPwd);
		
		assertThat(match).isTrue();
	}
}
