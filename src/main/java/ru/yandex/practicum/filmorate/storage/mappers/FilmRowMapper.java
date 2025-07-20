package ru.yandex.practicum.filmorate.storage.mappers;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.storage.film.GenreRepository;
import ru.yandex.practicum.filmorate.storage.film.LikeRepository;
import ru.yandex.practicum.filmorate.storage.film.MpaRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

@Component
@AllArgsConstructor
public class FilmRowMapper implements RowMapper<Film> {
    private final MpaRepository mpaRepository;
    private final GenreRepository genreRepository;
    private final LikeRepository likeRepository;

    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = Film.builder().build();
        film.setId(resultSet.getLong("film_id"));
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));
        film.setReleaseDate(resultSet.getDate("release_date").toLocalDate());
        film.setDuration(resultSet.getInt("duration"));

        Long filmId = film.getId();
        if (filmId > 0 && !resultSet.wasNull()) {
            film.setGenres(new HashSet<>(genreRepository.findByFilmId(filmId)));
        } else {
            film.setGenres(new HashSet<>());
        }

        Integer mpaId = resultSet.getObject("mpa_rating_id", Integer.class);
        if (mpaId != null) {
            film.setMpaRating(mpaRepository.findById(mpaId)
                    .orElse(null));
        }

        Set<Long> likes = likeRepository.findLikesByFilm(filmId);
        film.getMovieRating().clear();
        film.getMovieRating().addAll(likes);

        return film;
    }
}