package com.aimovie.backend.controller

import com.aimovie.backend.dto.BookingRequest
import com.aimovie.backend.entity.Booking
import com.aimovie.backend.service.BookingService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/bookings")
class BookingController(
    private val bookingService: BookingService
) {

    @GetMapping
    fun getAllBookings(): ResponseEntity<List<Booking>> {
        val userId = getAuthenticatedUserId()

        return ResponseEntity.ok(
            bookingService.findAll(userId)
        )
    }

    @GetMapping("/{bookingId}")
    fun getBookingById(
        @PathVariable bookingId: Long
    ): ResponseEntity<Booking> {

        val userId = getAuthenticatedUserId()

        val booking = bookingService.findById(
            bookingId,
            userId
        ) ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(booking)
    }

    @GetMapping("/code/{bookingCode}")
    fun getBookingByCode(
        @PathVariable bookingCode: String
    ): ResponseEntity<Booking> {

        val userId = getAuthenticatedUserId()

        val booking = bookingService.findByBookingCode(
            bookingCode,
            userId
        ) ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(booking)
    }

    @PostMapping
    fun createBooking(
        @RequestBody request: BookingRequest
    ): ResponseEntity<Booking> {

        val userId = getAuthenticatedUserId()

        val booking = bookingService.createBooking(
            userId = userId,
            showtimeId = request.showtimeId,
            showtimeSeatIds = request.showtimeSeatIds
        )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(booking)
    }

    private fun getAuthenticatedUserId(): Long {
        val authentication =
            SecurityContextHolder
                .getContext()
                .authentication
                ?: throw IllegalArgumentException(
                    "Authentication is required"
                )

        return authentication.name.toLongOrNull()
            ?: throw IllegalArgumentException(
                "Invalid authenticated user"
            )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        exception: IllegalArgumentException
    ): ResponseEntity<Map<String, String>> {

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                mapOf(
                    "error" to (
                        exception.message
                            ?: "Invalid booking request"
                        )
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
                    "error" to (
                        exception.message
                            ?: "Booking conflict"
                        )
                )
            )
    }
}