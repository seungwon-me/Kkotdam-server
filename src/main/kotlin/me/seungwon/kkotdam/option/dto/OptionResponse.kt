
package me.seungwon.kkotdam.option.dto

data class OptionResponse(
    val occasions: List<Option>,
    val recipients: List<Option>,
    val moods: List<Option>,
    val sizes: List<Option>
)

data class Option(
    val value: String,
    val label: String
)
