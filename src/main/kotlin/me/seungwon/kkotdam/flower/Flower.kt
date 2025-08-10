
package me.seungwon.kkotdam.flower

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Flower(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true)
    val flowerId: String,

    val name: String,

    val imageUrl: String,

    val meaning: String,

    val color: String,

    val season: String
)
