package com.huynhliem.repository;

import com.huynhliem.model.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {
    List<TicketType> findByConcertId(Long concertId);

    @Modifying
    @Query("UPDATE TicketType t SET t.remainingQuantity = t.remainingQuantity - :qty WHERE t.id = :id AND t.remainingQuantity >= :qty")
    int decrementTicketQuantity(@Param("id") Long id, @Param("qty") Integer qty);

    @Modifying
    @Query("UPDATE TicketType t SET t.remainingQuantity = t.remainingQuantity + :qty WHERE t.id = :id")
    int incrementTicketQuantity(@Param("id") Long id, @Param("qty") Integer qty);
}
