package com.example.productservice;

import com.example.productservice.dto.ProductRequest;
import com.example.productservice.repository.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.bouncycastle.asn1.cmp.OOBCertHash;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers // junit 5 sẽ hiểu răng sẽ sử dụng test container để run test
@AutoConfigureMockMvc
class ProductServiceApplicationTests {

	@Container // junit 5 sẽ hiểu rằng đây là container , cụ thể là mongo container
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProductRepository productRepository;




	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry){
		// tại thời điểm bắt đầu chạy intergration test , ban đầu testcontainer sẽ start mongodbcontainer bằng cách
		// sử dụng docker image mông:4.4.2 , sau khi start container xong , nó sẽ lấy replica url và thêm vào trong properties
		// của spring context là spring.data.mongodb.uri dynamically (giống với cách định nghĩa trong application.properties
		// static )
		dynamicPropertyRegistry.add("spring.data.mongodb.uri" , mongoDBContainer::getReplicaSetUrl);
	}

	@Test
	void testCreateProduct() throws Exception {
		// mockMvc tạo một mocked servlet environment , ta có thể gọi đc controller enpoint và nhận được response
		ProductRequest productRequest = getProductRequest();
		String request = objectMapper.writeValueAsString(productRequest);
		mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
				.contentType(MediaType.APPLICATION_JSON)
				.content(request))
				.andExpect(status().isCreated());
		assertEquals(1, productRepository.findAll().size());

	}

	public ProductRequest getProductRequest(){
		return ProductRequest.builder()
				.name("iphone13")
				.description("iphone 13")
				.price(BigDecimal.valueOf(1200)).build();
	}



}
