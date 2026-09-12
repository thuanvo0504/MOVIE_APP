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

    fun findAll(): List<Booking> {
        return bookingRepository.findAll()
    }

    fun findById(bookingId: Long): Booking? {
        return bookingRepository.findById(bookingId).orElse(null)
    }

    fun findByBookingCode(bookingCode: String): Booking? {
        return bookingRepository.findByBookingCode(bookingCode)
    }

    @Transactional
    fun createBooking(
        userId: Long,
        showtimeId: Long,
        showtimeSeatIds: List<Long>
    ): Booking {

        // 1. Kiểm tra suất chiếu
        val showtime = showtimeRepository.findById(showtimeId)
            .orElseThrow {
                IllegalArgumentException("Showtime not found")
            }

        if (showtime.status != "AVAILABLE") {
            throw IllegalStateException("Showtime is not available")
        }

        // 2. Kiểm tra danh sách ghế
        if (showtimeSeatIds.isEmpty()) {
            throw IllegalArgumentException("At least one seat is required")
        }

        if (showtimeSeatIds.size != showtimeSeatIds.distinct().size) {
            throw IllegalArgumentException("Duplicate seat IDs are not allowed")
        }

        // 3. LOCK các ghế của suất chiếu
        val lockedSeats = showtimeSeatRepository
            .findByShowtimeIdForUpdate(showtimeId)

        // 4. Chỉ lấy các ghế mà client yêu cầu
        val selectedSeats = lockedSeats.filter {
            it.showtimeSeatId in showtimeSeatIds
        }

        // 5. Kiểm tra tất cả ghế có thuộc suất chiếu hay không
        if (selectedSeats.size != showtimeSeatIds.size) {
            throw IllegalArgumentException(
                "One or more seats do not belong to this showtime"
            )
        }

        // 6. Kiểm tra trạng thái ghế sau khi đã LOCK
        if (selectedSeats.any { it.status != "AVAILABLE" }) {
            throw IllegalStateException(
                "One or more selected seats are already booked"
            )
        }

        // 7. Lấy bảng giá theo suất chiếu
        val pricingList = seatTypePricingRepository
            .findByShowtimeId(showtimeId)

        val pricingMap = pricingList.associateBy {
            it.seatTypeId
        }

        // 8. Tạo BookingSeat và tính giá từ DATABASE
        val bookingSeats = selectedSeats.map { showtimeSeat ->

            val seatId = showtimeSeat.seatId

            val seat = seatRepository.findById(seatId)
                .orElseThrow {
                    IllegalArgumentException(
                        "Seat not found: $seatId"
                    )
                }

            val price = pricingMap[seat.seatTypeId]?.price
                ?: throw IllegalArgumentException(
                    "Price not found for seat type: ${seat.seatTypeId}"
                )

            BookingSeat().apply {
                this.showtimeSeatId =
                    showtimeSeat.showtimeSeatId!!

                this.price = price
            }
        }

        // 9. Tính tổng tiền ở BACKEND
        val totalAmount = bookingSeats.fold(
            BigDecimal.ZERO
        ) { total, bookingSeat ->
            total.add(bookingSeat.price)
        }

        // 10. Tạo Booking
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

        val savedBooking = bookingRepository.save(booking)

        // 11. Gắn BOOKING_ID cho BookingSeat
        bookingSeats.forEach {
            it.bookingId = savedBooking.bookingId!!
        }

        // 12. Lưu BookingSeat
        bookingSeatRepository.saveAll(bookingSeats)

        // 13. Chuyển trạng thái ghế thành BOOKED
        selectedSeats.forEach {
            it.status = "BOOKED"
            it.bookedAt = now
            it.heldAt = null
        }

        showtimeSeatRepository.saveAll(selectedSeats)

        // 14. Trả Booking vừa tạo
        return savedBooking
    }
}