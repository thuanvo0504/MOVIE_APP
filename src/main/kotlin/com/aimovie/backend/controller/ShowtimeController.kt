package com.aimovie.backend.controller

import com.aimovie.backend.entity.Showtime
import com.aimovie.backend.service.ShowtimeService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/showtimes")
class ShowtimeController(
    private val showtimeService: ShowtimeService
) {

    @GetMapping
    fun getAllShowtimes(): ResponseEntity<List<Showtime>> {
        return ResponseEntity.ok(showtimeService.findAll())
    }

    @GetMapping("/{showtimeId}")
    fun getShowtimeById(
        @PathVariable showtimeId: Long
    ): ResponseEntity<Showtime> {
        val showtime = showtimeService.findById(showtimeId)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(showtime)
    }
}