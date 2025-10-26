package com.travelagent.pos.utils

import java.util.regex.Pattern

object InputValidator {

    // ==================== PHONE NUMBER VALIDATION ====================
    /**
     * Validates Indonesian phone number
     * Rules:
     * - Must start with 08
     * - Must be 10-13 digits total
     * - Only contains numbers
     */
    fun validatePhoneNumber(phone: String): ValidationResult {
        val cleaned = phone.trim()

        return when {
            cleaned.isEmpty() -> ValidationResult.Error("Nomor telepon tidak boleh kosong")
            !cleaned.startsWith("08") -> ValidationResult.Error("Nomor telepon harus diawali dengan 08")
            !cleaned.matches(Regex("^08[0-9]{8,11}$")) ->
                ValidationResult.Error("Format nomor telepon salah (08xxxxxxxxx, 10-13 digit)")
            else -> ValidationResult.Success
        }
    }

    // ==================== VEHICLE PLATE NUMBER VALIDATION ====================
    /**
     * Validates Indonesian vehicle plate number
     * Rules:
     * - Format: B 1234 ABC or B 1234 AB
     * - Letters must be UPPERCASE
     * - Area code: 1-2 letters
     * - Number: 1-4 digits
     * - Series: 1-3 letters
     */
    fun validatePlateNumber(plate: String): ValidationResult {
        val cleaned = plate.trim()

        return when {
            cleaned.isEmpty() -> ValidationResult.Error("Nomor polisi tidak boleh kosong")
            cleaned.length < 5 -> ValidationResult.Error("Nomor polisi terlalu pendek")
            !isValidPlateFormat(cleaned) ->
                ValidationResult.Error("Format nomor polisi salah (contoh: B 1234 ABC)")
            !hasUppercaseLetters(cleaned) ->
                ValidationResult.Error("Huruf pada nomor polisi harus KAPITAL")
            else -> ValidationResult.Success
        }
    }

    /**
     * Auto-formats plate number to correct format with uppercase
     */
    fun formatPlateNumber(input: String): String {
        // Remove extra spaces and convert to uppercase
        val cleaned = input.trim().uppercase().replace(Regex("\\s+"), " ")

        // Try to format to standard pattern: L #### LL or LL #### LLL
        val parts = cleaned.split(" ")
        return when {
            parts.size == 3 -> {
                val area = parts[0].filter { it.isLetter() }
                val number = parts[1].filter { it.isDigit() }
                val series = parts[2].filter { it.isLetter() }
                "$area $number $series"
            }
            else -> cleaned
        }
    }

    private fun isValidPlateFormat(plate: String): Boolean {
        // Pattern: 1-2 letters, space, 1-4 digits, space, 1-3 letters
        val pattern = Pattern.compile("^[A-Z]{1,2}\\s\\d{1,4}\\s[A-Z]{1,3}$")
        return pattern.matcher(plate).matches()
    }

    private fun hasUppercaseLetters(plate: String): Boolean {
        val letters = plate.filter { it.isLetter() }
        return letters.all { it.isUpperCase() }
    }

    // ==================== PRICE VALIDATION ====================
    /**
     * Validates ticket price
     * Rules:
     * - Must be positive number
     * - Minimum: Rp 10,000
     * - Maximum: Rp 10,000,000
     * - No decimal points (Indonesian Rupiah)
     */
    fun validatePrice(price: String): ValidationResult {
        val cleaned = price.trim()

        return when {
            cleaned.isEmpty() -> ValidationResult.Error("Harga tidak boleh kosong")
            !cleaned.matches(Regex("^[0-9]+$")) ->
                ValidationResult.Error("Harga hanya boleh berisi angka")
            else -> {
                val priceValue = cleaned.toLongOrNull()
                when {
                    priceValue == null -> ValidationResult.Error("Format harga tidak valid")
                    priceValue < 10_000 ->
                        ValidationResult.Error("Harga minimum Rp 10.000")
                    priceValue > 10_000_000 ->
                        ValidationResult.Error("Harga maksimum Rp 10.000.000")
                    else -> ValidationResult.Success
                }
            }
        }
    }

    /**
     * Formats price with thousand separators
     */
    fun formatPrice(price: Long): String {
        return String.format("%,d", price).replace(',', '.')
    }

    // ==================== NAME VALIDATION ====================
    fun validateName(name: String, fieldName: String = "Nama"): ValidationResult {
        val cleaned = name.trim()

        return when {
            cleaned.isEmpty() -> ValidationResult.Error("$fieldName tidak boleh kosong")
            cleaned.length < 3 -> ValidationResult.Error("$fieldName terlalu pendek (min 3 karakter)")
            cleaned.length > 100 -> ValidationResult.Error("$fieldName terlalu panjang (max 100 karakter)")
            !cleaned.matches(Regex("^[a-zA-Z\\s.'-]+$")) ->
                ValidationResult.Error("$fieldName hanya boleh berisi huruf, spasi, titik, apostrof, dan strip")
            else -> ValidationResult.Success
        }
    }

    // ==================== ADDRESS VALIDATION ====================
    fun validateAddress(address: String): ValidationResult {
        val cleaned = address.trim()

        return when {
            cleaned.isEmpty() -> ValidationResult.Error("Alamat tidak boleh kosong")
            cleaned.length < 5 -> ValidationResult.Error("Alamat terlalu pendek (min 5 karakter)")
            cleaned.length > 200 -> ValidationResult.Error("Alamat terlalu panjang (max 200 karakter)")
            else -> ValidationResult.Success
        }
    }

    // ==================== PAYMENT AMOUNT VALIDATION ====================
    fun validatePaymentAmount(amount: String, remaining: Double): ValidationResult {
        val cleaned = amount.trim()

        return when {
            cleaned.isEmpty() -> ValidationResult.Error("Jumlah pembayaran tidak boleh kosong")
            !cleaned.matches(Regex("^[0-9]+$")) ->
                ValidationResult.Error("Jumlah hanya boleh berisi angka")
            else -> {
                val amountValue = cleaned.toDoubleOrNull()
                when {
                    amountValue == null -> ValidationResult.Error("Format jumlah tidak valid")
                    amountValue <= 0 -> ValidationResult.Error("Jumlah harus lebih dari 0")
                    amountValue > remaining ->
                        ValidationResult.Error("Jumlah melebihi sisa tagihan (Rp ${formatPrice(remaining.toLong())})")
                    else -> ValidationResult.Success
                }
            }
        }
    }
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}