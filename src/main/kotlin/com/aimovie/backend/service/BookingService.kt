package com.aimovie.backend.service

import com.aimovie.backend.entity.Booking
import com.aimovie.backend.entity.BookingSeat
import com.aimovie.backend.repository.BookingRepository
import com.aimovie.backend.repository.BookingSeatRepository
import com.aimovie.backend.repository.SeatRepository
import com.aimovie.backend.repository.SeatTypePricingRepository
import com.aimovie.backend.repository.ShowtimeRepository
import com.aimovie.backend.repository.ShowtimeSeatRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional
class BookingService(
    private val bookingRepository: BookingRepository,
    private val bookingSeatRepository: BookingSeatRepository,
    private val showtimeSeatRepository: ShowtimeSeatRepository,
    private val seatRepository: SeatRepository,
    private val seatTypePricingRepository: SeatTypePricingRepository,
    private val showtimeRepository: ShowtimeRepository
) {

    fun findAll(userId: Long): List<Booking> {
        return bookingRepository.findByUserId(userId)
    }

    fun findById(
        bookingId: Long,
        userId: Long
    ): Booking? {
        val booking = bookingRepository.findById(bookingId)
            .orElse(null)

        return if (booking?.userId == userId) {
            booking
        } else {
            null
        }
    }

    fun findByBookingCode(
        bookingCode: String,
        userId: Long
    ): Booking? {
        val booking = bookingRepository.findByBookingCode(bookingCode)

        return if (booking?.userId == userId) {
            booking
        } else {
            null
        }
    }

    @Transactional
    fun createBooking(
        userId: Long,
        showtimeId: Long,
        showtimeSeatIds: List<Long>
    ): Booking {

        val showtime = showtimeRepository.findById(showtimeId)
            .orElseThrow {
                IllegalArgumentException("Showtime not found")
            }

        if (showtime.status != "AVAILABLE") {
            throw IllegalStateException(
                "Showtime is not available"
            )
        }

        if (showtimeSeatIds.isEmpty()) {
            throw IllegalArgumentException(
                "At least one seat is required"
            )
        }

        if (showtimeSeatIds.size != showtimeSeatIds.distinct().size) {
            throw IllegalArgumentException(
                "Duplicate seat IDs are not allowed"
            )
        }

        val lockedSeats =
            showtimeSeatRepository.findByShowtimeIdForUpdate(showtimeId)

        val selectedSeats =
            lockedSeats.filter {
                it.showtimeSeatId in showtimeSeatIds
            }

        if (selectedSeats.size != showtimeSeatIds.size) {
            throw IllegalArgumentException(
                "One or more seats do not belong to this showtime"
            )
        }

        if (selectedSeats.any { it.status != "AVAILABLE" }) {
            throw IllegalStateException(
                "One or more selected seats are already booked"
            )
        }

        val pricingList =
            seatTypePricingRepository.findByShowtimeId(showtimeId)

        val pricingMap =
            pricingList.associateBy { it.seatTypeId }

        val bookingSeats =
            selectedSeats.map { showtimeSeat ->

                val seatId = showtimeSeat.seatId

                val seat = seatRepository.findById(seatId)
                    .orElseThrow {
                        IllegalArgumentException(
                            "Seat not found: $seatId"
                        )
                    }

                val price =
                    pricingMap[seat.seatTypeId]?.price
                        ?: throw IllegalArgumentException(
                            "Price not found for seat type: ${seat.seatTypeId}"
                        )

                BookingSeat().apply {
                    this.showtimeSeatId =
                        showtimeSeat.showtimeSeatId!!

                    this.price = price
                }
            }

        val totalAmount =
            bookingSeats.fold(BigDecimal.ZERO) { total, bookingSeat ->
                total.add(bookingSeat.price)
            }

        val now = LocalDateTime.now()

        val booking = Booking().apply {
            this.userId = userId
            this.showtimeId = showtimeId
            this.bookingCode = "BK-${UUID.randomUUID()}"
            this.totalAmount = totalAmount
            this.status = "CONFIRMED"
            this.createdAt = now
            this.updatedAt = now
        }

        val savedBooking =
            bookingRepository.save(booking)

        bookingSeats.forEach { bookingSeat ->
            bookingSeat.bookingId =
                savedBooking.bookingId!!

            bookingSeatRepository.save(bookingSeat)
        }

        selectedSeats.forEach { showtimeSeat ->
            showtimeSeat.status = "BOOKED"
            showtimeSeat.bookedAt = now
            showtimeSeat.heldAt = null
        }

        showtimeSeatRepository.saveAll(selectedSeats)

        return savedBooking
    }
}