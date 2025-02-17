package com.devsuperior.dscatalog.services;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;//import statico
import static org.mockito.Mockito.verify;//import statico
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.tests.Factory;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {
	
	@InjectMocks
	private ProductService service;
	
	@Mock
	private ProductRepository repository;
	
	@Mock
	private CategoryRepository categoryRepository;
	
	private long existingId;
	private long nonExistingId;
	private long dependentId;
	private Category category;
	//findAllPagedShouldReturnPage()
	private PageImpl<Product> page;
	private Product product;
	private ProductDTO productDto;
	
	@BeforeEach
	void setUp() throws Exception {
		existingId = 1L;
		nonExistingId = 2L;
		dependentId = 3L;
		category = Factory.createCategory();
		product = Factory.createdProduct();
		productDto = Factory.createdProductDTO();
		//findAllPagedShouldReturnPage()
		page = new PageImpl<>(List.of(product));
		
		when(repository.getReferenceById(existingId)).thenReturn(product);
		when(repository.getReferenceById(nonExistingId)).thenThrow(EntityNotFoundException.class);
		
		when(categoryRepository.getReferenceById(existingId)).thenReturn(category);
		when(categoryRepository.getReferenceById(nonExistingId)).thenThrow(EntityNotFoundException.class);
		//findAllPagedShouldReturnPage()
		when(repository.findAll((Pageable)ArgumentMatchers.any())).thenReturn(page);
		
		when(repository.save(ArgumentMatchers.any())).thenReturn(product);
		//como se lê
		//quando eu chamar o repository.save passando qualquer objeto retorna um page
		
		//findByIdShouldReturnProductDTOwhenIdExists()
		when(repository.findById(existingId)).thenReturn(Optional.of(product));
		//findByIdShouldThrouwResourceNotFoundExceptionWhenIdDoesNotExist()
		when(repository.findById(nonExistingId)).thenReturn(Optional.empty());
	
		//deleteShoulDoNothingWhenExists()
		doNothing().when(repository).deleteById(existingId);
		//deleteShouldThrouwDatabaseExceptionWhenDependentId()
		doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);
		//deleteShoulThrowResourceNotFoundExceptionWhenIdDoesNotExisting()
		when(repository.existsById(existingId)).thenReturn(true);
		when(repository.existsById(nonExistingId)).thenReturn(false);
		//deleteShouldThrouwDatabaseExceptionWhenDependentId()
		when(repository.existsById(dependentId)).thenReturn(true);
	}
	
	@Test
	public void deleteShoulDoNothingWhenExists() {
		Assertions.assertDoesNotThrow(() -> {
			service.delete(existingId);
		});
		
		//import statico do Mockito sem ele a chamada seria Mockito.verify, Mockito.times...etc.
		verify(repository, times(1)).deleteById(existingId);
	}
	
	@Test
	public void deleteShoulThrowResourceNotFoundExceptionWhenIdDoesNotExisting() {
		
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.delete(nonExistingId);
		});
		
	}
	
	@Test
	public void deleteShouldThrouwDatabaseExceptionWhenDependentId() {
		Assertions.assertThrows(DatabaseException.class, () -> {
			service.delete(dependentId);
		});
	}
	
	@Test
	public void findAllPagedShouldReturnPage() {
		Pageable pageable = PageRequest.of(0, 10);
		
		Page<ProductDTO> result = service.findAllPaged(pageable);
		
		Assertions.assertNotNull(result);
		Mockito.verify(repository).findAll(pageable);
	}
	
	@Test
	public void findByIdShouldReturnProductDTOwhenIdExists() {
		
		ProductDTO result = service.findById(existingId);
		
		Assertions.assertNotNull(result);	
		
	}
	
	@Test
	public void findByIdShouldThrouwResourceNotFoundExceptionWhenIdDoesNotExist() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.findById(nonExistingId);
		});
	}
	
	@Test
	public void updateShouldReturnProductDTOwhenIdExisting() {
		
		
		ProductDTO result = service.update(existingId, productDto);
		
		Assertions.assertNotNull(result);
	}
	

	@Test
	public void updateShouldThrouwResourceNotFoundExceptionWhenIdDoesNotExist() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.update(nonExistingId, productDto);
		});
	}
	

}
