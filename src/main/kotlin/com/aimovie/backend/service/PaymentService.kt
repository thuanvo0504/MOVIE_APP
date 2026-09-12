package com.aimovie.backend.service

import com.aimovie.backend.entity.Payment
import com.aimovie.backend.repository.BookingRepository
import com.aimovie.backend.repository.PaymentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val bookingRepository: BookingRepository
) {

    fun findAll(): List<Payment> {
        return paymentRepository.findAll()
    }

    fun findById(paymentId: Long): Payment? {
        return paymentRepository.findById(paymentId).orElse(null)
    }

    fun findByBookingId(bookingId: Long): List<Payment> {
        return paymentRepository.findByBookingId(bookingId)
    }

    fun findByTransactionCode(transactionCode: String): Payment? {
        return paymentRepository.findByTransactionCode(transactionCode)
    }

    @Transactional
    fun createPayment(
        bookingId: Long,
        amount: BigDecimal,
        paymentMethod: String
    ): Payment {

        // 1. Kiểm tra Booking tồn tại
        val booking = bookingRepository.findById(bookingId)
            .orElseThrow {
                IllegalArgumentException("Booking not found")
            }

        // 2. Booking phải ở trạng thái CONFIRMED
        if (booking.status != "CONFIRMED") {
            throw IllegalStateException(
                "Booking is not available for payment"
            )
        }

        // 3. Không cho thanh toán số tiền khác với Booking
        if (amount.compareTo(booking.totalAmount) != 0) {
            throw IllegalArgumentException(
                "Payment amount does not match booking total"
            )
        }

        // 4. Chỉ hỗ trợ MOCK ở giai đoạn hiện tại
        if (paymentMethod != "MOCK") {
            throw IllegalArgumentException(
                "Unsupported payment method"
            )
        }

        // 5. Không tạo payment SUCCESS thứ hai cho cùng Booking
        val existingSuccessfulPayment =
            paymentRepository.findByBookingId(bookingId)
                .firstOrNull { it.status == "SUCCESS" }

        if (existingSuccessfulPayment != null) {
            throw IllegalStateException(
                "Booking has already been paid"
            )
        }

        // 6. Mock payment thành công
        val payment = Payment().apply {
            this.bookingId = bookingId
            this.amount = booking.totalAmount
            this.paymentMethod = paymentMethod
            this.status = "SUCCESS"
            this.transactionCode = "TXN-${UUID.randomUUID()}"
            this.createdAt = LocalDateTime.now()
        }

        return paymentRepository.save(payment)
    }
}