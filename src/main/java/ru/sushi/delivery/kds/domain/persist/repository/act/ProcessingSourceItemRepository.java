package ru.sushi.delivery.kds.domain.persist.repository.act;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sushi.delivery.kds.domain.persist.entity.act.ProcessingSourceItem;

@Repository
public interface ProcessingSourceItemRepository extends JpaRepository<ProcessingSourceItem, Long> {

    @Modifying
    @Transactional
    void deleteByProcessingActId(Long processingActId);
}
