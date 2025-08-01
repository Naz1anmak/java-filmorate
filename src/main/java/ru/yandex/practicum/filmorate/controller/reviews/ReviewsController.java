package ru.yandex.practicum.filmorate.controller.reviews;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.review.Reviews;
import ru.yandex.practicum.filmorate.service.ReviewsService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewsController {
    private final ReviewsService reviewsService;

    @PostMapping
    public Reviews create(@Valid @RequestBody Reviews reviews) {
        return reviewsService.create(reviews);
    }

    @PutMapping
    public Reviews update(@Valid @RequestBody Reviews reviews) {
        return reviewsService.update(reviews);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        reviewsService.delete(id);
    }

    @GetMapping("/{id}")
    public Reviews findById(@PathVariable("id") Long id) {
        return reviewsService.findById(id);
    }

    @GetMapping()
    public List<Reviews> findAll(@RequestParam(required = false) Long filmId,
                                 @RequestParam(defaultValue = "10") int count) {
        return reviewsService.findAll(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        reviewsService.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable Long id, @PathVariable Long userId) {
        reviewsService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        reviewsService.deleteLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislike(@PathVariable Long id, @PathVariable Long userId) {
        reviewsService.deleteDislike(id, userId);
    }
}
