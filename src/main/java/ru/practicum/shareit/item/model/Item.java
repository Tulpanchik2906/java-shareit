package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;

/**
 * TODO Sprint add-controllers.
 */
@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "items")
public class Item {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;// уникальный идентификатор вещи;
    @Column(name = "name")
    private String name; // краткое название;
    @Column(name = "description")
    private String description; // развёрнутое описание;
    @Column(name = "available")
    private Boolean available; // статус о том, доступна или нет вещь для аренды;
    @ManyToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    private User owner; // владелец вещи;
    @OneToOne
    @JoinColumn(name = "request_id", referencedColumnName = "id")
    private ItemRequest request; // если вещь была создана по запросу другого пользователя,
    // то в этом поле будет храниться ссылка на соответствующий запрос
}
