package com.aimovie.backend.repository

import com.aimovie.backend.entity.Showtime
import org.springframework.data.jpa.repository.JpaRepository

interface ShowtimeRepository : JpaRepository<Showtime, Long>