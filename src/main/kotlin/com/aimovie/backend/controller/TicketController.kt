package com.aimovie.backend.controller

import com.aimovie.backend.entity.Ticket
import com.aimovie.backend.service.TicketService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/tickets")
class TicketController(
    private val ticketService: TicketService
) {

    @GetMapping
    fun getAllTickets(): ResponseEntity<List<Ticket>> =
        ResponseEntity.ok(
            ticketService.findAll()
        )

    @GetMapping("/{ticketId}")
    fun getTicketById(
        @PathVariable ticketId: Long
    ): ResponseEntity<Ticket> {

        val ticket = ticketService.findById(ticketId)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(ticket)
    }

    @GetMapping("/booking/{bookingId}")
    fun getTicketsByBookingId(
        @PathVariable bookingId: Long
    ): ResponseEntity<List<Ticket>> {

        return ResponseEntity.ok(
            ticketService.findByBookingId(bookingId)
        )
    }

    @GetMapping("/booking-seat/{bookingSeatId}")
    fun getTicketByBookingSeatId(
        @PathVariable bookingSeatId: Long
    ): ResponseEntity<Ticket> {

        val ticket = ticketService.findByBookingSeatId(bookingSeatId)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(ticket)
    }

    @GetMapping("/qr/{qrCode}")
    fun getTicketByQrCode(
        @PathVariable qrCode: String
    ): ResponseEntity<Ticket> {

        val ticket = ticketService.findByQrCode(qrCode)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(ticket)
    }

    @PostMapping("/booking/{bookingId}")
    fun createTicketsForBooking(
        @PathVariable bookingId: Long
    ): ResponseEntity<List<Ticket>> {

        val tickets = ticketService.createTicketsForBooking(bookingId)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(tickets)
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
                            ?: "Ticket creation conflict"
                        )
                )
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
                            ?: "Invalid request"
                        )
                )
            )
    }
}