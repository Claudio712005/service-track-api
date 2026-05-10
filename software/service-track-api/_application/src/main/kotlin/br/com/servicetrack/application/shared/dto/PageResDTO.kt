package br.com.servicetrack.application.shared.dto

import kotlin.math.ceil

data class PageResDTO<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val total: Long,
    val totalPages: Int
) {
    companion object {
        fun <T> de(content: List<T>, page: Int, size: Int, total: Long): PageResDTO<T> {
            val totalPages = if (size == 0) 0 else ceil(total.toDouble() / size).toInt()
            return PageResDTO(content, page, size, total, totalPages)
        }
    }
}
