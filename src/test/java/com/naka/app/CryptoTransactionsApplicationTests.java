package com.naka.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.apache.kafka.clients.admin.NewTopic;



@SpringBootTest
class CryptoTransactionsApplicationTests {
	
    //private static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1, false, 5, "cat", "hat");

//    @Test
//    public void test() {
//    	embeddedKafka.getEmbeddedKafka().addTopics(new NewTopic("thing1", 10, (short) 1), new NewTopic("thing2", 15, (short) 1));
//    }

	@Test
	void contextLoads() {
		System.out.println("Test running");
	}

}
