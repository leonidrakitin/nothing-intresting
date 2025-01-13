package ru.sushi.delivery.kds.domain.persist.repository.act;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sushi.delivery.kds.domain.persist.entity.act.InvoiceActItem;

import java.util.List;

@Repository
public interface InvoiceActItemRepository extends JpaRepository<InvoiceActItem, Long> {

    List<InvoiceActItem> findAllByInvoiceActId(Long invoiceActId);

    @Modifying
    @Transactional
    void deleteByInvoiceActId(Long invoiceId);
}
