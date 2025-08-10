
package me.seungwon.kkotdam.flower

import org.springframework.data.jpa.repository.JpaRepository

interface FlowerRepository : JpaRepository<Flower, Long> {
    fun findByNameContaining(name: String): List<Flower>
}
