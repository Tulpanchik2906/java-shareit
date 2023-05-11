package ru.practicum.shareit.booking;

/*
    Статус бронирования
 */
public enum BookingStatus {
    WAITING, // новое бронирование, ожидает одобрения
    APPROVED, // бронирование подтверждено владельцем
    REJECTED, // бронированиео тклонено владельцем
    CANCELED // бронирование отменено создателем

}
