package com.example.todayexercise.repository.Workout;

import com.example.todayexercise.entity.User;
import com.example.todayexercise.entity.Workout;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WorkoutRepository extends JpaRepository<Workout,Long>,WorkoutRepositoryCustom {
    @Query("SELECT w.id,w.createdAt,w.cardioExTime,ce.exName, ce.km,w.strengthExTime, se.part, se.exName, se.kg, se.rep, se.set " +
            "FROM Workout w " +
            "LEFT JOIN CardioEx ce ON w = ce.workoutId " +
            "LEFT JOIN StrengthEx se ON w = se.workoutId " +
            "WHERE w.usersId = :user " +
            "AND(:cursor = 0 OR w.id < :cursor) "+
            "ORDER BY w.createdAt DESC ")
    Page<Object[]> findWorkoutWithCardioAndStrength(
            @Param("user") User user,
            @Param("cursor") Long cursor,
            Pageable pageRequest);


    @Query("SELECT COUNT(w) " +
            "FROM Workout w " +
            "LEFT JOIN CardioEx ce ON w = ce.workoutId " +
            "LEFT JOIN StrengthEx se ON w = se.workoutId " +
            "WHERE w.usersId = :user AND w.id < :cursor")
    Long countWorkoutsBeforeCursor(@Param("user") User user, @Param("cursor") Long cursor);

}
