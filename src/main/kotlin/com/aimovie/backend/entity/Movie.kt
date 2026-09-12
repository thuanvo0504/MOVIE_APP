package com.aimovie.backend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime
import java.math.BigDecimal
@Entity
@Table(name = "MOVIES")
class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MOVIE_ID")
    var movieId: Long? = null

    @Column(name = "TITLE", nullable = false)
    var title: String = ""

    @Column(name = "DESCRIPTION")
    var description: String? = null

    @Column(name = "DURATION_MINUTES", nullable = false)
    var durationMinutes: Int = 0

    @Column(name = "RELEASE_DATE")
    var releaseDate: LocalDate? = null

    @Column(name = "RATING")
    var rating: BigDecimal? = null

    @Column(name = "POSTER_URL")
    var posterUrl: String? = null

    @Column(name = "TRAILER_URL")
    var trailerUrl: String? = null

    @Column(name = "STATUS", nullable = false)
    var status: String = ""

    @Column(name = "CREATED_AT")
    var createdAt: LocalDateTime? = null

    @Column(name = "UPDATED_AT")
    var updatedAt: LocalDateTime? = null
}