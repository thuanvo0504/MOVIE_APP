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
    fun getAllBookings(): ResponseEntity<List<Booking>> =
        ResponseEntity.ok(bookingService.findAll())

    @GetMapping("/{bookingId}")
    fun getBookingById(
        @PathVariable bookingId: Long
    ): ResponseEntity<Booking> {
        val booking = bookingService.findById(bookingId)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(booking)
    }

    @GetMapping("/code/{bookingCode}")
    fun getBookingByCode(
        @PathVariable bookingCode: String
    ): ResponseEntity<Booking> {
        val booking = bookingService.findByBookingCode(bookingCode)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(booking)
    }

    @PostMapping
    fun createBooking(
        @RequestBody request: BookingRequest
    ): ResponseEntity<Booking> {

        val authentication =
            SecurityContextHolder.getContext().authentication
                ?: throw IllegalArgumentException(
                    "Authentication is required"
                )

        val userId = authentication.name.toLongOrNull()
            ?: throw IllegalArgumentException(
                "Invalid authenticated user"
            )

        val booking = bookingService.createBooking(
            userId = userId,
            showtimeId = request.showtimeId,
            showtimeSeatIds = request.showtimeSeatIds
        )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(booking)
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(
        exception: IllegalStateException
    ): ResponseEntity<Map<String, String>> =
        ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(
                mapOf(
                    "error" to (
                        exception.message
                            ?: "Booking conflict"
                        )
                )
            )

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        exception: IllegalArgumentException
    ): ResponseEntity<Map<String, String>> =
        ResponseEntity
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