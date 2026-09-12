package com.aimovie.backend.service

import com.aimovie.backend.entity.Ticket
import com.aimovie.backend.repository.BookingRepository
import com.aimovie.backend.repository.BookingSeatRepository
import com.aimovie.backend.repository.PaymentRepository
import com.aimovie.backend.repository.TicketRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class TicketService(
    private val ticketRepository: TicketRepository,
    private val bookingRepository: BookingRepository,
    private val bookingSeatRepository: BookingSeatRepository,
    private val paymentRepository: PaymentRepository
) {

    fun findAll(): List<Ticket> {
        return ticketRepository.findAll()
    }

    fun findById(ticketId: Long): Ticket? {
        return ticketRepository.findById(ticketId).orElse(null)
    }

    fun findByBookingId(bookingId: Long): List<Ticket> {
        return ticketRepository.findByBookingId(bookingId)
    }

    fun findByBookingSeatId(bookingSeatId: Long): Ticket? {
        return ticketRepository.findByBookingSeatId(bookingSeatId)
    }

    fun findByQrCode(qrCode: String): Ticket? {
        return ticketRepository.findByQrCode(qrCode)
    }

    @Transactional
    fun createTicketsForBooking(bookingId: Long): List<Ticket> {

        val booking = bookingRepository.findById(bookingId)
            .orElseThrow {
                IllegalArgumentException("Booking not found: $bookingId")
            }

        if (booking.status != "CONFIRMED") {
            throw IllegalStateException(
                "Booking is not confirmed"
            )
        }

        val hasSuccessfulPayment = paymentRepository
            .findByBookingId(bookingId)
            .any { it.status == "SUCCESS" }

        if (!hasSuccessfulPayment) {
            throw IllegalStateException(
                "Booking has not been paid successfully"
            )
        }

        val bookingSeats = bookingSeatRepository
            .findByBookingId(bookingId)

        if (bookingSeats.isEmpty()) {
            throw IllegalArgumentException(
                "No booking seats found for booking: $bookingId"
            )
        }

        val now = LocalDateTime.now()

        return bookingSeats.map { bookingSeat ->

            val bookingSeatId = bookingSeat.bookingSeatId!!

            val existingTicket =
                ticketRepository.findByBookingSeatId(bookingSeatId)

            existingTicket ?: run {

                val ticket = Ticket().apply {
                    this.bookingId = bookingId
                    this.bookingSeatId = bookingSeatId
                    this.qrCode =
                        "QR-${bookingId}-${bookingSeatId}-${UUID.randomUUID()}"
                    this.status = "VALID"
                    this.createdAt = now
                }

                ticketRepository.save(ticket)
            }
        }
    }
}