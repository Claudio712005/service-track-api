package br.com.servicetrack.domain.valueObject

@JvmInline
value class PhoneNumberTest(val value: String) {
    init {
        require(value.length >= 8)
    }
}