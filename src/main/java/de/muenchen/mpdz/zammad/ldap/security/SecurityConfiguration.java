package de.muenchen.mpdz.zammad.ldap.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

	@Value("${spring.security.user.name}")
	private String username;

	@Value("${spring.security.user.password}")
	private String password;

	@Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		 http
		 	.csrf()
		 	.disable()
		 	.authorizeHttpRequests()
		 	.anyRequest().authenticated()
		 	.and()
		 	.httpBasic();

        return http.build();
    }


	@Bean
    public InMemoryUserDetailsManager userDetailsService() {

		PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

        UserDetails user = User
            .withUsername(username)
            .password(encoder.encode( password))
            .roles("ldapsync")
            .build();
        return new InMemoryUserDetailsManager(user);

    }


}