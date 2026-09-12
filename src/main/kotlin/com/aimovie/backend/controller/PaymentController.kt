package com.aimovie.backend.controller

import com.aimovie.backend.dto.PaymentRequest
import com.aimovie.backend.entity.Payment
import com.aimovie.backend.service.PaymentService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/payments")
class PaymentController(
    private val paymentService: PaymentService
) {

    @GetMapping
    fun getAllPayments(): ResponseEntity<List<Payment>> =
        ResponseEntity.ok(paymentService.findAll())

    @GetMapping("/{paymentId}")
    fun getPaymentById(
        @PathVariable paymentId: Long
    ): ResponseEntity<Payment> {

        val payment = paymentService.findById(paymentId)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(payment)
    }

    @GetMapping("/booking/{bookingId}")
    fun getPaymentsByBookingId(
        @PathVariable bookingId: Long
    ): ResponseEntity<List<Payment>> {

        return ResponseEntity.ok(
            paymentService.findByBookingId(bookingId)
        )
    }

    @GetMapping("/transaction/{transactionCode}")
    fun getPaymentByTransactionCode(
        @PathVariable transactionCode: String
    ): ResponseEntity<Payment> {

        val payment = paymentService.findByTransactionCode(transactionCode)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(payment)
    }

    @PostMapping
    fun createPayment(
        @RequestBody request: PaymentRequest,
        authentication: Authentication
    ): ResponseEntity<Payment> {

        val payment = paymentService.createPayment(
            bookingId = request.bookingId,
            amount = request.amount,
            paymentMethod = request.paymentMethod
        )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(payment)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        exception: IllegalArgumentException
    ): ResponseEntity<Map<String, String>> {

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                mapOf(
                    "error" to (exception.message ?: "Invalid payment request")
                )
            )
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(
        exception: IllegalStateException
    ): ResponseEntity<Map<String, String>> {

        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(
                mapOf(
                    "error" to (exception.message ?: "Payment conflict")
                )
            )
    }
}