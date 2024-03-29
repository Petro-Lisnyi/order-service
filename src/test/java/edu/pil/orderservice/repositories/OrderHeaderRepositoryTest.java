package edu.pil.orderservice.repositories;

import edu.pil.orderservice.domain.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
@ActiveProfiles("local")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class OrderHeaderRepositoryTest {
    @Autowired
    OrderHeaderRepository orderHeaderRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    OrderApprovalRepository orderApprovalRepository;

    private Product product;

    @BeforeEach
    void setUp(){
        var newProduct = new Product();
        newProduct.setDescription("New product");
        newProduct.setProductStatus(ProductStatus.NEW);
        product = productRepository.saveAndFlush(newProduct);
    }

    @Test
    void testSaveOrder() {
        OrderHeader orderHeader = new OrderHeader();

        var customer = new Customer();
        customer.setCustomerName("New Customer");
        customer.setPhone("0123456789");
        var address = new Address();
        address.setCity("The best city");
        customer.setAddress(address);

        var savedCustomer = customerRepository.save(customer);
        orderHeader.setCustomer(savedCustomer);
        OrderHeader savedOrder = orderHeaderRepository.save(orderHeader);

        assertNotNull(savedOrder);
        assertNotNull(savedOrder.getId());

        OrderHeader fetchedOrder = orderHeaderRepository.getById(savedOrder.getId());

        assertNotNull(fetchedOrder);
        assertNotNull(fetchedOrder.getId());
        System.out.println(fetchedOrder.getCreatedDate().toLocalDateTime());
        assertNotNull(fetchedOrder.getCreatedDate());
        assertNotNull(fetchedOrder.getLastModifiedDate());
    }
    @Test
    void testSaveOrderWithLine() {
        OrderHeader orderHeader = new OrderHeader();

        var customer = new Customer();
        customer.setCustomerName("New Customer");
        var savedCustomer = customerRepository.save(customer);

        var orderApproval = new OrderApproval();
        orderApproval.setApprovedBy(customer.getCustomerName());
//        var savedOrderApproval = orderApprovalRepository.save(orderApproval);


        var orderLine = new OrderLine();
        orderLine.setQuantityOrdered(4);
        orderLine.setProduct(product);

        orderHeader.setCustomer(savedCustomer);
        orderHeader.setOrderApproval(orderApproval);
        orderHeader.addOrderLine(orderLine);
        OrderHeader savedOrder = orderHeaderRepository.save(orderHeader);

        assertNotNull(savedOrder);
        assertNotNull(savedOrder.getId());
        assertNotNull(savedOrder.getOrderLines());
        assertEquals(savedOrder.getOrderLines().size(), 1);
    }
    @Test
    void testDeleteOrderWithLine() {

        var orderHeader = new OrderHeader();

        var customer = new Customer();
        customer.setCustomerName("New Customer");
        var savedCustomer = customerRepository.save(customer);

        var orderApproval = new OrderApproval();
        orderApproval.setApprovedBy(customer.getCustomerName());
//        var savedOrderApproval = orderApprovalRepository.save(orderApproval);


        var orderLine = new OrderLine();
        orderLine.setQuantityOrdered(5);
        orderLine.setProduct(product);

        orderHeader.setCustomer(savedCustomer);
        orderHeader.setOrderApproval(orderApproval);
        orderHeader.addOrderLine(orderLine);
        var savedOrder = orderHeaderRepository.saveAndFlush(orderHeader);

        assertNotNull(savedOrder);
        assertNotNull(savedOrder.getId());
        assertNotNull(savedOrder.getOrderLines());
        assertEquals(savedOrder.getOrderLines().size(), 1);

        orderHeaderRepository.deleteById(savedOrder.getId());
        orderHeaderRepository.flush();

        assertThrows(EntityNotFoundException.class, () -> {
            var fetched = orderHeaderRepository.getById(savedOrder.getId());
            System.out.println(fetched);
            assertNull(fetched);
        });
    }

    @Test
    void customerValidationTest(){
        var customer = new Customer();
        var address = new Address();

        customer.setEmail("not validEmail");
        customer.setPhone("0987654321");

        address.setAddress("somewhere there");
        address.setState("the state");
        address.setCity("the city");
        customer.setAddress(address);

        assertThrows(ConstraintViolationException.class, () -> customerRepository.save(customer));
    }
}
