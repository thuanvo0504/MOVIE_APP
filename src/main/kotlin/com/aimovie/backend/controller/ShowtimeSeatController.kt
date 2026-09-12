package com.aimovie.backend.controller

import com.aimovie.backend.entity.ShowtimeSeat
import com.aimovie.backend.service.ShowtimeSeatService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/showtime-seats")
class ShowtimeSeatController(
    private val showtimeSeatService: ShowtimeSeatService
) {

    @GetMapping("/showtime/{showtimeId}")
    fun getSeatsByShowtime(
        @PathVariable showtimeId: Long
    ): ResponseEntity<List<ShowtimeSeat>> {
        return ResponseEntity.ok(
            showtimeSeatService.findByShowtimeId(showtimeId)
        )
    }

    @GetMapping("/{showtimeSeatId}")
    fun getShowtimeSeatById(
        @PathVariable showtimeSeatId: Long
    ): ResponseEntity<ShowtimeSeat> {

        val showtimeSeat = showtimeSeatService.findById(showtimeSeatId)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(showtimeSeat)
    }
}