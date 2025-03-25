package com.cumaliguzel.barberappointment.model

import com.cumaliguzel.barberappointment.data.Appointment
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Güvenli şekilde tarih dönüşümü yapan extension function
fun Appointment.getDateAsLocalDate(): LocalDate {
    return try {
        LocalDate.parse(this.date, DateTimeFormatter.ISO_DATE)
    } catch (e: Exception) {
        // Tarih ayrıştırılamazsa bugünü döndür
        LocalDate.now()
    }
} 