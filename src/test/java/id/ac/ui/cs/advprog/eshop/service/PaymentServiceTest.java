package id.ac.ui.cs.advprog.eshop.service;

import id.ac.ui.cs.advprog.eshop.enums.OrderStatus;
import id.ac.ui.cs.advprog.eshop.enums.PaymentStatus;
import id.ac.ui.cs.advprog.eshop.model.Order;
import id.ac.ui.cs.advprog.eshop.model.Product;
import id.ac.ui.cs.advprog.eshop.model.Payment;
import id.ac.ui.cs.advprog.eshop.model.PaymentWithCOD;
import id.ac.ui.cs.advprog.eshop.model.PaymentWithVoucher;
import id.ac.ui.cs.advprog.eshop.repository.PaymentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @InjectMocks
    PaymentServiceImpl paymentService;

    @Mock
    PaymentRepository paymentRepository;

    List<Payment> paymentList;
    List<Product> productList;
    List<Order> orderList;

    @BeforeEach
    void setup() {
        paymentList = new ArrayList<>();

        productList = new ArrayList<>();
        Product product1 = new Product();
        product1.setProductId("eb558e9f-1c39-460e-8860-71af6af63bd6");
        product1.setProductName("Sampo Cap Bambang");
        product1.setProductQuantity(2);
        productList.add(product1);

        orderList = new ArrayList<>();
        Order order1 = new Order("136522556-012a-4c07-b546-54eb1396d79b", 
            productList, 1708560000L, "Safira Sudrajat");
        orderList.add(order1);
        Order order2 = new Order("7f9e15bb-4b15-42f4-aebc-c3af385fb078",
            productList, 1708570000L, "Safira Sudrajat");
        orderList.add(order2);

        Map<String, String> paymentData = new HashMap<>();
        paymentData.put("voucherCode", "ESHOP1234ABC5678");
        PaymentWithVoucher payment1 = new PaymentWithVoucher("4074c620-013b-4414-b085-08f7b08940payment1", "VOUCHER", orderList.get(0), paymentData);
        paymentList.add(payment1);

        paymentData = new HashMap<>();
        paymentData.put("address", "Universitas Indonesia");
        paymentData.put("deliveryFee", "2000");
        PaymentWithCOD payment2 = new PaymentWithCOD("ec556e96-10a5-4d47-a068-d45c6fca71c0", "COD", orderList.get(0), paymentData);
        paymentList.add(payment2);
    }

    @Test
    void testAddPaymentService() {
        Payment payment1 = paymentList.get(0);
        doReturn(payment1).when(paymentRepository).save(any(Payment.class));
        payment1 = paymentService.addPayment(payment1.getOrder(), "VOUCHER", payment1.getPaymentData());

        Payment payment2 = paymentList.get(1);
        doReturn(payment2).when(paymentRepository).save(any(Payment.class));
        payment2 = paymentService.addPayment(payment2.getOrder(), "COD", payment2.getPaymentData());

        doReturn(payment1).when(paymentRepository).findById(payment1.getId());
        Payment findResult = paymentService.getPayment(payment1.getId());

        assertEquals(payment1.getId(),findResult.getId() );
        assertEquals(payment1.getStatus(), findResult.getStatus());
        assertEquals(payment1.getMethod(), findResult.getMethod());

        doReturn(payment2).when(paymentRepository).findById(payment2.getId());
        findResult = paymentService.getPayment(payment2.getId());

        assertEquals(payment2.getId(),findResult.getId() );
        assertEquals(payment2.getStatus(), findResult.getStatus());
        assertEquals(payment2.getMethod(), findResult.getMethod());
    }

    @Test
    void testSetStatusSuccessful() {
        Map<String, String> paymentData = new HashMap<>();
        paymentData.put("voucherCode","ESHOP1234ABC5678");
        Payment payment1 = new Payment("9a72b490-9853-4ae9-8927-97f459989e8e", "VOUCHER", orderList.get(0), paymentData);

        assertEquals(PaymentStatus.PENDING.getValue(),payment1.getStatus());
        paymentService.setStatus(payment1, PaymentStatus.SUCCESS.getValue());
        assertEquals(PaymentStatus.SUCCESS.getValue(),payment1.getStatus());
        paymentService.setStatus(payment1, PaymentStatus.REJECTED.getValue());
        assertEquals(PaymentStatus.REJECTED.getValue(),payment1.getStatus());
    }

    @Test
    void testSetStatusFailed() {
        assertThrows(IllegalArgumentException.class, ()->
            paymentService.setStatus(paymentList.get(0), "NotStatus")
        );
    }

    @Test
    void testUpdateOrderStatusWhenPaymentSuccess() {
        Order order = new Order("11c590d2-b148-45bd-9012-2b808c85c3b7", 
            productList, 1708560000L, "Bambang Sutejo");
        Map<String, String> paymentData = new HashMap<>();
        paymentData.put("address", "Kukusan");
        paymentData.put("deliveryFee", "5000");
        
        PaymentWithCOD payment = new PaymentWithCOD(UUID.randomUUID().toString(), "COD", order, paymentData);

        assertEquals(OrderStatus.SUCCESS.getValue(), payment.getOrder().getStatus());
    }

    @Test
    void testUpdateOrderStatusWhenPaymentRejected() {
        Order order = new Order("11c590d2-b148-45bd-9012-2b808c85c3b7", productList, 1708560000L, "Bambang Sutejo");
        Map<String, String> paymentData = new HashMap<>();
        paymentData.put("address", " ");
        paymentData.put("deliveryFee", " ");

        PaymentWithCOD payment = new PaymentWithCOD(UUID.randomUUID().toString(), "COD", order, paymentData);

        assertEquals(OrderStatus.FAILED.getValue(), payment.getOrder().getStatus());
        paymentData.clear();
    }

    @Test
    void testGetPaymentIfFound() {
        Payment payment = paymentList.get(0);
        doReturn(payment).when(paymentRepository).findById(payment.getId());

        Payment paymentFound = paymentService.getPayment(payment.getId());
        assertEquals(payment.getId(), paymentFound.getId());
        assertEquals("VOUCHER",paymentFound.getMethod());
        assertEquals(payment.getStatus(), paymentFound.getStatus());
    }

    @Test
    void testGetPaymentIfNotFound() {
        doReturn(null).when(paymentRepository).findById("zczc");

        Payment payment = paymentService.getPayment("zczc");
        assertNull(payment);
    }

    @Test
    void testGetAllPayments() {
        doReturn(paymentList).when(paymentRepository).getAllPayments();
        List<Payment> resultPayments = paymentService.getAllPayments();

        assertNotNull(resultPayments);
        assertEquals(paymentList.size(), resultPayments.size());
        assertTrue(resultPayments.containsAll(paymentList));
    }
}
