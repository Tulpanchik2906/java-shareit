package ru.practicum.shareit.request.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "requests")
public class ItemRequest {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // уникальный идентификатор запроса;

    @Column(name = "description")
    private String description; // текст запроса, содержащий описание требуемой вещи;

    @ManyToOne()
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User requester; // пользователь, создавший запрос;

    @Column(name = "create_date")
    private LocalDateTime created; // дата и время создания запроса.

    @Transient
    private List<Item> items;

}
