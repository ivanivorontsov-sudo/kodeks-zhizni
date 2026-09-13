package org.ivanivorontsov.kodekszhizni.data

/**
 * Seed examples removed in 1.1.0 — new installs start empty.
 * Existing seed rows are deleted by Room migration / onOpen cleanup.
 */
object SeedData {
    fun articles(@Suppress("UNUSED_PARAMETER") now: Long = System.currentTimeMillis()): List<Article> =
        emptyList()
}
