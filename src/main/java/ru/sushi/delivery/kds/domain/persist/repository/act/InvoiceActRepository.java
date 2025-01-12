package ru.sushi.delivery.kds.domain.persist.repository.act;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sushi.delivery.kds.domain.persist.entity.act.InvoiceAct;

import java.util.List;

@Repository
public interface InvoiceActRepository extends JpaRepository<InvoiceAct, Long> {
    @Query("""
        select i from InvoiceAct i
        where (:vendorFilter is null or i.vendor = :vendorFilter)
    """)
    //  and (:fromDate is null or i.date >= :fromDate)
//    List<InvoiceAct> findFiltered(String vendorFilter, LocalDateTime fromDate, Pageable pageable);
    List<InvoiceAct> findFiltered(String vendorFilter, Pageable pageable);
}
