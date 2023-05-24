package ru.practicum.shareit.booking.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.util.exception.NotFoundException;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingMapper {

    private final ItemRepository itemRepository;

    public BookingDto toBookingDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .booker(booking.getBooker())
                .item(booking.getItem())
                .status(booking.getStatus())
                .build();
    }

    public Booking toBooking(BookingDto bookingDto) {
        return Booking.builder()
                .id(bookingDto.getId())
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .booker(bookingDto.getBooker())
                .item(bookingDto.getItem())
                .status(bookingDto.getStatus())
                .build();
    }

    public Booking toBooking(BookingCreateDto bookingCreateDto) {
        return Booking.builder()
                .start(bookingCreateDto.getStart())
                .end(bookingCreateDto.getEnd())
                .item(getItem(bookingCreateDto.getItemId()))
                .build();
    }

    private Item getItem(Long itemId) {
        Item item = itemRepository.findById(itemId).get();
        if (item == null) {
            log.error("Вещь с id: {} не найден.", itemId);
            throw new NotFoundException("Вещь с id: " + itemId + " не найдена.");
        }
        return item;
    }
}
