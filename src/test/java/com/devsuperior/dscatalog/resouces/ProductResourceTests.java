package com.devsuperior.dscatalog.resouces;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.resources.ProductResource;
import com.devsuperior.dscatalog.services.ProductService;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.tests.Factory;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ProductResource.class)
public class ProductResourceTests {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean
	private ProductService service;
	
	private Long existingId;
	private Long nonexistingId;
	private Long dependentId;
	private ProductDTO productDTO;
	
	private PageImpl<ProductDTO> page;
	
	@BeforeEach
	void setUp() throws Exception {
		
		existingId = 1L;
		nonexistingId = 2L;
		dependentId = 3L;
		
		productDTO = Factory.createdProductDTO();
		
		page = new PageImpl<>(List.of(productDTO));
		
		when(service.findAllPaged(any())).thenReturn(page);
		
		when(service.findById(existingId)).thenReturn(productDTO);
		when(service.findById(nonexistingId)).thenThrow(ResourceNotFoundException.class);

		when(service.update(eq(existingId), any())).thenReturn(productDTO);
		when(service.update(eq(nonexistingId), any())).thenThrow(ResourceNotFoundException.class);
		
		when(service.insert(any())).thenReturn(productDTO);
	
		doNothing().when(service).delete(existingId);
		doThrow(ResourceNotFoundException.class).when(service).delete(nonexistingId);
		doThrow(DatabaseException.class).when(service).delete(dependentId);
	}
	
	@Test
	public void deleteShouldReturnNoContentWhenIdExists() throws Exception{
		
		ResultActions result = 		
				mockMvc.perform(delete("/products/{id}", existingId)
						.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isNoContent());

		
	}
	
	@Test
	public void deleteShouldReturnNotFoundWhenDoesNotExists() throws Exception{
		
		ResultActions result = 		
				mockMvc.perform(delete("/products/{id}", nonexistingId)
						.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isNotFound());
			
	}
	
	@Test
	public void insertShouldReturnProdutoDTOCreated() throws Exception{
		
		String JsonBody = objectMapper.writeValueAsString(productDTO);
		
		ResultActions result = 		
				mockMvc.perform(post("/products", existingId)
						.content(JsonBody)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isCreated());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").exists());
		result.andExpect(jsonPath("$.description").exists());
		
	}
	
	@Test
	public void insertShouldReturnProdutoDTOWhenIdExists() throws Exception{
		
		String JsonBody = objectMapper.writeValueAsString(productDTO);
		
		ResultActions result = 		
				mockMvc.perform(put("/products/{id}", existingId)
						.content(JsonBody)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").exists());
		result.andExpect(jsonPath("$.description").exists());
		
	}
	
	@Test
	public void findAllShouldReturnPage() throws Exception {
		
		ResultActions result = 		
				mockMvc.perform(get("/products")
						.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isOk());
	}
	
	@Test
	public void findByIdShouldReturnProductDTOWhenIdExists() throws Exception{
		ResultActions result = 		
				mockMvc.perform(get("/products/{id}", existingId)
						.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").exists());
		result.andExpect(jsonPath("$.description").exists());

	}
	
	@Test
	public void findByIdShouldReturnNotFoundWhenIdDoesExist() throws Exception {
		ResultActions result = 		
				mockMvc.perform(get("/products/{id}", nonexistingId)
						.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isNotFound());

	}

	
	@Test
	public void updateShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
		String JsonBody = objectMapper.writeValueAsString(productDTO);

		ResultActions result = 
				mockMvc.perform(put("/products/{id}", nonexistingId)
						.content(JsonBody)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isNotFound());
	}

	@Test
	public void updateShouldReturnProductDTOWhenIdExist() throws Exception{
		
		String JsonBody = objectMapper.writeValueAsString(productDTO);
		
		ResultActions result = 
				mockMvc.perform(put("/products/{id}", existingId)
						.content(JsonBody)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isOk());

		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").exists());
		result.andExpect(jsonPath("$.description").exists());
	}
}
