// src/test/java/com/tuantu/salesapp/models/entity/CustomerTest.java
package com.tuantu.salesapp.models.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

// Unit tests for the Customer entity record verifying compact constructor invariant validations,
// factory method defaults, value immutability, and equality contracts — ensuring that invalid customer
// data is rejected before reaching persistence or business logic layers without requiring database access.
class CustomerTest {

    // Builds a baseline valid customer record with all 17 components populated.
    private Customer createValidCustomer() {
        return new Customer(
                1,
                "KH0001",
                "Cửa hàng Tuấn Tú",
                10,
                20,
                30,
                40,
                CustomerRank.A,
                new BigDecimal("10000000"),
                30,
                5,
                "0901234567",
                "123 Đường Lê Lợi, Phường 1, Quận 1",
                "Khách hàng thân thiết",
                true,
                Instant.parse("2026-01-01T08:00:00Z"),
                Instant.parse("2026-01-02T09:00:00Z"));
    }

    // Creates a customer copy overriding code to test validation invariants.
    private Customer withCode(String code) {
        Customer c = createValidCustomer();
        return new Customer(
                c.id(),
                code,
                c.name(),
                c.groupId(),
                c.provinceId(),
                c.wardId(),
                c.salesRepId(),
                c.rank(),
                c.creditLimit(),
                c.paymentTermDays(),
                c.reminderDaysBefore(),
                c.phone(),
                c.address(),
                c.note(),
                c.isActive(),
                c.createdAt(),
                c.updatedAt());
    }

    // Creates a customer copy overriding name to test validation invariants.
    private Customer withName(String name) {
        Customer c = createValidCustomer();
        return new Customer(
                c.id(),
                c.code(),
                name,
                c.groupId(),
                c.provinceId(),
                c.wardId(),
                c.salesRepId(),
                c.rank(),
                c.creditLimit(),
                c.paymentTermDays(),
                c.reminderDaysBefore(),
                c.phone(),
                c.address(),
                c.note(),
                c.isActive(),
                c.createdAt(),
                c.updatedAt());
    }

    // Creates a customer copy overriding creditLimit to test validation invariants.
    private Customer withCreditLimit(BigDecimal creditLimit) {
        Customer c = createValidCustomer();
        return new Customer(
                c.id(),
                c.code(),
                c.name(),
                c.groupId(),
                c.provinceId(),
                c.wardId(),
                c.salesRepId(),
                c.rank(),
                creditLimit,
                c.paymentTermDays(),
                c.reminderDaysBefore(),
                c.phone(),
                c.address(),
                c.note(),
                c.isActive(),
                c.createdAt(),
                c.updatedAt());
    }

    // Creates a customer copy overriding paymentTermDays to test validation invariants.
    private Customer withPaymentTermDays(Integer paymentTermDays) {
        Customer c = createValidCustomer();
        return new Customer(
                c.id(),
                c.code(),
                c.name(),
                c.groupId(),
                c.provinceId(),
                c.wardId(),
                c.salesRepId(),
                c.rank(),
                c.creditLimit(),
                paymentTermDays,
                c.reminderDaysBefore(),
                c.phone(),
                c.address(),
                c.note(),
                c.isActive(),
                c.createdAt(),
                c.updatedAt());
    }

    // Creates a customer copy overriding reminderDaysBefore to test validation invariants.
    private Customer withReminderDaysBefore(Integer reminderDaysBefore) {
        Customer c = createValidCustomer();
        return new Customer(
                c.id(),
                c.code(),
                c.name(),
                c.groupId(),
                c.provinceId(),
                c.wardId(),
                c.salesRepId(),
                c.rank(),
                c.creditLimit(),
                c.paymentTermDays(),
                reminderDaysBefore,
                c.phone(),
                c.address(),
                c.note(),
                c.isActive(),
                c.createdAt(),
                c.updatedAt());
    }

    // C1: Factory method initializes default values for id, timestamps, optional fields, and sets isActive to true.
    @Test
    void shouldCreateNewCustomerWithFactoryDefaults() {
        Customer customer = Customer.newCustomer("KH001", "Cửa hàng A");

        assertThat(customer.id()).isNull();
        assertThat(customer.code()).isEqualTo("KH001");
        assertThat(customer.name()).isEqualTo("Cửa hàng A");
        assertThat(customer.groupId()).isNull();
        assertThat(customer.provinceId()).isNull();
        assertThat(customer.wardId()).isNull();
        assertThat(customer.salesRepId()).isNull();
        assertThat(customer.rank()).isNull();
        assertThat(customer.creditLimit()).isNull();
        assertThat(customer.paymentTermDays()).isNull();
        assertThat(customer.reminderDaysBefore()).isNull();
        assertThat(customer.phone()).isNull();
        assertThat(customer.address()).isNull();
        assertThat(customer.note()).isNull();
        assertThat(customer.isActive()).isTrue();
        assertThat(customer.createdAt()).isNull();
        assertThat(customer.updatedAt()).isNull();
    }

    // C2: Null, empty, or whitespace-only customer code is rejected.
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void shouldRejectNullOrBlankCode(String invalidCode) {
        assertThatThrownBy(() -> withCode(invalidCode))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Customer code must not be null or blank.");
    }

    // C3: Null, empty, or whitespace-only customer name is rejected.
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void shouldRejectNullOrBlankName(String invalidName) {
        assertThatThrownBy(() -> withName(invalidName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Customer name must not be null or blank.");
    }

    // C4: Customer code and name retain surrounding whitespace as given without automatic trimming.
    @Test
    void shouldNotTrimCodeAndName() {
        Customer customer = Customer.newCustomer(" KH001 ", " Cửa hàng A ");

        assertThat(customer.code()).isEqualTo(" KH001 ");
        assertThat(customer.name()).isEqualTo(" Cửa hàng A ");
    }

    // C5: Null, zero, and positive credit limits are valid.
    @Test
    void shouldAcceptNullZeroAndPositiveCreditLimit() {
        Customer nullLimit = withCreditLimit(null);
        assertThat(nullLimit.creditLimit()).isNull();

        Customer zeroLimit = withCreditLimit(BigDecimal.ZERO);
        assertThat(zeroLimit.creditLimit()).isEqualTo(BigDecimal.ZERO);

        BigDecimal positiveAmount = new BigDecimal("5000000");
        Customer positiveLimit = withCreditLimit(positiveAmount);
        assertThat(positiveLimit.creditLimit()).isEqualTo(positiveAmount);
    }

    // C6: Negative credit limit is rejected.
    @Test
    void shouldRejectNegativeCreditLimit() {
        assertThatThrownBy(() -> withCreditLimit(new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("Customer credit limit");
    }

    // C7: Whole number credit limit with decimal scale is accepted without changing its scale.
    @Test
    void shouldAcceptCreditLimitWithTrailingZerosWithoutModifyingScale() {
        BigDecimal limit = new BigDecimal("1000.00");
        Customer customer = withCreditLimit(limit);

        assertThat(customer.creditLimit()).isEqualByComparingTo(limit);
        assertThat(customer.creditLimit().scale()).isEqualTo(2);
    }

    // C8: Fractional VND amounts in credit limit are rejected.
    @Test
    void shouldRejectFractionalVndCreditLimit() {
        assertThatThrownBy(() -> withCreditLimit(new BigDecimal("1000.5")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Customer credit limit must be a whole number of VND.");
    }

    // C9: Negative fractional credit limit is rejected with IllegalArgumentException.
    @Test
    void shouldRejectNegativeFractionalCreditLimit() {
        assertThatThrownBy(() -> withCreditLimit(new BigDecimal("-0.5"))).isInstanceOf(IllegalArgumentException.class);
    }

    // C10: Payment term days accepts null and zero, and rejects negative values.
    @Test
    void shouldAcceptNullAndZeroPaymentTermDaysAndRejectNegative() {
        assertThat(withPaymentTermDays(null).paymentTermDays()).isNull();
        assertThat(withPaymentTermDays(0).paymentTermDays()).isZero();

        assertThatThrownBy(() -> withPaymentTermDays(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("Customer payment term days");
    }

    // C11: Reminder days before accepts null and zero, and rejects negative values.
    @Test
    void shouldAcceptNullAndZeroReminderDaysBeforeAndRejectNegative() {
        assertThat(withReminderDaysBefore(null).reminderDaysBefore()).isNull();
        assertThat(withReminderDaysBefore(0).reminderDaysBefore()).isZero();

        assertThatThrownBy(() -> withReminderDaysBefore(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("Customer reminder days before");
    }

    // C12: Identical Customer records satisfy Java record value equality and hashCode contract.
    @Test
    void shouldVerifyEqualityAndHashCodeForIdenticalRecords() {
        Customer first = createValidCustomer();
        Customer second = createValidCustomer();

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    // C13: CustomerRank enum values and order match database CHECK constraint.
    @Test
    void shouldVerifyCustomerRankValuesAndOrder() {
        assertThat(CustomerRank.valueOf("A")).isEqualTo(CustomerRank.A);
        assertThat(CustomerRank.valueOf("B")).isEqualTo(CustomerRank.B);
        assertThat(CustomerRank.valueOf("C")).isEqualTo(CustomerRank.C);

        assertThat(CustomerRank.values()).containsExactly(CustomerRank.A, CustomerRank.B, CustomerRank.C);
    }
}
