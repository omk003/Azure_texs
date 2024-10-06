package com.example.chat_managenent;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.chat_management.ChatManagementApplication;

@SpringBootTest(classes = ChatManagementApplication.class, properties = "spring.profiles.active=test")
class ChatManagenentApplicationTests {

	@Test
	void contextLoads() {
	}

}
